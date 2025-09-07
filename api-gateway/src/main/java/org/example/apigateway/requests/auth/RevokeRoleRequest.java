package org.example.apigateway.requests.auth;

import lombok.Data;

@Data
public class RevokeRoleRequest {
    Long userId;
    RoleName roleName;
}
