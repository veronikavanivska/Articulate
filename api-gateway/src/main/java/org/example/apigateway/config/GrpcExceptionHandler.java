package org.example.apigateway.config;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import jakarta.servlet.ServletException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GrpcExceptionHandler {

    @ExceptionHandler(StatusRuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleGrpcDirect(StatusRuntimeException ex) {
        return buildGrpcResponse(ex);
    }

    // Łapie przypadki gdy StatusRuntimeException jest "przyczyną" (root cause)
    @ExceptionHandler({ ServletException.class, RuntimeException.class, Exception.class })
    public ResponseEntity<Map<String, Object>> handleWrapped(Exception ex) {
        Throwable root = ex;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }

        if (root instanceof StatusRuntimeException grpcEx) {
            return buildGrpcResponse(grpcEx);
        }

        // fallback: zawsze JSON, a nie HTML
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "code", "INTERNAL",
                "message", "Internal Server Error"
        ));
    }

    private ResponseEntity<Map<String, Object>> buildGrpcResponse(StatusRuntimeException ex) {
        Status status = ex.getStatus();
        Status.Code code = status.getCode();          // np. ALREADY_EXISTS
        String description = status.getDescription(); // np. "Email already exists"

        HttpStatus http = switch (code) {
            case INVALID_ARGUMENT -> HttpStatus.BAD_REQUEST;     // 400
            case ALREADY_EXISTS -> HttpStatus.CONFLICT;          // 409
            case NOT_FOUND -> HttpStatus.NOT_FOUND;              // 404
            case UNAUTHENTICATED -> HttpStatus.UNAUTHORIZED;     // 401
            case PERMISSION_DENIED -> HttpStatus.FORBIDDEN;      // 403
            default -> HttpStatus.INTERNAL_SERVER_ERROR;         // 500
        };

        return ResponseEntity.status(http).body(Map.of(
                "code", code.name(),
                "message", (description != null && !description.isBlank()) ? description : code.name()
        ));
    }
}
