package org.example.profiles.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.example.profiles.service.ProfileCommandService;
import org.example.profiles.service.ProfilesService;
import org.springframework.stereotype.Component;

@Component
public class GrpcServer {
    private final ProfilesService profilesService;
    private final ProfileCommandService profileCommandService;
    public GrpcServer(ProfilesService profilesService, ProfileCommandService profileCommandService) {
        this.profilesService = profilesService;
        this.profileCommandService = profileCommandService;
    }

    public void start() {
        try {
            Server server = ServerBuilder.
                    forPort(9090)
                    .addService(profilesService)
                    .addService(profileCommandService)
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
