package com.practice.jobqueue.controller.dto;

import com.practice.jobqueue.domain.JobStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
public class JobResponse {
    private Long id;
    private String type;
    private String payload;
    private JobStatus status;
    private Integer priority;
    private OffsetDateTime runAt;
    private Integer attemptCount;
    private Integer maxAttempts;
    private String lastError;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime startedAt;
    private OffsetDateTime finishedAt;
}
