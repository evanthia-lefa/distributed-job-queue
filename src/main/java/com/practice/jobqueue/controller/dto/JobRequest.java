package com.practice.jobqueue.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@Valid
public class JobRequest {
    @NotBlank(message = "type is required")
    @Size(max = 100, message = "type must not exceed 100 characters")
    private String type;

    @NotBlank(message = "payload is required")
    private String payload;

    @Min(value = 0, message = "priority must be at least 0")
    @Max(value = 10, message = "priority must not exceed 10")
    private Integer priority = 0;


    @FutureOrPresent(message = "runAt cannot be in the past")
    private OffsetDateTime runAt;
}
