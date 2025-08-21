package org.example.profiles.service;

import com.example.generated.*;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.example.profiles.entities.ProfileUser;
import org.example.profiles.repositories.ProfileAdminRepository;
import org.example.profiles.repositories.ProfileUserRepository;
import org.example.profiles.repositories.ProfileWorkerRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class ProfilesService extends ProfilesServiceGrpc.ProfilesServiceImplBase {

    private final ProfileUserRepository profileUserRepository;
    private final ProfileWorkerRepository profileWorkerRepository;
    private final ProfileAdminRepository profileAdminRepository;

    public ProfilesService(ProfileUserRepository profileUserRepository, ProfileWorkerRepository profileWorkerRepository, ProfileAdminRepository profileAdminRepository) {
        super();
        this.profileUserRepository = profileUserRepository;
        this.profileWorkerRepository = profileWorkerRepository;
        this.profileAdminRepository = profileAdminRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public void getMyProfile(GetProfileRequest request, StreamObserver<GetProfileResponse> responseObserver) {
        Long userId = request.getUserId();
        List<String> roles = request.getRolesList();

        ProfileUser user = profileUserRepository.findByUserId(userId) .orElseThrow(() -> new StatusRuntimeException(
                Status.NOT_FOUND.withDescription("Profile not found for user " + userId)
        ));


        com.example.generated.ProfileUser protoUser = com.example.generated.ProfileUser.newBuilder()
                .setFullname(user.getFullName() == null ? "" : user.getFullName())
                .setBio(user.getBio() == null ? "" : user.getBio())
                .build();

        com.example.generated.ProfileWorker protoWorker = null;

        if(roles.contains("ROLE_WORKER")){
            protoWorker = profileWorkerRepository.findById(userId)
                    .map(worker -> com.example.generated.ProfileWorker.newBuilder()
                            .setUserId(worker.getUserId())
                            .setDegreeTitle(worker.getDegreeTitle() == null ? "" : worker.getDegreeTitle())
                            .setUnitName(worker.getUnitName() == null ? "" : worker.getUnitName())
                            .build()
                    )
                    .orElse(null);
        }

        com.example.generated.ProfileAdmin protoAdmin = null;
        if (roles.contains("ROLE_ADMIN")) {
            protoAdmin = profileAdminRepository.findById(userId)
                    .map(admin -> com.example.generated.ProfileAdmin.newBuilder()
                            .setUserId(admin.getUserId())
                            .setUnitName(admin.getUnitName() == null ? "" : admin.getUnitName())
                            .build()
                    )
                    .orElse(null);
        }

        ProfileView.Builder viewBuilder = ProfileView.newBuilder()
                .setUser(protoUser);

        if (protoWorker != null) viewBuilder.setWorker(protoWorker);
        if (protoAdmin != null) viewBuilder.setAdmin(protoAdmin);

        ApiResponse apiResponse = ApiResponse.newBuilder().setCode(200).setMessage("Get this!!").build();

        GetProfileResponse response = GetProfileResponse.newBuilder().setProfile(viewBuilder.build()).setResponse(apiResponse).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
