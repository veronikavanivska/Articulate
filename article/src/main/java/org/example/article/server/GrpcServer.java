package org.example.article.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.example.article.service.AricleService;
import org.springframework.stereotype.Component;

@Component
public class GrpcServer {
    private final AricleService meinImportService;

    public GrpcServer(AricleService meinImportService) {
        this.meinImportService = meinImportService;
    }

    public void start() {
        try {
            Server server = ServerBuilder.
                    forPort(9090)
                    .maxInboundMessageSize(32 * 1024 * 1024)     // client can receive up to 32 MB
                    .maxInboundMetadataSize(64 * 1024)
                    .addService(meinImportService)
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
