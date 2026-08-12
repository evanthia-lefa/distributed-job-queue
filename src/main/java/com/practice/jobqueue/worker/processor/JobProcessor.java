package com.practice.jobqueue.worker.processor;

import com.practice.jobqueue.domain.Job;
import com.practice.jobqueue.service.JobService;
import com.practice.jobqueue.worker.handler.JobHandler;
import com.practice.jobqueue.worker.queue.JobQueueManager;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobProcessor {

    private final JobQueueManager jobQueueManager;
    private final JobHandler jobHandler;
    private final JobService jobService;


    @PostConstruct
    void startProcessor() {
        Thread processThread = new Thread(this::processLoop, "processThread");
        processThread.start();
    }


    private void processLoop() {
        log.debug("Process started");
        while (true) {
            try {
                Job job = jobQueueManager.dequeue();
                log.debug("Job is " + job.getId() + job.getType());
                jobHandler.handle(job);
                jobService.markJobSucceeded(job);
            } catch (Exception e) {
                log.error("Job processing failed", e);
            }

        }

    }

}
