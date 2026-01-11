package org.example.apigateway.clients;

import com.example.generated.*;
import com.google.protobuf.FieldMask;
import io.grpc.Channel;
import org.example.apigateway.Client;
import org.springframework.stereotype.Component;

@Client(host = "${article.server.host}",
        port = "${article.server.port}"
)
@Component
public class AdminArticleClient {

    private  AdminArticleServiceGrpc.AdminArticleServiceBlockingStub stub;

    public  ListPublicationsResponse adminListPublications(Long ownerId, Long typeId, Long disciplineId, Long cycleId, int page,
    int size, String sortBy, String sortDir,String title) {

        ListAdminPublicationRequest.Builder request = ListAdminPublicationRequest.newBuilder()
                .setPage(page)
                .setSize(size)
                .setSortBy(sortBy)
                .setSortDir(sortDir);


        if (ownerId != null) request.setOwnerId(ownerId);
        if (typeId != null) request.setTypeId(typeId);
        if (disciplineId != null) request.setDisciplineId(disciplineId);
        if (cycleId != null) request.setCycleId(cycleId);
        if(title != null) request.setTitle(title);


        return stub.adminListPublications(request.build());
    }

    public  PublicationView adminGetPublication(long publicationId , long ownerId) {
        GetPublicationRequest request = GetPublicationRequest.newBuilder()
                .setId(publicationId)
                .setUserId(ownerId)
                .build();

        return stub.adminGetPublication(request);
    }


    //discipline
    public  AdminListDisciplinesResponse adminListDisciplines(int page, int size, String sortDir) {

        AdminListDisciplinesRequest request = AdminListDisciplinesRequest.newBuilder()
                .setPage(page)
                .setSize(size)
                .setSortDir(sortDir)
                .build();

        return stub.adminListDisciplines(request);
    }

    public  RefItem adminCreateDiscipline(String disciplineName) {
        CreateDisciplineRequest request = CreateDisciplineRequest.newBuilder()
                .setDisciplineName(disciplineName)
                .build();

        return stub.adminCreateDiscipline(request);
    }

    public  RefItem adminUpdateDiscipline(long disciplineId, String disciplineName) {
        UpdateDisciplineRequest request = UpdateDisciplineRequest.newBuilder()
                .setId(disciplineId)
                .setDisciplineName(disciplineName)
                .build();

        return stub.adminUpdateDiscipline(request);
    }

    public  ApiResponse adminDeleteDiscipline(long disciplineId) {
        DeleteDisciplineRequest request = DeleteDisciplineRequest.newBuilder()
                .setId(disciplineId)
                .build();

        return stub.adminDeleteDiscipline(request);
    }


    //types
    public  AdminListTypesResponse adminListPublicationTypes(int page, int size, String sortDir) {
        AdminListTypesRequest request = AdminListTypesRequest.newBuilder()
                .setPage(page)
                .setSize(size)
                .setSortDir(sortDir)
                .build();

        return stub.adminListPublicationTypes(request);
    }

    public  RefItem adminCreatePublicationType(String typeName) {
        CreateTypeRequest request = CreateTypeRequest.newBuilder()
                .setName(typeName)
                .build();

        return stub.adminCreatePublicationType(request);
    }

    public  RefItem adminUpdatePublicationType(long publicationId, String typeName) {
        UpdateTypeRequest request = UpdateTypeRequest.newBuilder()
                .setId(publicationId)
                .setName(typeName)
                .build();

        return stub.adminUpdatePublicationType(request);
    }

    public  ApiResponse adminDeletePublicationType(long publicationId) {
        DeleteTypeRequest request = DeleteTypeRequest.newBuilder()
                .setId(publicationId)
                .build();

        return stub.adminDeletePublicationType(request);
    }


    //cycle
    public  AdminListCyclesResponse adminListEvalCycles(int page, int size, String sortDir) {
        AdminListCyclesRequest request = AdminListCyclesRequest.newBuilder()
                .setPage(page)
                .setSize(size)
                .setSortDir(sortDir)
                .build();

        return stub.adminListEvalCycles(request);
    }

    public  CycleItem adminCreateCycle(String cycleName, int yearFrom, int yearTo, boolean isActive, int activeYear) {
        CreateCycleRequest request = CreateCycleRequest.newBuilder()
                .setName(cycleName)
                .setYearFrom(yearFrom)
                .setYearTo(yearTo)
                .setIsActive(isActive)
                .setActiveYear(activeYear)
                .build();

        return stub.adminCreateEvalCycle(request);
    }

    public  CycleItem adminUpdateCycle(long cycleId, String cycleName , Integer yearFrom, Integer yearTo, Boolean isActive, Long meinVersionId, Long MeinMonoVersionId, Integer activeYear) {

        UpdateCycleRequest.Builder req = UpdateCycleRequest.newBuilder()
                .setId(cycleId);

        FieldMask.Builder mask = FieldMask.newBuilder();

        if(cycleName != null) {
            req.setName(cycleName);
            mask.addPaths("name");
        }
        if(yearFrom != null){
            req.setYearFrom(yearFrom);
            mask.addPaths("yearFrom");
        }
        if(yearTo != null){
            req.setYearTo(yearTo);
            mask.addPaths("yearTo");
        }
        if(isActive != null){
            req.setIsActive(isActive);
            mask.addPaths("isActive");
        }
        if(meinVersionId != null){
            req.setMeinVersionId(meinVersionId);
            mask.addPaths("meinVersionId");
        }
        if(MeinMonoVersionId != null){
            req.setMonoVersionId(MeinMonoVersionId);
            mask.addPaths("monoVersionId");
        }

        if(activeYear != null){
            req.setActiveYear(activeYear);
            mask.addPaths("activeYear");
        }

        req.setUpdateMask(mask.build());

        return stub.adminUpdateEvalCycle(req.build());
    }

    public  ApiResponse adminDeleteCycle(long cycleId) {
        DeleteCycleRequest request = DeleteCycleRequest.newBuilder()
                .setId(cycleId)
                .build();

        return stub.adminDeleteEvalCycle(request);
    }

    public  void init(Channel channel){
        stub = AdminArticleServiceGrpc.newBlockingStub(channel);
    }

}
