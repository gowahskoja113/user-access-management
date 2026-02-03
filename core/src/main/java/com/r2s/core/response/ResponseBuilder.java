package com.r2s.core.response;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Utility class for building consistent API responses
 */
@Component("r2sResponseBuilder")
@RequiredArgsConstructor
public class ResponseBuilder {

    /**
     * Build a success response with data and message
     * @param data Response data
     * @param message Success message
     * @return ResponseEntity with ApiResponse
     */
    public <T> ResponseEntity<ApiResponse<T>> buildSuccessResponse(T data, String message) {
        return ResponseEntity.ok(ApiResponse.success(data, message));
    }

    /**
     * Build an error response with message
     * @param message Error message
     * @return ResponseEntity with ApiResponse
     */
    public <T> ResponseEntity<ApiResponse<T>> buildErrorResponse(String message) {
        return ResponseEntity.badRequest().body(ApiResponse.error(message));
    }

    /**
     * Build a success response with data only
     * @param data Response data
     * @return ResponseEntity with ApiResponse
     */
    public <T> ResponseEntity<ApiResponse<T>> buildSuccessResponse(T data) {
        return buildSuccessResponse(data, "Success");
    }

    /**
     * Build a not found response
     * @param message Error message
     * @return ResponseEntity with ApiResponse
     */
    public <T> ResponseEntity<ApiResponse<T>> buildNotFoundResponse(String message) {
        // .build() will not return body and skip message for not found
        return ResponseEntity.notFound().build();
    }

    /**
     * Build an unauthorized response
     * @param message Error message
     * @return ResponseEntity with ApiResponse
     */
    public <T> ResponseEntity<ApiResponse<T>> buildUnauthorizedResponse(String message) {
        return ResponseEntity.status(401).body(ApiResponse.error(message));
    }

    /**
     * Build a forbidden response
     * @param message Error message
     * @return ResponseEntity with ApiResponse
     */
    public <T> ResponseEntity<ApiResponse<T>> buildForbiddenResponse(String message) {
        return ResponseEntity.status(403).body(ApiResponse.error(message));
    }
}