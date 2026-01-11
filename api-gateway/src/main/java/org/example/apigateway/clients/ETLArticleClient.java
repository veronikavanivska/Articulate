package org.example.apigateway.clients;

import com.example.generated.*;
import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import io.grpc.Channel;
import org.example.apigateway.Client;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Client(host = "${article.server.host}",
        port = "${article.server.port}"
)
@Component
public class ETLArticleClient {

    private ETLArticleServiceGrpc.ETLArticleServiceBlockingStub stub;

    public ImportMEiNReply importFile(MultipartFile file, String fileName, String label, long importedBy, boolean activateAfter){
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
                .setActivateAfter(activateAfter)
                .build();

        return stub.importFile(request);
    }

    public AdminListMeinVersionsResponse adminListMeinVersions(int page, int size, String sortDir ){
        AdminListMeinVersionsRequest request = AdminListMeinVersionsRequest.newBuilder()
                .setPage(page)
                .setSize(size)
                .setSortDir(sortDir)
                .build();

        return stub.adminListMeinVersions(request);
    }

    public AdminGetActiveMeinVersionResponse adminGetActiveMeinVersion(){
        return stub.adminGetActiveMeinVersion(Empty.getDefaultInstance());
    }

    public AdminGetMeinVersionResponse adminGetMeinVersion(long versionId){

        AdminGetMeinVersionRequest request = AdminGetMeinVersionRequest.newBuilder()
                .setVersionId(versionId)
                .build();

        return stub.adminGetMeinVersion(request);
    }

    public AdminListMeinJournalsResponse adminListMeinJournals(Long versionId, Integer page, Integer size, String sortDir,String title) {
        AdminListMeinJournalsRequest request = AdminListMeinJournalsRequest.newBuilder()
                .setVersionId(versionId)
                .setPage(page)
                .setSize(size)
                .setSortDir(sortDir)
                .setTitle(title)
                .build();

        return stub.adminListMeinJournals(request);
    }

    public AdminGetMeinJournalResponse adminGetMeinJournal(long versionId , long journalId){
        AdminGetMeinJournalRequest request = AdminGetMeinJournalRequest.newBuilder()
                .setVersionId(versionId)
                .setJournalId(journalId)
                .build();

        return stub.adminGetMeinJournal(request);
    }

    public ApiResponse adminActivateMeinVersion(long versionId){
        ActivateMeinVersionRequest request = ActivateMeinVersionRequest.newBuilder()
                .setVersionId(versionId)
                .build();

        return stub.adminActivateMeinVersion(request);
    }

    public ApiResponse adminDeactivateMeinVersion(long versionId){
        DeactivateMeinVersionRequest request = DeactivateMeinVersionRequest.newBuilder()
                .setVersionId(versionId)
                .build();

        return stub.adminDeactivateMeinVersion(request);
    }

    public DeleteMeinVersionResponse adminDeleteMeinVersion(long versionId){
        DeleteMeinVersionRequest request = DeleteMeinVersionRequest.newBuilder()
                .setVersionId(versionId)
                .build();

        return stub.adminDeleteMeinVersion(request);
    }

    public AdminRecalcCycleScoresResponse adminRecalcCycleScores(long cycleId){
        AdminRecalcCycleScoresRequest request = AdminRecalcCycleScoresRequest.newBuilder()
                .setCycleId(cycleId)
                .build();

        return stub.adminRecalculateCycleScores(request);
    }

    public GetJobStatusResponse getJobStatus(long jobId){
        GetJobStatusRequest request = GetJobStatusRequest.newBuilder()
                .setJobId(jobId)
                .build();

        return stub.adminGetJobStats(request);
    }

    public void init(Channel channel) {
        stub = ETLArticleServiceGrpc.newBlockingStub(channel);
    }


}
