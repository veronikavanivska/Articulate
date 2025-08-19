package org.example.apigateway.requests;

import lombok.Data;

@Data
public class AssignRoleRequest {
    Long userId;
    RoleName roleName;
}
