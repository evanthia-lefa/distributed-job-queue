package com.practice.jobqueue.worker.handler.impl;

import com.practice.jobqueue.domain.Job;
import com.practice.jobqueue.worker.handler.JobHandler;
import org.springframework.stereotype.Component;

@Component
public class EmailJobHandler implements JobHandler{
    public  void handle(Job job){
        // a mock service

    }
}
