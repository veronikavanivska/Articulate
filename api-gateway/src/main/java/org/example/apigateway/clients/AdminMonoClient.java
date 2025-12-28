package org.example.apigateway.clients;

import com.example.generated.*;
import io.grpc.Channel;
import org.example.apigateway.Client;

@Client(host = "${article.server.host}",
        port = "${article.server.port}"
)
public class AdminMonoClient {
    private static AdminMonographServiceGrpc.AdminMonographServiceBlockingStub stub;

    public static ListMonographsResponse listMonographs(Long ownerId, Long typeId, Long disciplineId, Long cycleId, int page,
                                                        int size, String sortBy, String sortDir, String title) {
        ListAdminMonographsRequest.Builder request = ListAdminMonographsRequest.newBuilder()
                .setPage(page)
                .setSize(size)
                .setSortBy(sortBy)
                .setSortDir(sortDir);

        if (ownerId != null) request.setOwnerId(ownerId);
        if (typeId != null) request.setTypeId(typeId);
        if (disciplineId != null) request.setDisciplineId(disciplineId);
        if (cycleId != null) request.setCycleId(cycleId);
        if (title != null) request.setTitle(title);

        return stub.adminListMonographs(request.build());
    }

    public static ListChaptersResponse listChapters(Long ownerId, Long typeId, Long disciplineId, Long cycleId, Integer page,
                                                    int size, String sortBy, String sortDir, String title) {
        ListAdminChaptersRequest.Builder request = ListAdminChaptersRequest.newBuilder()
                               .setPage(page)
                .setSize(size)
                .setSortBy(sortBy)
                .setSortDir(sortDir);

        if (ownerId != null) request.setOwnerId(ownerId);
        if (typeId != null) request.setTypeId(typeId);
        if (disciplineId != null) request.setDisciplineId(disciplineId);
        if (cycleId != null) request.setCycleId(cycleId);
        if (title != null) request.setTitle(title);

        return stub.adminListChapters(request.build());
    }

    public static MonographView getMonograph(  long id , long userId){
        GetMonographRequest request = GetMonographRequest.newBuilder()
                .setId(id)
                .setUserId(userId)
                .build();

        return stub.adminGetMonograph(request);

    }

    public static ChapterView getChapter(  long id , long userId){
        GetChapterRequest request = GetChapterRequest.newBuilder()
                .setId(id)
                .setUserId(userId)
                .build();

        return stub.adminGetChapter(request);
    }

    public static void init(Channel channel){
        stub = AdminMonographServiceGrpc.newBlockingStub(channel);
    }

}
