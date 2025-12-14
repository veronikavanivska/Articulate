package org.example.slots.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.stereotype.Component;

@Component
public class GrpcServer {

    public void start() {
        try {
            Server server = ServerBuilder.
                    forPort(9090)
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
