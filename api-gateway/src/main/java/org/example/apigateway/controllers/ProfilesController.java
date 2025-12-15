package org.example.apigateway.controllers;

import org.example.apigateway.clients.ProfilesClient;
import org.example.apigateway.mappers.ProfileMapper;
import org.example.apigateway.config.SecurityConfig;
import org.example.apigateway.requests.profiles.UpdateProfileRequest;
import org.example.apigateway.responses.GetProfileResponse;
import org.example.apigateway.responses.Response;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
public class ProfilesController {

    @GetMapping("/me")
    public GetProfileResponse getProfile() {
        Long userId =  Long.parseLong(SecurityConfig.getCurrentUserId());

        var response = ProfilesClient.getProfile(userId);

        return ProfileMapper.toResponse(response);
    }

    @PostMapping("/update")
    public Response<Void> updateProfile(@RequestBody UpdateProfileRequest request) {
        Long userId =  Long.parseLong(SecurityConfig.getCurrentUserId());

        var response = ProfilesClient.updateMyProfile(request, userId);

        var api = response.getResponse();

        return new Response<>(
                api.getCode(),
                api.getMessage()
        );
    }


    @GetMapping("/someone")
    public GetProfileResponse getSomeOneProfile(@RequestParam Long userId) {
        var response = ProfilesClient.seeSomeoneProfile(userId);

        return ProfileMapper.toResponse(response);
    }
}
