package org.example.apigateway.mappers;

import com.example.generated.*;
import org.example.apigateway.requests.profiles.UpdateProfileRequest;
import org.example.apigateway.responses.GetProfileResponse;
import org.example.apigateway.responses.Response;


public class ProfileMapper {

    public static GetProfileResponse toResponse(com.example.generated.GetProfileResponse protoProfile) {
        ProfileView view = protoProfile.hasProfile() ? protoProfile.getProfile() : null;

        GetProfileResponse.ProfileUser userProfile = null;
        GetProfileResponse.ProfileWorker workerProfile = null;
        GetProfileResponse.ProfileAdmin adminProfile = null;


        if(view != null && view.hasUser()) {
            var user = view.getUser();
            userProfile = new GetProfileResponse.ProfileUser(user.getFullname(),user.getBio());
        }

        if(view != null && view.hasWorker()) {
            var user = view.getWorker();

            java.util.List<org.example.apigateway.responses.DisciplineResponse> disciplines =
                    user.getDisciplinesList().stream()
                            .map(d -> new org.example.apigateway.responses.DisciplineResponse(d.getId(), d.getName()))
                            .toList();

            workerProfile = new GetProfileResponse.ProfileWorker(user.getDegreeTitle(), user.getUnitName() , disciplines);
        }

        if(view != null && view.hasAdmin()) {
            var user = view.getAdmin();
            adminProfile = new GetProfileResponse.ProfileAdmin(user.getUnitName());
        }

        Response<Void> response = new Response<>(
                protoProfile.getResponse().getCode(),
                protoProfile.getResponse().getMessage()
        );

        return new GetProfileResponse(userProfile,workerProfile,adminProfile,response);
    }

    public static UpdateMyProfileRequest toGrpc(UpdateProfileRequest request, Long userId) {
        UpdateMyProfileRequest.Builder builder = UpdateMyProfileRequest.newBuilder()
                .setUserId(userId);

        if(request.getUser() != null) {
            var user   = request.getUser();
            ProfileUser.Builder profileUser = ProfileUser.newBuilder();
            if(user.getFullName() != null) profileUser.setFullname(user.getFullName());
            if(user.getBio() != null) profileUser.setBio(user.getBio());
            builder.setUser(profileUser.build());
        }

        if(request.getWorker() != null) {
            var user = request.getWorker();
            ProfileWorker.Builder profileWorker = ProfileWorker.newBuilder();
            if(user.getDegreeTitle() != null) profileWorker.setDegreeTitle(user.getDegreeTitle());
            if(user.getUnitName() != null) profileWorker.setUnitName(user.getUnitName());
            builder.setWorker(profileWorker.build());
        }

        if(request.getAdmin() != null) {
            var user = request.getAdmin();
            ProfileAdmin.Builder profileAdmin = ProfileAdmin.newBuilder();
            if(user.getUnitName() != null) profileAdmin.setUnitName(user.getUnitName());
            builder.setAdmin(profileAdmin.build());
        }

        return builder.build();
    }
}
