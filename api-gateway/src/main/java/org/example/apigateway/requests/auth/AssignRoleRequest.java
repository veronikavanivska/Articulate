package org.example.apigateway.requests.auth;

import lombok.Data;

@Data
public class AssignRoleRequest {
    Long userId;
    RoleName roleName;
}
