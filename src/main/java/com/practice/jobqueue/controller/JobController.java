package com.practice.jobqueue.controller;

import com.practice.jobqueue.controller.dto.JobRequest;
import com.practice.jobqueue.controller.dto.JobResponse;
import com.practice.jobqueue.domain.JobStatus;
import com.practice.jobqueue.service.JobService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Exposes REST endpoints for creating, updating, querying.
 */
@Slf4j
@RestController
@RequestMapping("/v1/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    /**
     * Creates a new job in the queue.
     *
     * @param request the job details provided by the client
     * @return a {@code 201 Created} response containing the created job
     */
    @PostMapping
    public ResponseEntity<JobResponse> createJob(@RequestBody @Valid JobRequest request) {
        log.info("REST POST /v1/jobs - creating job of type '{}'", request.getType());
        JobResponse response = jobService.createJob(request);
        log.info("Job created successfully with id={}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Updates an existing job using the supplied request payload.
     *
     * @param id the identifier of the job to update
     * @param request the updated job details
     * @return a {@code 200 OK} response containing the updated job
     */
    @PutMapping("/{id}")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable @Positive(message = "id must be positive") Long id,
            @RequestBody @Valid JobRequest request) {
        log.info("REST PUT /v1/jobs/{} - updating job", id);
        JobResponse response = jobService.updateJob(id, request);
        log.info("Job id={} updated successfully", id);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a single job by its identifier.
     *
     * @param id the identifier of the job to fetch
     * @return a {@code 200 OK} response containing the requested job
     */
    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJobById(
            @PathVariable @Positive(message = "id must be positive") Long id) {
        log.debug("REST GET /v1/jobs/{} - fetching job", id);
        return ResponseEntity.ok(jobService.getJobById(id));
    }

    /**
     * Marks a job as cancelled.
     *
     * @param id the identifier of the job to cancel
     * @return a {@code 200 OK} response containing the updated job
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<JobResponse> cancelJob(
            @PathVariable @Positive(message = "id must be positive") Long id) {
        log.info("REST POST /v1/jobs/{}/cancel - cancelling job", id);
        JobResponse response = jobService.cancelJob(id);
        log.info("Job id={} cancelled successfully", id);
        return ResponseEntity.ok(response);
    }

    /**
     * Lists jobs by status and optionally filters them by type.
     *
     * @param status the required job status filter
     * @param type the optional job type filter
     * @return a {@code 200 OK} response containing matching jobs
     */
    @GetMapping
    public ResponseEntity<List<JobResponse>> getJobs(
            @RequestParam JobStatus status,
            @RequestParam(required = false) String type) {
        log.debug("REST GET /v1/jobs - querying jobs with status='{}', type='{}'", status, type);
        if (type == null || type.isBlank()) {
            return ResponseEntity.ok(jobService.getJobsByStatus(status));
        }

        return ResponseEntity.ok(jobService.getJobsByStatusAndType(status, type));
    }
}
