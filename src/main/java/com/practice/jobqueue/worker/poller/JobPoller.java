package com.practice.jobqueue.worker.poller;

import com.practice.jobqueue.domain.Job;
import com.practice.jobqueue.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class JobPoller {

    private final JobService jobService;

    private static final int DEFAULT_POLL_LIMIT = 10;


    @Scheduled(fixedDelayString = "${job.poller.fixed-delay:5000}")
    public void poll() {
        pollDueJobs();
    }

    public List<Job> pollDueJobs() {
        return pollDueJobs(DEFAULT_POLL_LIMIT);
    }

    public List<Job> pollDueJobs(int limit) {
        return jobService.claimJobs(limit);
    }
}
