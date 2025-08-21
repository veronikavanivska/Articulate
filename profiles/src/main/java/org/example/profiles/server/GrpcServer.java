package org.example.profiles.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.example.profiles.service.ProfilesService;
import org.springframework.stereotype.Component;

@Component
public class GrpcServer {
    private final ProfilesService profilesService;

    public GrpcServer(ProfilesService profilesService) {
        this.profilesService = profilesService;
    }

    public void start() {
        try {
            Server server = ServerBuilder.
                    forPort(9090).
                    addService(profilesService)
                    .build();

            server.start();
            System.out.println("Server started on port" + server.getPort());

            server.awaitTermination();
        } catch (Exception e) {
            System.err.println("Error starting server" + e.getMessage());
            e.printStackTrace();
        }
    }
}
