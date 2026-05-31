package com.practice.jobqueue.worker.queue;

import com.practice.jobqueue.domain.Job;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

@Component
public class JobQueueManager {

    private final BlockingQueue<Job> blockingQueue = new LinkedBlockingDeque<>(10);

    public boolean enqueue(Job job) {
        return blockingQueue.offer(job);
    }

    public Job dequeue() throws InterruptedException {
        return blockingQueue.take();
    }

    public int remainingCapacity() {
        return blockingQueue.remainingCapacity();
    }

}
