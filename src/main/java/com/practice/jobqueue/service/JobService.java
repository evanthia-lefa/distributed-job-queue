package com.practice.jobqueue.service;

import com.practice.jobqueue.controller.dto.JobRequest;
import com.practice.jobqueue.controller.dto.JobResponse;
import com.practice.jobqueue.domain.Job;
import com.practice.jobqueue.domain.JobStatus;
import com.practice.jobqueue.mapping.JobMapper;
import com.practice.jobqueue.repository.JobRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class JobService {

    private final JobRepository jobRepository;
    private final JobMapper jobMapper;

    public JobResponse createJob(JobRequest jobRequest) {
        log.debug("Creating job of type '{}' with priority={}", jobRequest.getType(), jobRequest.getPriority());
        Job job = jobMapper.toJob(jobRequest);
        job.setRunAt(resolveRunAt(jobRequest.getRunAt()));
        Job savedJob = jobRepository.save(job);
        log.info("Job created: id={}, type='{}', runAt={}", savedJob.getId(), savedJob.getType(), savedJob.getRunAt());
        return jobMapper.toJobResponse(savedJob);
    }

    public JobResponse updateJob(Long id, JobRequest jobRequest) {
        log.debug("Updating job id={}", id);
        Job job = getExistingJob(id);
        job.setType(jobRequest.getType());
        job.setPayload(jobRequest.getPayload());
        job.setPriority(jobRequest.getPriority());
        job.setRunAt(resolveRunAt(jobRequest.getRunAt()));
        job.setUpdatedAt(OffsetDateTime.now());

        Job savedJob = jobRepository.save(job);
        log.info("Job updated: id={}, type='{}', status={}", savedJob.getId(), savedJob.getType(), savedJob.getStatus());
        return jobMapper.toJobResponse(savedJob);
    }

    public JobResponse getJobById(Long id) {
        Job job = getExistingJob(id);
        return jobMapper.toJobResponse(job);
    }

    public JobResponse cancelJob(Long id) {
        log.info("Cancelling job id={}", id);
        Job job = getExistingJob(id);
        OffsetDateTime now = OffsetDateTime.now();

        job.setStatus(JobStatus.FAILED);
        job.setLastError("Job cancelled by user");
        job.setFinishedAt(now);
        job.setUpdatedAt(now);

        Job savedJob = jobRepository.save(job);
        log.info("Job id={} marked as FAILED (cancelled by user)", id);
        return jobMapper.toJobResponse(savedJob);
    }

    public List<JobResponse> getJobsByStatus(JobStatus status) {
        List<Job> jobs = jobRepository.findByStatus(status);
        return jobMapper.toJobResponseList(jobs);
    }

    public List<JobResponse> getJobsByStatusAndType(JobStatus status, String type) {
        List<Job> jobs = jobRepository.findByStatusAndType(status, type);
        return jobMapper.toJobResponseList(jobs);
    }


    private Job getExistingJob(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Job not found with id={}", id);
                    return new EntityNotFoundException("Job not found with id: " + id);
                });
    }

    private OffsetDateTime resolveRunAt(OffsetDateTime runAt) {
        if (runAt == null) {
            return OffsetDateTime.now();
        } else {
            return runAt;
        }
    }

    @Transactional
    public List<Job> claimJobs(int batchSize) {
        log.debug("Claiming up to {} due jobs", batchSize);
        OffsetDateTime now = OffsetDateTime.now();

        List<Job> jobs = jobRepository.findDueJobsForUpdate(
                JobStatus.PENDING,
                now,
                PageRequest.of(0, batchSize)
        );

        if (jobs.isEmpty()) {
            log.debug("No due jobs found");
            return jobs;
        }

        List<Long> ids = jobs.stream()
                .map(Job::getId)
                .toList();

        jobRepository.markJobsAsRunning(ids, JobStatus.RUNNING, now);
        log.info("Claimed {} jobs: ids={}", jobs.size(), ids);

        return jobs;
    }


    @Transactional
    public void releaseJob(Job job) {
        log.warn("Releasing job id={} back to PENDING (queue was full)", job.getId());
        jobRepository.resetToPending(job.getId());
    }

    @Transactional
    public void markJobSucceeded(Job job){
        jobRepository.markJobSucceeded(job.getId());
    }
}
