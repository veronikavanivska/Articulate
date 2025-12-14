package org.example.apigateway.clients;

import com.example.generated.*;
import io.grpc.Channel;
import org.example.apigateway.Client;

@Client(host = "${article.server.host}",
        port = "${article.server.port}"
)
public class AdminMonoClient {
    private static AdminMonographServiceGrpc.AdminMonographServiceBlockingStub stub;

    public static ListMonographsResponse listMonographs(long ownerId, int typeId, int disciplineId, int cycleId, int page,
                                                        int size, String sortBy, String sortDir) {
        ListAdminMonographsRequest request = ListAdminMonographsRequest.newBuilder()
                .setOwnerId(ownerId)
                .setTypeId(typeId)
                .setCycleId(cycleId)
                .setDisciplineId(disciplineId)
                .setPage(page)
                .setSize(size)
                .setSortBy(sortBy)
                .setSortDir(sortDir)
                .build();

        return stub.adminListMonographs(request);
    }

    public static ListChaptersResponse listChapters(long ownerId, int typeId, int disciplineId, int cycleId, int page,
                                                    int size, String sortBy, String sortDir){
        ListAdminChaptersRequest request = ListAdminChaptersRequest.newBuilder()
                .setOwnerId(ownerId)
                .setTypeId(typeId)
                .setCycleId(cycleId)
                .setDisciplineId(disciplineId)
                .setPage(page)
                .setSize(size)
                .setSortBy(sortBy)
                .setSortDir(sortDir)
                .build();

        return stub.adminListChapters(request);
    }

    public MonographView getMonograph(  long id , long userId){
        GetMonographRequest request = GetMonographRequest.newBuilder()
                .setId(id)
                .setUserId(userId)
                .build();

        return stub.adminGetMonograph(request);

    }

    public ChapterView getChapter(  long id , long userId){
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
