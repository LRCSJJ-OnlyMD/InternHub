package com.internhub.controller;

import com.internhub.dto.BulkOperationRequest;
import com.internhub.dto.BulkOperationResponse;
import com.internhub.service.InternshipService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for bulk operations on internships. Allows efficient
 * management of multiple internships simultaneously.
 */
@RestController
@RequestMapping("/api/internships/bulk")
public class BulkOperationController {

    private final InternshipService internshipService;

    public BulkOperationController(InternshipService internshipService) {
        this.internshipService = internshipService;
    }

    /**
     * Perform bulk operations on internships. Supports: UPDATE_STATUS,
     * ASSIGN_INSTRUCTOR, DELETE, VALIDATE, REJECT
     */
    @PostMapping("/operation")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BulkOperationResponse> performBulkOperation(
            @RequestBody BulkOperationRequest request,
            Authentication authentication) {

        Long userId = Long.parseLong(authentication.getName());
        BulkOperationResponse response = internshipService.performBulkOperation(request, userId);

        return ResponseEntity.ok(response);
    }

    /**
     * Bulk status update endpoint.
     */
    @PostMapping("/update-status")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<BulkOperationResponse> bulkUpdateStatus(
            @RequestBody BulkOperationRequest request,
            Authentication authentication) {

        request.setOperationType(BulkOperationRequest.BulkOperationType.UPDATE_STATUS);
        Long userId = Long.parseLong(authentication.getName());
        BulkOperationResponse response = internshipService.performBulkOperation(request, userId);

        return ResponseEntity.ok(response);
    }

    /**
     * Bulk instructor assignment endpoint.
     */
    @PostMapping("/assign-instructor")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<BulkOperationResponse> bulkAssignInstructor(
            @RequestBody BulkOperationRequest request,
            Authentication authentication) {

        request.setOperationType(BulkOperationRequest.BulkOperationType.ASSIGN_INSTRUCTOR);
        Long userId = Long.parseLong(authentication.getName());
        BulkOperationResponse response = internshipService.performBulkOperation(request, userId);

        return ResponseEntity.ok(response);
    }

    /**
     * Bulk validate endpoint.
     */
    @PostMapping("/validate")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<BulkOperationResponse> bulkValidate(
            @RequestBody BulkOperationRequest request,
            Authentication authentication) {

        request.setOperationType(BulkOperationRequest.BulkOperationType.VALIDATE);
        Long userId = Long.parseLong(authentication.getName());
        BulkOperationResponse response = internshipService.performBulkOperation(request, userId);

        return ResponseEntity.ok(response);
    }

    /**
     * Bulk reject endpoint.
     */
    @PostMapping("/reject")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<BulkOperationResponse> bulkReject(
            @RequestBody BulkOperationRequest request,
            Authentication authentication) {

        request.setOperationType(BulkOperationRequest.BulkOperationType.REJECT);
        Long userId = Long.parseLong(authentication.getName());
        BulkOperationResponse response = internshipService.performBulkOperation(request, userId);

        return ResponseEntity.ok(response);
    }

    /**
     * Bulk delete endpoint.
     */
    @PostMapping("/delete")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<BulkOperationResponse> bulkDelete(
            @RequestBody BulkOperationRequest request,
            Authentication authentication) {

        request.setOperationType(BulkOperationRequest.BulkOperationType.DELETE);
        Long userId = Long.parseLong(authentication.getName());
        BulkOperationResponse response = internshipService.performBulkOperation(request, userId);

        return ResponseEntity.ok(response);
    }
}
