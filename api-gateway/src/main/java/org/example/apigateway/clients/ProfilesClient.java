package org.example.apigateway.clients;

import com.example.generated.*;
import com.google.protobuf.Empty;
import io.grpc.Channel;
import org.example.apigateway.Client;
import org.example.apigateway.requests.profiles.UpdateProfileRequest;
import org.springframework.stereotype.Component;

import static org.example.apigateway.mappers.ProfileMapper.toGrpc;

@Client(
        host = "${profiles.server.host}",
        port = "${profiles.server.port}"
)
@Component
public class ProfilesClient {

    private ProfilesServiceGrpc.ProfilesServiceBlockingStub stub;

    public GetProfileResponse getProfile(Long userId) {
        GetProfileRequest request = GetProfileRequest.newBuilder().setUserId(userId).build();

        return stub.getMyProfile(request);
    }

    public UpdateMyProfileResponse updateMyProfile(UpdateProfileRequest request, Long userId) {
        UpdateMyProfileRequest grpcReq = toGrpc(request, userId);

        return stub.updateMyProfile(grpcReq);
    }

    public GetProfileResponse seeSomeoneProfile(Long userId) {
        SeeSomeoneProfileRequest request = SeeSomeoneProfileRequest.newBuilder().setUserId(userId).build();
        return stub.seeSomeoneProfile(request);
    }

    public ListWorkerDisciplinesResponse listWorkerDisciplines(Long userId) {
        ListWorkerDisciplinesRequest request = ListWorkerDisciplinesRequest.newBuilder().setUserId(userId).build();
        return stub.listWorkerDisciplines(request);
    }


    public ListWorkerDisciplinesResponse addWorkerDiscipline(Long userId, Long disciplineId) {
        AddWorkerDisciplineRequest req = AddWorkerDisciplineRequest.newBuilder()
                .setUserId(userId)
                .setDisciplineId(disciplineId)
                .build();
        return stub.addWorkerDiscipline(req);
    }

    public ListWorkerDisciplinesResponse removeWorkerDiscipline(Long userId, Long disciplineId) {
        RemoveWorkerDisciplineRequest req = RemoveWorkerDisciplineRequest.newBuilder()
                .setUserId(userId)
                .setDisciplineId(disciplineId)
                .build();
        return stub.removeWorkerDiscipline(req);
    }

    public GetOrCreateStatementResponse getOrCreateStatement(Long userId, Long disciplineId, int evalYear) {
        GetOrCreateStatementRequest req = GetOrCreateStatementRequest.newBuilder()
                .setUserId(userId)
                .setDisciplineId(disciplineId)
                .setEvalYear(evalYear)
                .build();
        return stub.getOrCreateStatement(req);
    }

    public AdminInitStatementsForYearResponse adminInitStatementsForYear(int evalYear) {
        AdminInitStatementsForYearRequest req = AdminInitStatementsForYearRequest.newBuilder()
                .setEvalYear(evalYear)
                .build();
        return stub.adminInitStatementsForYear(req);
    }

    public ListAllProfilesResponse allProfiles(String fullName, Integer page ,  Integer size , String sortBy, String sortDir) {
        ListAllProfilesRequest request = ListAllProfilesRequest.newBuilder()
                .setFullname(fullName)
                .setPage(page)
                .setSize(size)
                .setSortBy(sortBy)
                .setSortDir(sortDir)
                .build();
        return stub.listAllProfiles(request);
    }

    public ListDisciplineResponse listDiscipline() {
        return stub.listDisciplines(Empty.getDefaultInstance());
    }

    public void init(Channel channel) {
        stub = ProfilesServiceGrpc.newBlockingStub(channel);
    }

}
