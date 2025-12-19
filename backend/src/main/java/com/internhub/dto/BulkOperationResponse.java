package com.internhub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for bulk operations results.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkOperationResponse {

    private int totalRequested;
    private int successCount;
    private int failureCount;
    private List<OperationResult> results;
    private String message;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperationResult {

        private Long internshipId;
        private boolean success;
        private String message;
    }
}
