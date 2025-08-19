package org.example.auth.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.example.auth.helpers.TokenHelper;
import org.example.auth.services.AuthService;
import org.springframework.stereotype.Component;

@Component
public class GrpcServer {
    private final AuthService authService;

    public GrpcServer(AuthService authService) {
        this.authService = authService;
    }

    public void start() {
        try {
            Server server = ServerBuilder.
                    forPort(9090).
                    addService(authService)
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
