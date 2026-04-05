package com.practice.jobqueue.repository;

import com.practice.jobqueue.domain.Job;
import com.practice.jobqueue.domain.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Persistence operations for {@link Job} entities.
 */
public interface JobRepository extends JpaRepository<Job, Long> {

    /**
     * Returns all jobs with the given status.
     */
    List<Job> findByStatus(JobStatus status);

    /**
     * Returns all jobs with the given status and type.
     */
    List<Job> findByStatusAndType(JobStatus status, String type);

}
