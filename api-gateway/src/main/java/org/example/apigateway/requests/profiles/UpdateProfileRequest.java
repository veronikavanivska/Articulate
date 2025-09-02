package org.example.apigateway.requests.profiles;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateProfileRequest {
    private ProfileUser user;
    private ProfileWorker worker;
    private ProfileAdmin admin;

    @Data
    public static class ProfileUser{
        String fullName;
        String bio;
    }

    @Data
    public static class ProfileWorker{
        String degreeTitle;
        String unitName;
    }

    @Data
    public static class ProfileAdmin{
        String unitName;
    }

}
