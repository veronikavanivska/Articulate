package org.example.apigateway.controllers;



import org.example.apigateway.clients.AuthClient;
import org.example.apigateway.requests.LoginRequest;
import org.example.apigateway.requests.RegistrationRequest;
import org.example.apigateway.responses.Response;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.PermitAll;

@RequestMapping("/auth")
@RestController
public class AuthController {

    @PostMapping("/registration")
    public Response<Void> registration(@RequestBody RegistrationRequest registrationRequest) {
        var apiResponse = AuthClient.register(registrationRequest.getEmail(), registrationRequest.getPassword());

        return new Response<>(
                apiResponse.getCode(),
                apiResponse.getMessage()
        );
    }

    @PostMapping("/login")
    public Response<String> login(@RequestBody LoginRequest loginRequest) {
        var apiResponse = AuthClient.login(loginRequest.getEmail(), loginRequest.getPassword());
        var api = apiResponse.getApiResponse();
        return new Response<>(
                api.getCode(),
                api.getMessage(),
                apiResponse.getToken()
        );
    }
}
