package com.practice.jobqueue.service;

import com.practice.jobqueue.controller.dto.JobRequest;
import com.practice.jobqueue.controller.dto.JobResponse;
import com.practice.jobqueue.domain.Job;
import com.practice.jobqueue.domain.JobStatus;
import com.practice.jobqueue.mapping.JobMapper;
import com.practice.jobqueue.repository.JobRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobMapper jobMapper;

    @InjectMocks
    private JobService jobService;

    @Test
    void createJobSetsDefaultFieldsAndUsesCurrentTimeWhenRunAtMissing() {
        JobRequest request = new JobRequest();
        request.setType("email");
        request.setPayload("{\"to\":\"user@example.com\"}");
        request.setPriority(5);

        Job mappedJob = new Job();
        mappedJob.setType(request.getType());
        mappedJob.setPayload(request.getPayload());
        mappedJob.setPriority(request.getPriority());

        Job savedJob = new Job();
        savedJob.setId(1L);

        JobResponse expectedResponse = new JobResponse();
        expectedResponse.setId(1L);

        when(jobMapper.toJob(request)).thenReturn(mappedJob);
        when(jobRepository.save(any(Job.class))).thenReturn(savedJob);
        when(jobMapper.toJobResponse(savedJob)).thenReturn(expectedResponse);

        OffsetDateTime beforeCall = OffsetDateTime.now();
        JobResponse actualResponse = jobService.createJob(request);
        OffsetDateTime afterCall = OffsetDateTime.now();

        ArgumentCaptor<Job> savedJobCaptor = ArgumentCaptor.forClass(Job.class);
        verify(jobRepository).save(savedJobCaptor.capture());

        Job persistedJob = savedJobCaptor.getValue();
        assertEquals(JobStatus.PENDING, persistedJob.getStatus());
        assertEquals(0, persistedJob.getAttemptCount());
        assertNotNull(persistedJob.getRunAt());
        assertTrue(!persistedJob.getRunAt().isBefore(beforeCall));
        assertTrue(!persistedJob.getRunAt().isAfter(afterCall));
        assertSame(expectedResponse, actualResponse);
    }

    @Test
    void createJobUsesProvidedRunAt() {
        OffsetDateTime requestedRunAt = OffsetDateTime.parse("2026-03-21T10:15:30+02:00");

        JobRequest request = new JobRequest();
        request.setType("report");
        request.setPayload("{\"reportId\":42}");
        request.setRunAt(requestedRunAt);

        Job mappedJob = new Job();
        Job savedJob = new Job();
        JobResponse expectedResponse = new JobResponse();

        when(jobMapper.toJob(request)).thenReturn(mappedJob);
        when(jobRepository.save(any(Job.class))).thenReturn(savedJob);
        when(jobMapper.toJobResponse(savedJob)).thenReturn(expectedResponse);

        jobService.createJob(request);

        ArgumentCaptor<Job> savedJobCaptor = ArgumentCaptor.forClass(Job.class);
        verify(jobRepository).save(savedJobCaptor.capture());
        assertEquals(requestedRunAt, savedJobCaptor.getValue().getRunAt());
    }

    @Test
    void updateJobOverwritesEditableFields() {
        long jobId = 5L;
        OffsetDateTime requestedRunAt = OffsetDateTime.parse("2026-03-22T08:00:00+02:00");

        JobRequest request = new JobRequest();
        request.setType("invoice");
        request.setPayload("{\"invoiceId\":11}");
        request.setPriority(8);
        request.setRunAt(requestedRunAt);

        Job existingJob = new Job();
        existingJob.setId(jobId);
        existingJob.setStatus(JobStatus.PENDING);

        Job savedJob = new Job();
        JobResponse expectedResponse = new JobResponse();

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(existingJob));
        when(jobRepository.save(existingJob)).thenReturn(savedJob);
        when(jobMapper.toJobResponse(savedJob)).thenReturn(expectedResponse);

        JobResponse actualResponse = jobService.updateJob(jobId, request);

        assertEquals("invoice", existingJob.getType());
        assertEquals("{\"invoiceId\":11}", existingJob.getPayload());
        assertEquals(8, existingJob.getPriority());
        assertEquals(requestedRunAt, existingJob.getRunAt());
        assertNotNull(existingJob.getUpdatedAt());
        assertSame(expectedResponse, actualResponse);
    }

    @Test
    void getJobByIdReturnsMappedResponseWhenJobExists() {
        long jobId = 7L;
        Job job = new Job();
        JobResponse expectedResponse = new JobResponse();

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(jobMapper.toJobResponse(job)).thenReturn(expectedResponse);

        JobResponse actualResponse = jobService.getJobById(jobId);

        assertSame(expectedResponse, actualResponse);
    }

    @Test
    void getJobByIdThrowsWhenJobDoesNotExist() {
        long jobId = 99L;
        when(jobRepository.findById(jobId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> jobService.getJobById(jobId)
        );

        assertEquals("Job not found with id: 99", exception.getMessage());
        verify(jobMapper, never()).toJobResponse(any(Job.class));
    }

    @Test
    void cancelJobMarksJobAsFailed() {
        long jobId = 12L;
        Job existingJob = new Job();
        existingJob.setId(jobId);
        existingJob.setStatus(JobStatus.PENDING);

        Job savedJob = new Job();
        JobResponse expectedResponse = new JobResponse();

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(existingJob));
        when(jobRepository.save(existingJob)).thenReturn(savedJob);
        when(jobMapper.toJobResponse(savedJob)).thenReturn(expectedResponse);

        JobResponse actualResponse = jobService.cancelJob(jobId);

        assertEquals(JobStatus.FAILED, existingJob.getStatus());
        assertEquals("Job cancelled by user", existingJob.getLastError());
        assertNotNull(existingJob.getFinishedAt());
        assertNotNull(existingJob.getUpdatedAt());
        assertSame(expectedResponse, actualResponse);
    }

    @Test
    void getJobsByStatusReturnsMappedResponses() {
        JobStatus status = JobStatus.PENDING;
        List<Job> jobs = List.of(new Job(), new Job());
        List<JobResponse> expectedResponses = List.of(new JobResponse(), new JobResponse());

        when(jobRepository.findByStatus(status)).thenReturn(jobs);
        when(jobMapper.toJobResponseList(jobs)).thenReturn(expectedResponses);

        List<JobResponse> actualResponses = jobService.getJobsByStatus(status);

        assertSame(expectedResponses, actualResponses);
    }

    @Test
    void getJobsByStatusAndTypeReturnsMappedResponses() {
        JobStatus status = JobStatus.FAILED;
        String type = "email";
        List<Job> jobs = List.of(new Job());
        List<JobResponse> expectedResponses = List.of(new JobResponse());

        when(jobRepository.findByStatusAndType(status, type)).thenReturn(jobs);
        when(jobMapper.toJobResponseList(jobs)).thenReturn(expectedResponses);

        List<JobResponse> actualResponses = jobService.getJobsByStatusAndType(status, type);

        assertSame(expectedResponses, actualResponses);
    }
}
