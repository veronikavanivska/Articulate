package org.example.apigateway.clients;

import com.example.generated.*;
import com.google.protobuf.ByteString;
import io.grpc.Channel;
import org.example.apigateway.Client;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Client(host = "${article.server.host}",
        port = "${article.server.port}"
)
@Component
public class ETLMonoClient {

    private ETLMonoServiceGrpc.ETLMonoServiceBlockingStub stub;

    public ImportMEiNReply importFile(MultipartFile file, String fileName, String label, long importedBy){

        ByteString fileBytes = null;
        try {
            fileBytes = ByteString.copyFrom(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ImportMEiNRequest request = ImportMEiNRequest.newBuilder()
                .setFile(fileBytes)
                .setFilename(fileName)
                .setLabel(label == null ? "" : label)
                .setImportedBy(importedBy)
                .build();

        return stub.importFile(request);
    }

    public AdminGetMeinMonoVersionResponse adminGetMeinMonoVersion(Long versionId){
        AdminGetMeinMonoVersionRequest request = AdminGetMeinMonoVersionRequest.newBuilder()
                .setId(versionId)
                .build();

        return stub.adminGetMeinMonoVersion(request);
    }

    public AdminGetMeinMonoPublisherResponse adminGetMeinMonoPublisher(Long publisherId){
        AdminGetMeinMonoPublisherRequest request = AdminGetMeinMonoPublisherRequest.newBuilder()
                .setId(publisherId)
                .build();

        return stub.adminGetMeinMonoPublisher(request);
    }

    public AdminListMeinMonoVersionsResponse adminListMeinMonoVersions(Integer page, Integer size, String sortDir) {
        AdminListMeinMonoVersionsRequest request = AdminListMeinMonoVersionsRequest.newBuilder()
                .setPage(page)
                .setSize(size)
                .setSortDir(sortDir)
                .build();

        return stub.adminListMeinMonoVersions(request);
    }

    public AdminListMeinMonoPublishersResponse adminListMeinMonoPublishers(Long versionId, Integer page, Integer size, String sortDir,String title){
        AdminListMeinMonoPublishersRequest request = AdminListMeinMonoPublishersRequest.newBuilder()
                .setVersionId(versionId)
                .setPage(page)
                .setSize(size)
                .setSortDir(sortDir)
                .setTitle(title)
                .build();

        return stub.adminListMeinMonoPublishers(request);
    }

    public DeleteMeinMonoVersionResponse deleteMeinMonoVersion(Long versionId){
        DeleteMeinMonoVersionRequest request = DeleteMeinMonoVersionRequest.newBuilder()
                .setId(versionId)
                .build();

        return stub.adminDeleteMeinMonoVersion(request);
    }

    public AdminRecalcMonoCycleScoresResponse adminRecalcMonoCycleScores(Long cycleId){
        AdminRecalcMonoCycleScoresRequest request = AdminRecalcMonoCycleScoresRequest.newBuilder()
                .setCycleId(cycleId)
                .build();

        return stub.adminRecalculateMonoCycleScores(request);
    }

    public void init(Channel channel) {
        stub = ETLMonoServiceGrpc.newBlockingStub(channel);
    }

}
