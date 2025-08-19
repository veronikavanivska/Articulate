package org.example.apigateway.requests;

import lombok.Data;

@Data
public class RevokeRoleRequest {
    Long userId;
    RoleName roleName;
}
