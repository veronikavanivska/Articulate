package org.example.apigateway.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.apigateway.requests.profiles.UpdateProfileRequest;

@Data
@AllArgsConstructor
public class GetProfileResponse {

    private ProfileUser user;
    private ProfileWorker worker;
    private ProfileAdmin admin;
    private Response<Void> response;

    @Data
    @AllArgsConstructor
    public static class ProfileUser{
        String fullName;
        String bio;
    }

    @Data
    @AllArgsConstructor
    public static class ProfileWorker{
        String degreeTitle;
        String unitName;
    }

    @Data
    @AllArgsConstructor
    public static class ProfileAdmin{
        String unitName;
    }


}
