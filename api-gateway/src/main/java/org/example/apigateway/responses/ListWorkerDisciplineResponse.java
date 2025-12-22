package org.example.apigateway.responses;

import lombok.Data;

import java.util.List;

@Data
public class ListWorkerDisciplineResponse {
    List<DisciplineResponse> discipline;
    ApiResponse apiResponse;
}
