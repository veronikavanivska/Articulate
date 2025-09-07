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

        ProfileUser user = profileUserRepository.findByUserId(userId) .orElseThrow(() -> new StatusRuntimeException(
                Status.NOT_FOUND.withDescription("Profile not found for user " + userId)
        ));


        com.example.generated.ProfileUser protoUser = com.example.generated.ProfileUser.newBuilder()
                .setFullname(user.getFullname() == null ? "" : user.getFullname())
                .setBio(user.getBio() == null ? "" : user.getBio())
                .build();

        ProfileWorker protoWorker = null;

        if(user.getWorker() != null) {
            org.example.profiles.entities.ProfileWorker worker = profileWorkerRepository.findByUserId(userId).orElseThrow(() -> new StatusRuntimeException(
                    Status.NOT_FOUND.withDescription("Profile not found for user " + userId)
            ));

            protoWorker = ProfileWorker.newBuilder()
                    .setDegreeTitle(worker.getDegreeTitle())
                    .setUnitName(worker.getUnitName())
                    .build();
        }


        ProfileAdmin protoAdmin = null;
        if (user.getAdmin() != null) {
            org.example.profiles.entities.ProfileAdmin admin = profileAdminRepository.findByUserId(userId).orElseThrow(() -> new StatusRuntimeException(
                    Status.NOT_FOUND.withDescription("Profile not found for user " + userId)
            ));

            protoAdmin = ProfileAdmin.newBuilder().setUnitName(admin.getUnitName()).build();
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

    @Override
    @Transactional
    public void updateMyProfile(UpdateMyProfileRequest request, StreamObserver<UpdateMyProfileResponse> responseObserver) {
        Long userId = request.getUserId();


        var userProfile = profileUserRepository.findByUserId(userId).orElseThrow(() -> new StatusRuntimeException(
                Status.NOT_FOUND.withDescription("Profile not found for user " + userId)));

        var user = request.getUser();
        var entity = profileUserRepository.findByUserId(userId).orElseThrow();

        entity.setFullname(user.getFullname() == null ? "" : user.getFullname());
        entity.setBio(user.getBio() == null ? "" : user.getBio());
        profileUserRepository.save(entity);

        com.example.generated.ProfileUser protoUser = com.example.generated.ProfileUser.newBuilder()
                    .setFullname(entity.getFullname())
                    .setBio(entity.getBio())
                    .build();



        ProfileWorker protoWorker = null;

        if(userProfile.getWorker() != null) {
            var worker = request.getWorker();
            var entity2 = profileWorkerRepository.findByUserId(userId).orElseThrow(() -> new StatusRuntimeException(
                    Status.NOT_FOUND.withDescription("Profile not found for user " + userId)
            ));

            entity2.setDegreeTitle(worker.getDegreeTitle() == null ? "" : worker.getDegreeTitle());
            entity2.setUnitName(worker.getUnitName() == null ? "" : worker.getUnitName());
            profileWorkerRepository.save(entity2);

            protoWorker = ProfileWorker.newBuilder()
                    .setDegreeTitle(entity2.getDegreeTitle())
                    .setUnitName(entity2.getUnitName())
                    .build();
        }


        ProfileAdmin protoAdmin = null;

        if(userProfile.getAdmin() != null) {
            var admin = request.getAdmin();
            var entity3 = profileAdminRepository.findByUserId(userId).orElseThrow(() -> new StatusRuntimeException(
                    Status.NOT_FOUND.withDescription("Profile not found for user " + userId)
            ));

            entity3.setUnitName(admin.getUnitName() == null ? "" : admin.getUnitName());
            profileAdminRepository.save(entity3);

            protoAdmin = ProfileAdmin.newBuilder().setUnitName(entity3.getUnitName()).build();
        }

        ProfileView.Builder viewBuilder = ProfileView.newBuilder()
                .setUser(protoUser);

        if (protoWorker != null) viewBuilder.setWorker(protoWorker);
        if (protoAdmin != null) viewBuilder.setAdmin(protoAdmin);

        ApiResponse apiResponse = ApiResponse.newBuilder().setCode(200).setMessage("Update this!!").build();

        UpdateMyProfileResponse response = UpdateMyProfileResponse.newBuilder().setResponse(apiResponse).setProfile(viewBuilder.build()).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }

    @Override
    public void seeSomeoneProfile(SeeSomeoneProfileRequest request, StreamObserver<GetProfileResponse> responseObserver) {
        Long userId = request.getUserId();

        ProfileUser user = profileUserRepository.findByUserId(userId).orElseThrow();

        com.example.generated.ProfileUser protoUser = com.example.generated.ProfileUser.newBuilder()
                .setFullname(user.getFullname())
                .setBio(user.getBio())
                .build();


        ProfileWorker protoWorker = null;

        if(user.getWorker() != null) {
            org.example.profiles.entities.ProfileWorker worker = profileWorkerRepository.findByUserId(userId).orElseThrow(() -> new StatusRuntimeException(
                    Status.NOT_FOUND.withDescription("Profile not found for user " + userId)
            ));

            protoWorker = ProfileWorker.newBuilder()
                    .setDegreeTitle(worker.getDegreeTitle())
                    .setUnitName(worker.getUnitName())
                    .build();
        }


        ProfileAdmin protoAdmin = null;
        if (user.getAdmin() != null) {
            org.example.profiles.entities.ProfileAdmin admin = profileAdminRepository.findByUserId(userId).orElseThrow(() -> new StatusRuntimeException(
                    Status.NOT_FOUND.withDescription("Profile not found for user " + userId)
            ));

            protoAdmin = ProfileAdmin.newBuilder().setUnitName(admin.getUnitName()).build();
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
