package com.practice.jobqueue.mapping;

import com.practice.jobqueue.controller.dto.JobRequest;
import com.practice.jobqueue.controller.dto.JobResponse;
import com.practice.jobqueue.domain.Job;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface JobMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "type", source = "type")
    @Mapping(target = "payload", source = "payload")
    @Mapping(target = "runAt", source = "runAt")
    @Mapping(target = "priority", source = "priority")
    Job toJob(JobRequest jobRequest);

    JobResponse toJobResponse(Job job);

    List<JobResponse> toJobResponseList(List<Job> job);

}
