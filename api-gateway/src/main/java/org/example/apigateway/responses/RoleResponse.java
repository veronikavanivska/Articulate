package org.example.apigateway.responses;

import lombok.Data;

@Data
public class RoleResponse {
    private String value; // np. "ROLE_ADMIN"
    private String label; // np. "Administrator"

}
