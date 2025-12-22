package org.example.apigateway.responses;

import com.google.protobuf.Api;
import lombok.Data;

@Data
public class AdminInitStatementsForYearResponse {
    int year;
    long count;
    ApiResponse apiResponse;
}
