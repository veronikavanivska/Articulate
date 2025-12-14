package org.example.apigateway.clients;

import com.example.generated.*;
import com.google.protobuf.FieldMask;
import io.grpc.Channel;
import org.example.apigateway.Client;
import org.example.apigateway.requests.articles.Coauthors;

import java.util.ArrayList;
import java.util.List;

@Client(host = "${article.server.host}",
        port = "${article.server.port}"
)
public class WorkerMonoClient {
    private static WorkerMonographServiceGrpc.WorkerMonographServiceBlockingStub stub;

    public static MonographView createMonograph(long userId, long typeId, long disciplineId, String title, String doi, String isbn, String monograficPublisherTitle, int publicationYest, List<Coauthors> coauthors){
        List<CoauthorInput> coauthorInputs = new ArrayList<>();

        for(Coauthors coauthor: coauthors){
            CoauthorInput input = CoauthorInput.newBuilder()
                    .setUserId(coauthor.getUserId())
                    .setFullName(coauthor.getFullName())
                    .build();

            coauthorInputs.add(input);
        }

        CreateMonographRequest request = CreateMonographRequest.newBuilder()
                .setUserId(userId)
                .setTypeId(typeId)
                .setDisciplineId(disciplineId)
                .setTitle(title)
                .setDoi(doi)
                .setIsbn(isbn)
                .setMonograficPublisherTitle(monograficPublisherTitle)
                .setPublicationYear(publicationYest)
                .addAllInput(coauthorInputs)
                .build();

        return stub.createMonograph(request);
    }


    public static ChapterView createChapter(long userId, long typeId, long disciplineId, String monographChapterTitle, String monographTitle, String monographPublisherTitle, String doi, String isbn, int publicationYear, List<Coauthors> coauthors){
        List<CoauthorInput> coauthorInputs = new ArrayList<>();

        for(Coauthors coauthor: coauthors){
            CoauthorInput input = CoauthorInput.newBuilder()
                    .setUserId(coauthor.getUserId())
                    .setFullName(coauthor.getFullName())
                    .build();

            coauthorInputs.add(input);
        }

        CreateChapterRequest request = CreateChapterRequest.newBuilder()
                .setUserId(userId)
                .setTypeId(typeId)
                .setDisciplineId(disciplineId)
                .setMonographChapterTitle(monographChapterTitle)
                .setMonographTitle(monographTitle)
                .setMonographPublisherTitle(monographPublisherTitle)
                .setDoi(doi)
                .setIsbn(isbn)
                .setPublicationYear(publicationYear)
                .addAllInput(coauthorInputs)
                .build();

        return stub.createChapter(request);
    }

    public static MonographView getMonograph(long id, long userId){
        GetMonographRequest request = GetMonographRequest.newBuilder()
                .setId(id)
                .setUserId(userId)
                .build();

        return stub.getMonograph(request);
    }

    public static ChapterView getChapter(long id, long userId){
        GetChapterRequest request = GetChapterRequest.newBuilder()
                .setId(id)
                .setUserId(userId)
                .build();

        return stub.getChapter(request);
    }

    public static MonographView updateMonograph(long id , long userId, Long typeId, Long disciplineId, String title, String doi, String isbn, String monographPublisherTitle, Integer publicationYear, List<Coauthors> coauthors){
        UpdateMonographRequest.Builder req = UpdateMonographRequest.newBuilder()
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
        if (isbn != null) {
            req.setIsbn(isbn);
            mask.addPaths("isbn");
        }

        if (publicationYear != null) {
            req.setPublicationYear(publicationYear);
            mask.addPaths("publicationYear");
        }
        if (coauthors != null) {
            List<CoauthorInput> coauthorInputs = new ArrayList<>();

            for(Coauthors coauthor: coauthors){
                CoauthorInput input = CoauthorInput.newBuilder()
                        .setUserId(coauthor.getUserId())
                        .setFullName(coauthor.getFullName())
                        .build();

                coauthorInputs.add(input);
            }

            req.addAllReplaceCoauthors(coauthorInputs);
            mask.addPaths("replaceCoauthors");
        }

        if(monographPublisherTitle != null){
            req.setMonographPublisherTitle(monographPublisherTitle);
            mask.addPaths("monographPublisherTitle");
        }

        if (mask.getPathsCount() == 0) {
            throw new IllegalArgumentException("No fields to update");
        }

        req.setUpdateMask(mask.build());

        return stub.updateMonograph(req.build());
    }


    public static ChapterView updateChapter(long id, long userId, Long typeId, Long disciplineId, String monographChapterTitle, String monographTitle, String monographPublisherTitle, String doi, String isbn, Integer publicationYear, List<Coauthors> coauthors)
    {
        UpdateChapterRequest.Builder req = UpdateChapterRequest.newBuilder()
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
        if (monographChapterTitle != null) {
            req.setMonograficChapterTitle(monographChapterTitle);
            mask.addPaths("monograficChapterTitle");
        }
        if(monographTitle != null){
            req.setMonograficTitle(monographTitle);
            mask.addPaths("monographTitle");
        }

        if (doi != null) {
            req.setDoi(doi);
            mask.addPaths("doi");
        }
        if (isbn != null) {
            req.setIsbn(isbn);
            mask.addPaths("isbn");
        }

        if (publicationYear != null) {
            req.setPublicationYear(publicationYear);
            mask.addPaths("publicationYear");
        }
        if (coauthors != null) {
            List<CoauthorInput> coauthorInputs = new ArrayList<>();

            for(Coauthors coauthor: coauthors){
                CoauthorInput input = CoauthorInput.newBuilder()
                        .setUserId(coauthor.getUserId())
                        .setFullName(coauthor.getFullName())
                        .build();

                coauthorInputs.add(input);
            }

            req.addAllReplaceCoauthors(coauthorInputs);
            mask.addPaths("replaceCoauthors");
        }

        if(monographPublisherTitle != null){
            req.setMonographPublisher(monographPublisherTitle);
            mask.addPaths("monographPublisher");
        }

        if (mask.getPathsCount() == 0) {
            throw new IllegalArgumentException("No fields to update");
        }

        req.setUpdateMask(mask.build());

        return stub.updateChapter(req.build());
    }

    public static ListMonographsResponse listMonographs(long ownerId, int typeId, int disciplineId, int cycleId, int page,
                                                        int size, String sortBy, String sortDir) {
        ListMonographsRequest request = ListMonographsRequest.newBuilder()
                .setUserId(ownerId)
                .setTypeId(typeId)
                .setCycleId(cycleId)
                .setDisciplineId(disciplineId)
                .setPage(page)
                .setSize(size)
                .setSortBy(sortBy)
                .setSortDir(sortDir)
                .build();

        return stub.listMyMonographs(request);
    }

    public static ListChaptersResponse listChapters(long ownerId, int typeId, int disciplineId, int cycleId, int page, int size, String sortBy, String sortDir) {
        ListChaptersRequest request = ListChaptersRequest.newBuilder()
                .setUserId(ownerId)
                .setTypeId(typeId)
                .setCycleId(cycleId)
                .setDisciplineId(disciplineId)
                .setPage(page)
                .setSize(size)
                .setSortBy(sortBy)
                .setSortDir(sortDir)
                .build();

        return stub.listMyChapters(request);
    }

    public static ApiResponse deleteMonograph(long id, long userId) {
        DeleteMonographRequest request = DeleteMonographRequest.newBuilder()
                .setId(id)
                .setUserId(userId)
                .build();

        return stub.deleteMonograph(request);
    }

    public static ApiResponse deleteChapter(long id, long userId) {
        DeleteChapterRequest request = DeleteChapterRequest.newBuilder()
                .setId(id)
                .setUserId(userId)
                .build();

        return stub.deleteChapter(request);
    }

    public static void init(Channel channel){
        stub = WorkerMonographServiceGrpc.newBlockingStub(channel);
    }
}
