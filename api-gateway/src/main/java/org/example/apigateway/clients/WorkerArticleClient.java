package org.example.apigateway.clients;

import com.example.generated.*;

import com.google.protobuf.FieldMask;
import io.grpc.Channel;
import org.example.apigateway.Client;
import org.example.apigateway.requests.articles.Coauthors;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;

@Client(host = "${article.server.host}",
        port = "${article.server.port}"
)
public class WorkerArticleClient {
    private static WorkerArticleServiceGrpc.WorkerArticleServiceBlockingStub stub;


    public static PublicationView createPublication(long userId, long typeId, long disciplineId, String title, String doi,
                                             String issn, String eissn, String journalTitle, int publicationYear,
                                             List<Coauthors> coauthors){

        List<CoauthorInput> coauthorInputs = new ArrayList<>();

        for(Coauthors coauthor: coauthors){
            CoauthorInput input = CoauthorInput.newBuilder()
                    .setUserId(coauthor.getUserId())
                    .setFullName(coauthor.getFullName())
                    .build();

            coauthorInputs.add(input);
        }


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
                .addAllCoauthors(coauthorInputs)
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

    public static ListPublicationsResponse listMyPublications(Long userId, Long typeId, Long disciplineId, Long cycleId,
                                                       int page, int size, String sortBy, String sortDir){

        ListPublicationsRequest.Builder request = ListPublicationsRequest.newBuilder()
                .setPage(page)
                .setSize(size)
                .setSortBy(sortBy)
                .setSortDir(sortDir);

        if (userId != null) request.setUserId(userId);
        if (typeId != null) request.setTypeId(typeId);
        if (disciplineId != null) request.setDisciplineId(disciplineId);
        if (cycleId != null) request.setCycleId(cycleId);

        return stub.listMyPublications(request.build());
    }

    public static PublicationView updatePublication(long id, long userId, Long typeId, Long disciplineId,
                                             String title, String doi, String issn, String eissn,
                                             String journalTitle, Integer publicationYear, List<Coauthors> replaceCoauthors){


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
            List<CoauthorInput> coauthorInputs = new ArrayList<>();

            for(Coauthors coauthor: replaceCoauthors){
                CoauthorInput input = CoauthorInput.newBuilder()
                        .setUserId(coauthor.getUserId())
                        .setFullName(coauthor.getFullName())
                        .build();

                coauthorInputs.add(input);
            }

            req.addAllReplaceCoauthors(coauthorInputs);
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
