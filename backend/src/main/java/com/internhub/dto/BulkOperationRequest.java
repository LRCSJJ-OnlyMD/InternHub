package com.internhub.dto;

import com.internhub.model.InternshipStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for bulk operations on internships.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkOperationRequest {

    private List<Long> internshipIds;
    private BulkOperationType operationType;
    private InternshipStatus newStatus;
    private Long newInstructorId;
    private String rejectionReason;

    public enum BulkOperationType {
        UPDATE_STATUS,
        ASSIGN_INSTRUCTOR,
        DELETE,
        VALIDATE,
        REJECT
    }
}
