package org.example.auth.helpers;

import org.example.auth.entities.RoleName;

public class RoleMapper {

    public static org.example.auth.entities.RoleName map(com.example.generated.RoleName roleName) {
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
}
