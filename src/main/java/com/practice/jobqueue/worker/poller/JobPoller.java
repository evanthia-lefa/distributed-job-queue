package com.practice.jobqueue.worker.poller;

import com.practice.jobqueue.domain.Job;
import com.practice.jobqueue.service.JobService;
import com.practice.jobqueue.worker.queue.JobQueueManager;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class JobPoller {

    private final JobService jobService;
    private final JobQueueManager jobQueueManager;

    @Scheduled(fixedDelayString = "${job.poller.fixed-delay:5000}")
    public void poll() {
        int limit = jobQueueManager.remainingCapacity();
        if (limit == 0) return;  // queue is full, skip this cycle
        List<Job> jobs = pollDueJobs(limit);
        jobs.forEach(job -> {
            boolean isEnqueued = jobQueueManager.enqueue(job);
            if (!isEnqueued) {
                jobService.releaseJob(job); // reset to PENDING
            }
        });
    }

    public List<Job> pollDueJobs(int limit) {
        return jobService.claimJobs(limit);
    }
}
