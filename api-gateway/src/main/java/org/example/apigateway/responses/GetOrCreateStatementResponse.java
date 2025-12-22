package org.example.apigateway.responses;

import com.google.protobuf.Api;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetOrCreateStatementResponse {
    WorkerStatementResponse statement;
    ApiResponse apiResponse;
}
