package org.example.apigateway.config;

import org.example.apigateway.requests.auth.RoleName;
import org.springframework.stereotype.Component;

import static com.example.generated.RoleName.*;

@Component
public class RoleMapper {

    public static RoleName map(com.example.generated.RoleName roleName) {
        if(roleName == null || roleName == com.example.generated.RoleName.UNRECOGNIZED) {
            throw new IllegalArgumentException("RoleName cannot be null");
        }
        switch (roleName) {
            case ROLE_USER -> {
                return RoleName.ROLE_USER;
            }
            case ROLE_ADMIN -> {
                return RoleName.ROLE_ADMIN;
            }
            case ROLE_WORKER ->{
                return RoleName.ROLE_WORKER;
            }
            default -> throw new IllegalArgumentException("Unknown role: " + roleName);
        }

    }

    public static com.example.generated.RoleName map(RoleName roleName) {
        if(roleName == null) {
            throw new IllegalArgumentException("RoleName cannot be null");
        }
        switch (roleName) {
            case ROLE_USER -> {
                return ROLE_USER;
            }
            case ROLE_ADMIN -> {
                return ROLE_ADMIN;
            }
            case ROLE_WORKER ->{
                return ROLE_WORKER;
            }
            default -> throw new IllegalArgumentException("Unknown role: " + roleName);
        }

    }
}
