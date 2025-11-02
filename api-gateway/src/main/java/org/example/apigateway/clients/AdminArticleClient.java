package org.example.apigateway.clients;

import com.example.generated.*;
import io.grpc.Channel;
import org.example.apigateway.Client;

@Client(host = "${article.server.host}",
        port = "${article.server.port}"
)
public class AdminArticleClient {

    private static AdminArticleServiceGrpc.AdminArticleServiceBlockingStub stub;

    public static ListPublicationsResponse adminListPublications(long ownerId, int typeId, int disciplineId, int cycleId, int page,
    int size, String sortBy, String sortDir) {

        ListAdminPublicationRequest request = ListAdminPublicationRequest.newBuilder()
                .setOwnerId(ownerId)
                .setTypeId(typeId)
                .setCycleId(cycleId)
                .setDisciplineId(disciplineId)
                .setPage(page)
                .setSize(size)
                .setSortBy(sortBy)
                .setSortDir(sortDir)
                .build();

        return stub.adminListPublications(request);
    }

    public static PublicationView adminGetPublication(long publicationId , long ownerId) {
        GetPublicationRequest request = GetPublicationRequest.newBuilder()
                .setId(publicationId)
                .setUserId(ownerId)
                .build();

        return stub.adminGetPublication(request);
    }


    //discipline
    public static AdminListDisciplinesResponse adminListDisciplines(int page, int size, String sortDir) {

        AdminListDisciplinesRequest request = AdminListDisciplinesRequest.newBuilder()
                .setPage(page)
                .setSize(size)
                .setSortDir(sortDir)
                .build();

        return stub.adminListDisciplines(request);
    }

    public static RefItem adminCreateDiscipline(String disciplineName) {
        CreateDisciplineRequest request = CreateDisciplineRequest.newBuilder()
                .setDisciplineName(disciplineName)
                .build();

        return stub.adminCreateDiscipline(request);
    }

    public static RefItem adminUpdateDiscipline(long disciplineId, String disciplineName) {
        UpdateDisciplineRequest request = UpdateDisciplineRequest.newBuilder()
                .setId(disciplineId)
                .setDisciplineName(disciplineName)
                .build();

        return stub.adminUpdateDiscipline(request);
    }

    public static ApiResponse adminDeleteDiscipline(long disciplineId) {
        DeleteDisciplineRequest request = DeleteDisciplineRequest.newBuilder()
                .setId(disciplineId)
                .build();

        return stub.adminDeleteDiscipline(request);
    }


    //types
    public static AdminListTypesResponse adminListPublicationTypes(int page, int size, String sortDir) {
        AdminListTypesRequest request = AdminListTypesRequest.newBuilder()
                .setPage(page)
                .setSize(size)
                .setSortDir(sortDir)
                .build();

        return stub.adminListPublicationTypes(request);
    }

    public static RefItem adminCreatePublicationType(String typeName) {
        CreateTypeRequest request = CreateTypeRequest.newBuilder()
                .setName(typeName)
                .build();

        return stub.adminCreatePublicationType(request);
    }

    public static RefItem adminUpdatePublicationType(long publicationId, String typeName) {
        UpdateTypeRequest request = UpdateTypeRequest.newBuilder()
                .setId(publicationId)
                .setName(typeName)
                .build();

        return stub.adminUpdatePublicationType(request);
    }

    public static ApiResponse adminDeletePublicationType(long publicationId) {
        DeleteTypeRequest request = DeleteTypeRequest.newBuilder()
                .setId(publicationId)
                .build();

        return stub.adminDeletePublicationType(request);
    }


    //cycle
    public static AdminListCyclesResponse adminListEvalCycles(int page, int size, String sortDir) {
        AdminListCyclesRequest request = AdminListCyclesRequest.newBuilder()
                .setPage(page)
                .setSize(size)
                .setSortDir(sortDir)
                .build();

        return stub.adminListEvalCycles(request);
    }

    public static CycleItem adminCreateCycle(String cycleName) {
        CreateCycleRequest request = CreateCycleRequest.newBuilder()
                .setName(cycleName)
                .build();

        return stub.adminCreateEvalCycle(request);
    }

    public static CycleItem adminUpdateCycle(long cycleId, String cycleName) {
        UpdateCycleRequest request = UpdateCycleRequest.newBuilder()
                .setId(cycleId)
                .setName(cycleName)
                .build();

        return stub.adminUpdateEvalCycle(request);
    }

    public ApiResponse adminDeleteCycle(long cycleId) {
        DeleteCycleRequest request = DeleteCycleRequest.newBuilder()
                .setId(cycleId)
                .build();

        return stub.adminDeleteEvalCycle(request);
    }

    public static void init(Channel channel){
        stub = AdminArticleServiceGrpc.newBlockingStub(channel);
    }

}
