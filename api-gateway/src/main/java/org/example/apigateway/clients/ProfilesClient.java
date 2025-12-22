package org.example.apigateway.clients;

import com.example.generated.*;
import io.grpc.Channel;
import org.example.apigateway.Client;
import org.example.apigateway.requests.profiles.UpdateProfileRequest;

import static org.example.apigateway.mappers.ProfileMapper.toGrpc;

@Client(
        host = "${profiles.server.host}",
        port = "${profiles.server.port}"
)
public class ProfilesClient {

    private static ProfilesServiceGrpc.ProfilesServiceBlockingStub stub;

    public static GetProfileResponse getProfile(Long userId) {
        GetProfileRequest request = GetProfileRequest.newBuilder().setUserId(userId).build();

        return stub.getMyProfile(request);
    }

    public static UpdateMyProfileResponse updateMyProfile(UpdateProfileRequest request, Long userId) {
        UpdateMyProfileRequest grpcReq = toGrpc(request, userId);

        return stub.updateMyProfile(grpcReq);
    }

    public static GetProfileResponse seeSomeoneProfile(Long userId) {
        SeeSomeoneProfileRequest request = SeeSomeoneProfileRequest.newBuilder().setUserId(userId).build();
        return stub.seeSomeoneProfile(request);
    }

    public static ListWorkerDisciplinesResponse listWorkerDisciplines(Long userId) {
        ListWorkerDisciplinesRequest request = ListWorkerDisciplinesRequest.newBuilder().setUserId(userId).build();
        return stub.listWorkerDisciplines(request);
    }


    public static ListWorkerDisciplinesResponse addWorkerDiscipline(Long userId, Long disciplineId) {
        AddWorkerDisciplineRequest req = AddWorkerDisciplineRequest.newBuilder()
                .setUserId(userId)
                .setDisciplineId(disciplineId)
                .build();
        return stub.addWorkerDiscipline(req);
    }

    public static ListWorkerDisciplinesResponse removeWorkerDiscipline(Long userId, Long disciplineId) {
        RemoveWorkerDisciplineRequest req = RemoveWorkerDisciplineRequest.newBuilder()
                .setUserId(userId)
                .setDisciplineId(disciplineId)
                .build();
        return stub.removeWorkerDiscipline(req);
    }

    public static GetOrCreateStatementResponse getOrCreateStatement(Long userId, Long disciplineId, int evalYear) {
        GetOrCreateStatementRequest req = GetOrCreateStatementRequest.newBuilder()
                .setUserId(userId)
                .setDisciplineId(disciplineId)
                .setEvalYear(evalYear)
                .build();
        return stub.getOrCreateStatement(req);
    }

    public static AdminInitStatementsForYearResponse adminInitStatementsForYear(int evalYear) {
        AdminInitStatementsForYearRequest req = AdminInitStatementsForYearRequest.newBuilder()
                .setEvalYear(evalYear)
                .build();
        return stub.adminInitStatementsForYear(req);
    }

    public static void init(Channel channel) {
        stub = ProfilesServiceGrpc.newBlockingStub(channel);
    }

}
