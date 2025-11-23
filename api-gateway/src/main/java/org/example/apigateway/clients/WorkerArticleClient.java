package org.example.apigateway.clients;

import com.example.generated.*;

import com.google.protobuf.FieldMask;
import io.grpc.Channel;
import org.example.apigateway.Client;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Client(host = "${article.server.host}",
        port = "${article.server.port}"
)
public class WorkerArticleClient {
    private static WorkerArticleServiceGrpc.WorkerArticleServiceBlockingStub stub;


    public static PublicationView createPublication(long userId, long typeId, long disciplineId, String title, String doi,
                                             String issn, String eissn, String journalTitle, int publicationYear,
                                             List<String> coauthors){
        CreatePublicationRequest request = CreatePublicationRequest.newBuilder()
                .setUserId(userId)
                .setTypeId(typeId)
                .setTitle(title)
                .setDisciplineId(disciplineId)
                .setDoi(doi)
                .setPublicationYear(publicationYear)
                .setIssn(issn)
                .setEissn(eissn)
                .setJournalTitle(journalTitle)
                .addAllCoauthors(coauthors)
                .build();

        return stub.createPublication(request);
    }



    public static PublicationView getPublication(long userId, long publicationId){
        GetPublicationRequest request = GetPublicationRequest.newBuilder()
                .setId(publicationId)
                .setUserId(userId)
                .build();

        return stub.getPublication(request);
    }

    public static ListPublicationsResponse listMyPublications(long userId, long typeId, long disciplineId, long cycleId,
                                                       int page, int size, String sortBy, String sortDir){

        ListPublicationsRequest request = ListPublicationsRequest.newBuilder()
                .setUserId(userId)
                .setTypeId(typeId)
                .setDisciplineId(disciplineId)
                .setCycleId(cycleId)
                .setPage(page)
                .setSize(size)
                .setSortBy(sortBy)
                .setSortDir(sortDir)
                .build();

        return stub.listMyPublications(request);
    }

    public static PublicationView updatePublication(long id, long userId, Long typeId, Long disciplineId,
                                             String title, String doi, String issn, String eissn,
                                             String journalTitle, Integer publicationYear, List<String> replaceCoauthors){
        UpdatePublicationRequest.Builder req = UpdatePublicationRequest.newBuilder()
                .setId(id)
                .setUserId(userId);

        FieldMask.Builder mask = FieldMask.newBuilder();

        if (typeId != null) {
            req.setTypeId(typeId);
            mask.addPaths("typeId");
        }
        if (disciplineId != null) {
            req.setDisciplineId(disciplineId);
            mask.addPaths("disciplineId");
        }
        if (title != null) {
            req.setTitle(title);
            mask.addPaths("title");
        }
        if (doi != null) {
            req.setDoi(doi);
            mask.addPaths("doi");
        }
        if (issn != null) {
            req.setIssn(issn);
            mask.addPaths("issn");
        }
        if (eissn != null) {
            req.setEissn(eissn);
            mask.addPaths("eissn");
        }
        if (journalTitle != null) {
            req.setJournalTitle(journalTitle);
            mask.addPaths("journalTitle");
        }
        if (publicationYear != null) {
            req.setPublicationYear(publicationYear);
            mask.addPaths("publicationYear");
        }
        if (replaceCoauthors != null) {
            req.addAllReplaceCoauthors(replaceCoauthors);
            mask.addPaths("replaceCoauthors");
        }


        if (mask.getPathsCount() == 0) {
            throw new IllegalArgumentException("No fields to update");
        }

        req.setUpdateMask(mask.build());

        return stub.updatePublication(req.build());
    }

    public static ApiResponse deletePublication(long id, long userId){
        DeletePublicationRequest request = DeletePublicationRequest.newBuilder()
                .setId(id)
                .setUserId(userId)
                .build();

        return stub.deletePublication(request);
    }

    public static void init(Channel channel) {
        stub = WorkerArticleServiceGrpc.newBlockingStub(channel);
    }
}
