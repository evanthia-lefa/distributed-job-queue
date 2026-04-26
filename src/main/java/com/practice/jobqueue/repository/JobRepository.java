package com.practice.jobqueue.repository;

import com.practice.jobqueue.domain.Job;
import com.practice.jobqueue.domain.JobStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
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


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")
    })
    @Query("""
            SELECT j
            FROM Job j
            WHERE j.status = :status
              AND j.runAt <= :now
              AND j.attemptCount < j.maxAttempts
            ORDER BY j.priority DESC, j.runAt ASC, j.id ASC
            """)
    List<Job> findDueJobsForUpdate(
            @Param("status") JobStatus status,
            @Param("now") OffsetDateTime now,
            Pageable pageable);

    @Modifying
    @Query("""
            UPDATE Job j
            SET j.status = :runningStatus,
                j.attemptCount = j.attemptCount + 1,
                j.startedAt = :now,
                j.updatedAt = :now
            WHERE j.id IN :ids
            """)
    int markJobsAsRunning(
            @Param("ids") List<Long> ids,
            @Param("runningStatus") JobStatus runningStatus,
            @Param("now") OffsetDateTime now);


}
