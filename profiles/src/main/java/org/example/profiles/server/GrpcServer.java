package org.example.profiles.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.example.profiles.service.ProfileCommandService;
import org.example.profiles.service.ProfileDiscSync;
import org.example.profiles.service.ProfilesService;
import org.springframework.stereotype.Component;

@Component
public class GrpcServer {
    private final ProfilesService profilesService;
    private final ProfileCommandService profileCommandService;
    private final ProfileDiscSync profileDiscSync;

    public GrpcServer(ProfilesService profilesService, ProfileCommandService profileCommandService, ProfileDiscSync profileDiscSync) {
        this.profilesService = profilesService;
        this.profileCommandService = profileCommandService;
        this.profileDiscSync = profileDiscSync;
    }

    public void start() {
        try {
            Server server = ServerBuilder.
                    forPort(9090)
                    .addService(profilesService)
                    .addService(profileCommandService)
                    .addService(profileDiscSync)
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
