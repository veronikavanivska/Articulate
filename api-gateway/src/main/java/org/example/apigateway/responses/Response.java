package org.example.apigateway.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Response<T> {
    private int code;
    private String message;
    private T accessToken;
    private T refreshToken;

    public Response(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
