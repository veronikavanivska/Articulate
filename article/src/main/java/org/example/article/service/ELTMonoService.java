package org.example.article.service;

import com.example.generated.ETLMonoServiceGrpc;
import com.example.generated.ImportMEiNReply;
import com.example.generated.ImportMEiNRequest;
import io.grpc.stub.StreamObserver;
import org.example.article.ETL.MonoETLService;
import org.springframework.stereotype.Service;

@Service
public class ELTMonoService extends ETLMonoServiceGrpc.ETLMonoServiceImplBase {

    private final MonoETLService monoETLService;

    public ELTMonoService(MonoETLService monoETLService) {
        this.monoETLService = monoETLService;
    }

    @Override
    public void importFile(ImportMEiNRequest request, StreamObserver<ImportMEiNReply> responseObserver) {

        try {
            byte[] bytes = request.getFile().toByteArray();
            String filename = request.getFilename();

            Long versionId = monoETLService.importPDF(
                    bytes,
                    filename,
                    request.getLabel(),
                    request.getImportedBy()
            );

            boolean already = (versionId == null);
            long id = already ? -1L : versionId;
            ImportMEiNReply resp = ImportMEiNReply.newBuilder()
                    .setVersionId(id)
                    .setAlreadyImported(already)
                    .build();

            responseObserver.onNext(resp);
            responseObserver.onCompleted();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
