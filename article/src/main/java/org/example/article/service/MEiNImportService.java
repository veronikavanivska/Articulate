package org.example.article.service;

import com.example.generated.ImportMEiNReply;
import com.example.generated.ImportMEiNRequest;
import com.example.generated.MEiNImportGrpc;
import io.grpc.stub.StreamObserver;
import org.example.article.ETL.ETLService;
import org.springframework.stereotype.Service;

@Service
public class MEiNImportService extends MEiNImportGrpc.MEiNImportImplBase {

    final private ETLService ETLService;

    public MEiNImportService(ETLService ETLService) {
        this.ETLService = ETLService;
    }
    @Override
    public void importFile(ImportMEiNRequest request, StreamObserver<ImportMEiNReply> responseObserver) {

        try {
            byte[] bytes = request.getFile().toByteArray();
            String filename = request.getFilename();

            Long versionId = ETLService.importExcel(
                    bytes,
                    filename,
                    request.getLabel(),
                    request.getImportedBy(),
                    request.getActivateAfter()
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
