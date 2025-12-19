package org.example.slots.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.example.slots.service.SlotServiceImpl;
import org.springframework.stereotype.Component;

@Component
public class GrpcServer {
    private final SlotServiceImpl slotService;

    public GrpcServer(SlotServiceImpl slotService) {
        this.slotService = slotService;
    }
    public void start() {
        try {
            Server server = ServerBuilder.
                    forPort(9090)
                    .addService(slotService)
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
