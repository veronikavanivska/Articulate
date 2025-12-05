package org.example.article.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.example.article.ETL.MonoETLService;
import org.example.article.service.*;
import org.springframework.stereotype.Component;

@Component
public class GrpcServer {
    private final ArticleService meinImportService;
    private final ETLArticleService etlArticleService;
    private final AdminArticleService adminArticleService;
    private final WorkerArticleService workerArticleService;
    private final ELTMonoService monoETLService;
    private final WorkerMonoService workerMonoService;

    public GrpcServer(ArticleService meinImportService,WorkerMonoService workerMonoService, ETLArticleService etlArticleService, AdminArticleService adminArticleService, WorkerArticleService workerArticleService, ELTMonoService monoETLService) {
        this.adminArticleService = adminArticleService;
        this.etlArticleService = etlArticleService;
        this.workerArticleService = workerArticleService;
        this.meinImportService = meinImportService;
        this.monoETLService = monoETLService;
        this.workerMonoService = workerMonoService;
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
                    .addService(monoETLService)
                    .addService(workerMonoService)
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
