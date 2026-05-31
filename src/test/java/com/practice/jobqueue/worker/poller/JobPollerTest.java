package com.practice.jobqueue.worker.poller;

import com.practice.jobqueue.domain.Job;
import com.practice.jobqueue.service.JobService;
import com.practice.jobqueue.worker.queue.JobQueueManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobPollerTest {

    @Mock
    private JobService jobService;
    @Mock
    private JobQueueManager jobQueueManager;

    @InjectMocks
    private JobPoller jobPoller;

    @Test
    void pollJobs() {
        List<Job> jobs = List.of(new Job(), new Job());

        when(jobQueueManager.remainingCapacity()).thenReturn(3);
        when(jobService.claimJobs(3)).thenReturn(jobs);
        when(jobQueueManager.enqueue(any(Job.class))).thenReturn(true);

        jobPoller.poll();

        verify(jobService).claimJobs(3);
        verify(jobQueueManager, times(2)).enqueue(any(Job.class));
    }

    @Test
    void pollJobsAndFailedEnqueue() {
        List<Job> jobs = List.of(new Job());

        when(jobQueueManager.remainingCapacity()).thenReturn(3);
        when(jobService.claimJobs(3)).thenReturn(jobs);
        when(jobQueueManager.enqueue(any(Job.class))).thenReturn(false);

        jobPoller.poll();

        verify(jobService).releaseJob(any(Job.class));
    }

}
