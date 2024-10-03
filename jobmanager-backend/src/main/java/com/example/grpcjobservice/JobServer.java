package com.example.grpcjobservice;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class JobServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(8080)
                .addService(new JobServiceImpl())
                .build();

        System.out.println("Server started on port 8080");
        server.start();
        server.awaitTermination();
    }
}
