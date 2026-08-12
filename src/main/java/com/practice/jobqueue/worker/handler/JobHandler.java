package com.practice.jobqueue.worker.handler;

import com.practice.jobqueue.domain.Job;


public interface JobHandler {

    void handle(Job job);
}
