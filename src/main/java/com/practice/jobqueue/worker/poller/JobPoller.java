package com.practice.jobqueue.worker.poller;

import com.practice.jobqueue.domain.Job;
import com.practice.jobqueue.service.JobService;
import com.practice.jobqueue.worker.queue.JobQueueManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class JobPoller {

    private final JobService jobService;
    private final JobQueueManager jobQueueManager;

    @Scheduled(fixedDelayString = "${job.poller.fixed-delay:5000}")
    public void poll() {
        int limit = jobQueueManager.remainingCapacity();
        if (limit == 0) {
            log.debug("Job queue is full, skipping poll cycle");
            return;
        }

        log.debug("Polling for due jobs, queue capacity available={}", limit);
        List<Job> jobs = pollDueJobs(limit);

        if (jobs.isEmpty()) {
            log.debug("No jobs to enqueue in this poll cycle");
            return;
        }

        jobs.forEach(job -> {
            boolean isEnqueued = jobQueueManager.enqueue(job);
            if (isEnqueued) {
                log.info("Job id={} enqueued for processing", job.getId());
            } else {
                log.warn("Failed to enqueue job id={}, releasing back to PENDING", job.getId());
                jobService.releaseJob(job);
            }
        });
    }

    public List<Job> pollDueJobs(int limit) {
        return jobService.claimJobs(limit);
    }
}
