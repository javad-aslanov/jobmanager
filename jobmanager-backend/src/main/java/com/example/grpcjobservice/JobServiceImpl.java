package com.example.grpcjobservice;

import com.example.grpcjobservice.JobServiceGrpc.JobServiceImplBase;
import io.grpc.stub.StreamObserver;
import com.google.protobuf.Timestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class JobServiceImpl extends JobServiceImplBase {

    private List<Job> jobList;

    public JobServiceImpl() {
        jobList = new ArrayList<>();

        jobList.add(Job.newBuilder()
                .setJobId("1")
                .setJobName("Job 1")
                .setJobStatus(JobStatus.SUBMITTED)
                .setCreatedAt(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
                .build());

        jobList.add(Job.newBuilder()
                .setJobId("2")
                .setJobName("Job 2")
                .setJobStatus(JobStatus.RUNNING)
                .setCreatedAt(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
                .build());
    }

    @Override
    public void listAllJobs(Empty request, StreamObserver<JobList> responseObserver) {
        JobList.Builder jobListBuilder = JobList.newBuilder();
        jobListBuilder.addAllJobs(jobList);
        responseObserver.onNext(jobListBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getJobDetails(JobId request, StreamObserver<JobDetails> responseObserver) {
        for (Job job : jobList) {
            if (job.getJobId().equals(request.getJobId())) {
                JobDetails jobDetails = JobDetails.newBuilder()
                        .setJobId(job.getJobId())
                        .setJobName(job.getJobName())
                        .setJobStatus(job.getJobStatus())
                        .setCreatedAt(job.getCreatedAt())
                        .build();
                responseObserver.onNext(jobDetails);
                responseObserver.onCompleted();
                return;
            }
        }
        responseObserver.onError(new Throwable("Job not found"));
    }
}
