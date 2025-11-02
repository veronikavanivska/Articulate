package org.example.article.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.example.article.service.AdminArticleService;
import org.example.article.service.ArticleService;
import org.example.article.service.ETLArticleService;
import org.example.article.service.WorkerArticleService;
import org.springframework.stereotype.Component;

@Component
public class GrpcServer {
    private final ArticleService meinImportService;
    private final ETLArticleService etlArticleService;
    private final AdminArticleService adminArticleService;
    private final WorkerArticleService workerArticleService;

    public GrpcServer(ArticleService meinImportService, ETLArticleService etlArticleService, AdminArticleService adminArticleService, WorkerArticleService workerArticleService) {
        this.adminArticleService = adminArticleService;
        this.etlArticleService = etlArticleService;
        this.workerArticleService = workerArticleService;
        this.meinImportService = meinImportService;
    }

    public void start() {
        try {
            Server server = ServerBuilder.
                    forPort(9090)
                    .maxInboundMessageSize(32 * 1024 * 1024)     // client can receive up to 32 MB
                    .maxInboundMetadataSize(64 * 1024)
                    .addService(meinImportService)
                    .addService(etlArticleService)
                    .addService(adminArticleService)
                    .addService(workerArticleService)
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
