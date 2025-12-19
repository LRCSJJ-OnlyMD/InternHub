package com.internhub.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.internhub.dto.BulkOperationRequest;
import com.internhub.dto.BulkOperationResponse;
import com.internhub.dto.InternshipRequest;
import com.internhub.dto.InternshipResponse;
import com.internhub.dto.InternshipSearchRequest;
import com.internhub.dto.RefusalRequest;
import com.internhub.model.Internship;
import com.internhub.model.InternshipStatus;
import com.internhub.model.Role;
import com.internhub.model.Sector;
import com.internhub.model.User;
import com.internhub.repository.InternshipRepository;
import com.internhub.repository.SectorRepository;
import com.internhub.repository.UserRepository;
import com.internhub.service.ActivityLogService;
import com.internhub.service.EmailService;
import com.internhub.service.InternshipService;
import com.internhub.service.NotificationService;
import com.internhub.specification.InternshipSpecification;

/**
 * Implementation of InternshipService. Follows SOLID principles: - SRP: Manages
 * only internship business logic - OCP: Extensible through inheritance or
 * composition - DIP: Depends on repository interfaces, not concrete
 * implementations
 */
@Service
@Transactional
public class InternshipServiceImpl implements InternshipService {

    private final InternshipRepository internshipRepository;
    private final UserRepository userRepository;
    private final SectorRepository sectorRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;

    public InternshipServiceImpl(InternshipRepository internshipRepository,
            UserRepository userRepository,
            SectorRepository sectorRepository,
            EmailService emailService,
            NotificationService notificationService,
            ActivityLogService activityLogService) {
        this.internshipRepository = internshipRepository;
        this.userRepository = userRepository;
        this.sectorRepository = sectorRepository;
        this.emailService = emailService;
        this.notificationService = notificationService;
        this.activityLogService = activityLogService;
    }

    @Override
    public InternshipResponse createInternship(InternshipRequest request, Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Sector sector = sectorRepository.findById(request.getSectorId())
                .orElseThrow(() -> new RuntimeException("Sector not found"));

        Internship internship = new Internship();
        internship.setTitle(request.getTitle());
        internship.setDescription(request.getDescription());
        internship.setCompanyName(request.getCompanyName());
        internship.setCompanyAddress(request.getCompanyAddress());
        internship.setStartDate(request.getStartDate());
        internship.setEndDate(request.getEndDate());
        internship.setStudent(student);
        internship.setSector(sector);
        internship.setStatus(InternshipStatus.DRAFT);

        Internship saved = internshipRepository.save(internship);

        // Log activity
        activityLogService.logActivity(student.getEmail(), ActivityLogService.ACTION_INTERNSHIP_CREATE,
                "INTERNSHIP", saved.getId(),
                "Created internship: " + saved.getTitle());

        return mapToResponse(saved);
    }

    @Override
    public InternshipResponse updateInternship(Long id, InternshipRequest request, Long studentId) {
        Internship internship = internshipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Internship not found"));

        // Authorization check
        if (!internship.getStudent().getId().equals(studentId)) {
            throw new RuntimeException("Unauthorized: Not your internship");
        }

        // Business rule: Only DRAFT or REFUSED can be modified
        if (!internship.isModifiable()) {
            throw new RuntimeException("Cannot modify internship with status: " + internship.getStatus());
        }

        Sector sector = sectorRepository.findById(request.getSectorId())
                .orElseThrow(() -> new RuntimeException("Sector not found"));

        internship.setTitle(request.getTitle());
        internship.setDescription(request.getDescription());
        internship.setCompanyName(request.getCompanyName());
        internship.setCompanyAddress(request.getCompanyAddress());
        internship.setStartDate(request.getStartDate());
        internship.setEndDate(request.getEndDate());
        internship.setSector(sector);

        Internship updated = internshipRepository.save(internship);
        return mapToResponse(updated);
    }

    @Override
    public InternshipResponse submitInternship(Long id, Long studentId) {
        Internship internship = internshipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Internship not found"));

        // Authorization check
        if (!internship.getStudent().getId().equals(studentId)) {
            throw new RuntimeException("Unauthorized: Not your internship");
        }

        // Business logic: Use domain method for status transition
        internship.submit();

        // Multi-instructor notification: Notify ALL instructors in the sector
        List<User> instructors = userRepository.findByRoleAndSectorsContaining(Role.INSTRUCTOR, internship.getSector());
        if (!instructors.isEmpty()) {
            String studentName = internship.getStudent().getFirstName() + " " + internship.getStudent().getLastName();

            // Send email and notification to ALL instructors in the sector
            for (User instructor : instructors) {
                emailService.sendInternshipSubmittedEmail(
                        instructor.getEmail(),
                        studentName,
                        internship.getTitle()
                );
                notificationService.notifyInternshipSubmitted(internship);
            }

            // Don't assign instructor yet - let them claim it
            internship.setInstructor(null);
        }

        Internship updated = internshipRepository.save(internship);

        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InternshipResponse> getStudentInternships(Long studentId) {
        return internshipRepository.findByStudentId(studentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InternshipResponse> getPendingInternshipsForInstructor(Long instructorId) {
        return internshipRepository.findPendingInternshipsForInstructor(
                InternshipStatus.PENDING_VALIDATION, instructorId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InternshipResponse> getValidatedInternshipsForInstructor(Long instructorId) {
        return internshipRepository.findByInstructorIdAndStatus(
                instructorId, InternshipStatus.VALIDATED)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InternshipResponse> getAvailableInternshipsForInstructor(Long instructorId) {
        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        // Get all PENDING_VALIDATION internships with no assigned instructor in instructor's sectors
        return internshipRepository.findAll().stream()
                .filter(internship -> internship.getStatus() == InternshipStatus.PENDING_VALIDATION)
                .filter(internship -> internship.getInstructor() == null)
                .filter(internship -> instructor.getSectors().contains(internship.getSector()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InternshipResponse claimInternship(Long id, Long instructorId) {
        Internship internship = internshipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Internship not found"));

        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        // Business rules validation
        if (internship.getStatus() != InternshipStatus.PENDING_VALIDATION) {
            throw new RuntimeException("Internship is not pending validation");
        }

        if (internship.getInstructor() != null) {
            throw new RuntimeException("Internship has already been claimed by another instructor");
        }

        if (!instructor.getSectors().contains(internship.getSector())) {
            throw new RuntimeException("Unauthorized: Sector not assigned to you");
        }

        // Claim the internship
        internship.setInstructor(instructor);
        Internship updated = internshipRepository.save(internship);

        // Send confirmation email and notification to student
        emailService.sendInternshipSubmittedEmail(
                instructor.getEmail(),
                internship.getStudent().getFirstName() + " " + internship.getStudent().getLastName(),
                internship.getTitle()
        );

        notificationService.notifyInternshipClaimed(updated);

        // Log activity
        activityLogService.logActivity(instructor.getEmail(), ActivityLogService.ACTION_INTERNSHIP_CLAIM,
                "INTERNSHIP", updated.getId(),
                "Claimed internship: " + updated.getTitle() + " by student "
                + updated.getStudent().getFirstName() + " " + updated.getStudent().getLastName());

        return mapToResponse(updated);
    }

    @Override
    public InternshipResponse validateInternship(Long id, Long instructorId) {
        Internship internship = internshipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Internship not found"));

        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        // Business rule: Instructor must have the sector assigned
        if (!instructor.getSectors().contains(internship.getSector())) {
            throw new RuntimeException("Unauthorized: Sector not assigned to you");
        }

        // Business logic: Use domain method for validation
        internship.validate(instructor);

        Internship updated = internshipRepository.save(internship);

        // Send email notification and in-app notification to student
        emailService.sendInternshipValidatedEmail(
                internship.getStudent().getEmail(),
                internship.getStudent().getFirstName() + " " + internship.getStudent().getLastName(),
                internship.getTitle(),
                instructor.getFirstName() + " " + instructor.getLastName()
        );

        notificationService.notifyInternshipValidated(updated, null);

        // Log activity
        activityLogService.logActivity(instructor.getEmail(), ActivityLogService.ACTION_INTERNSHIP_VALIDATE,
                "INTERNSHIP", updated.getId(),
                "Validated internship: " + updated.getTitle() + " by student "
                + updated.getStudent().getFirstName() + " " + updated.getStudent().getLastName());

        return mapToResponse(updated);
    }

    @Override
    public InternshipResponse refuseInternship(Long id, RefusalRequest refusalRequest, Long instructorId) {
        Internship internship = internshipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Internship not found"));

        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        // Business rule: Instructor must have the sector assigned
        if (!instructor.getSectors().contains(internship.getSector())) {
            throw new RuntimeException("Unauthorized: Sector not assigned to you");
        }

        // Business logic: Use domain method for refusal
        internship.refuse(refusalRequest.getRefusalComment());

        Internship updated = internshipRepository.save(internship);

        // Send email notification and in-app notification to student
        emailService.sendInternshipRefusedEmail(
                internship.getStudent().getEmail(),
                internship.getStudent().getFirstName() + " " + internship.getStudent().getLastName(),
                internship.getTitle(),
                refusalRequest.getRefusalComment()
        );

        notificationService.notifyInternshipRefused(updated, refusalRequest.getRefusalComment());

        // Log activity
        activityLogService.logActivity(instructor.getEmail(), ActivityLogService.ACTION_INTERNSHIP_REFUSE,
                "INTERNSHIP", updated.getId(),
                "Refused internship: " + updated.getTitle() + " by student "
                + updated.getStudent().getFirstName() + " " + updated.getStudent().getLastName()
                + " - Reason: " + refusalRequest.getRefusalComment());

        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InternshipResponse> getAllInternships() {
        return internshipRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public InternshipResponse getInternshipById(Long id) {
        Internship internship = internshipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Internship not found"));
        return mapToResponse(internship);
    }

    @Override
    public void deleteInternship(Long id) {
        if (!internshipRepository.existsById(id)) {
            throw new RuntimeException("Internship not found");
        }
        internshipRepository.deleteById(id);
    }

    @Override
    public InternshipResponse reassignInstructor(Long id, Long instructorId) {
        Internship internship = internshipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Internship not found"));

        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        if (instructor.getRole() != Role.INSTRUCTOR) {
            throw new RuntimeException("User is not an instructor");
        }

        internship.setInstructor(instructor);
        Internship updated = internshipRepository.save(internship);

        // Send email notification to student
        emailService.sendInstructorReassignedEmail(
                internship.getStudent().getEmail(),
                internship.getStudent().getFirstName() + " " + internship.getStudent().getLastName(),
                internship.getTitle(),
                instructor.getFirstName() + " " + instructor.getLastName()
        );

        return mapToResponse(updated);
    }

    // DEPRECATED: Report path is now handled via DocumentService
    /*
    public InternshipResponse setReportPath(Long id, String reportPath) {
        Internship internship = internshipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Internship not found"));

        internship.setReportPath(reportPath);
        Internship updated = internshipRepository.save(internship);

        // Send email notification to instructor if assigned
        if (internship.getInstructor() != null) {
            emailService.sendReportUploadedEmail(
                    internship.getInstructor().getEmail(),
                    internship.getInstructor().getFirstName() + " " + internship.getInstructor().getLastName(),
                    internship.getStudent().getFirstName() + " " + internship.getStudent().getLastName(),
                    internship.getTitle()
            );
        }

        return mapToResponse(updated);
    }

    public String getReportPath(Long id) {
        Internship internship = internshipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Internship not found"));

        return internship.getReportPath();
    }
     */
    @Transactional(readOnly = true)
    public List<InternshipResponse> searchInternships(
            Long sectorId, InternshipStatus status, String companyName,
            Long studentId, Long instructorId,
            LocalDate startDateFrom, LocalDate startDateTo,
            LocalDate endDateFrom, LocalDate endDateTo) {

        Specification<Internship> spec = InternshipSpecification.buildSpecification(
                sectorId, status, companyName, studentId, instructorId,
                startDateFrom, startDateTo, endDateFrom, endDateTo
        );

        return internshipRepository.findAll(spec)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InternshipResponse> searchInternships(Specification<Internship> spec) {
        return internshipRepository.findAll(spec)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Map Internship entity to response DTO. Follows SRP - single mapping
     * responsibility.
     */
    private InternshipResponse mapToResponse(Internship internship) {
        InternshipResponse response = new InternshipResponse();
        response.setId(internship.getId());
        response.setTitle(internship.getTitle());
        response.setDescription(internship.getDescription());
        response.setCompanyName(internship.getCompanyName());
        response.setCompanyAddress(internship.getCompanyAddress());
        response.setStartDate(internship.getStartDate());
        response.setEndDate(internship.getEndDate());
        response.setStatus(internship.getStatus());

        // Student info
        User student = internship.getStudent();
        response.setStudentId(student.getId());
        response.setStudentName(student.getFirstName() + " " + student.getLastName());
        response.setStudentEmail(student.getEmail());

        // Instructor info (if assigned)
        if (internship.getInstructor() != null) {
            User instructor = internship.getInstructor();
            response.setInstructorId(instructor.getId());
            response.setInstructorName(instructor.getFirstName() + " " + instructor.getLastName());
        }

        // Sector info
        Sector sector = internship.getSector();
        response.setSectorId(sector.getId());
        response.setSectorName(sector.getName());

        // Report info - now handled via DocumentService, check for documents instead
        // TODO: Query DocumentService to check if report exists
        response.setHasReport(false); // Placeholder until DocumentService integration
        response.setRefusalComment(internship.getRefusalComment());

        // Timestamps
        response.setCreatedAt(internship.getCreatedAt());
        response.setUpdatedAt(internship.getUpdatedAt());
        response.setSubmittedAt(internship.getSubmittedAt());
        response.setValidatedAt(internship.getValidatedAt());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InternshipResponse> searchInternshipsEnhanced(InternshipSearchRequest searchRequest) {
        // Build specification
        Specification<Internship> spec = InternshipSpecification.buildEnhancedSpecification(
                searchRequest.getSectorId(),
                searchRequest.getStatus(),
                searchRequest.getCompanyName(),
                searchRequest.getTitle(),
                searchRequest.getStudentId(),
                searchRequest.getInstructorId(),
                searchRequest.getStudentName(),
                searchRequest.getInstructorName(),
                searchRequest.getStartDateFrom(),
                searchRequest.getStartDateTo(),
                searchRequest.getEndDateFrom(),
                searchRequest.getEndDateTo()
        );

        // Build pageable with sorting
        Sort sort = "DESC".equalsIgnoreCase(searchRequest.getSortDirection())
                ? Sort.by(searchRequest.getSortBy()).descending()
                : Sort.by(searchRequest.getSortBy()).ascending();

        Pageable pageable = PageRequest.of(
                searchRequest.getPage(),
                searchRequest.getSize(),
                sort
        );

        // Execute query
        Page<Internship> internshipPage = internshipRepository.findAll(spec, pageable);

        // Map to response DTOs
        return internshipPage.map(this::mapToResponse);
    }

    @Override
    public BulkOperationResponse performBulkOperation(BulkOperationRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<BulkOperationResponse.OperationResult> results = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        for (Long internshipId : request.getInternshipIds()) {
            try {
                Internship internship = internshipRepository.findById(internshipId)
                        .orElseThrow(() -> new RuntimeException("Internship not found: " + internshipId));

                boolean success = false;
                String message = "";

                switch (request.getOperationType()) {
                    case UPDATE_STATUS:
                        if (canUpdateStatus(user, internship, request.getNewStatus())) {
                            internship.setStatus(request.getNewStatus());
                            internshipRepository.save(internship);

                            // Log activity
                            // TODO: Implement specific activity logging methods
                            // activityLogService.logStatusChange(internship, user);
                            // Send notification
                            // TODO: Implement specific notification methods
                            // notificationService.notifyStatusChange(internship);
                            success = true;
                            message = "Status updated to " + request.getNewStatus();
                        } else {
                            message = "Not authorized to update status";
                        }
                        break;

                    case ASSIGN_INSTRUCTOR:
                        if (user.getRole() == Role.ADMIN || user.getRole() == Role.INSTRUCTOR) {
                            User instructor = userRepository.findById(request.getNewInstructorId())
                                    .orElseThrow(() -> new RuntimeException("Instructor not found"));

                            if (instructor.getRole() != Role.INSTRUCTOR) {
                                message = "User is not an instructor";
                            } else {
                                internship.setInstructor(instructor);
                                internshipRepository.save(internship);

                                // Log activity
                                // TODO: Implement specific activity logging
                                // activityLogService.logInstructorAssignment(internship, instructor, user);
                                // Send notification
                                // TODO: Implement specific notification method
                                // notificationService.notifyInstructorAssignment(internship, instructor);
                                success = true;
                                message = "Instructor assigned successfully";
                            }
                        } else {
                            message = "Not authorized to assign instructor";
                        }
                        break;

                    case VALIDATE:
                        if (user.getRole() == Role.ADMIN
                                || (user.getRole() == Role.INSTRUCTOR
                                && internship.getInstructor() != null
                                && internship.getInstructor().getId().equals(userId))) {

                            internship.setStatus(InternshipStatus.VALIDATED);
                            internshipRepository.save(internship);

                            // Log activity
                            // TODO: Implement specific logging
                            // activityLogService.logValidation(internship, user);
                            // Send notification
                            // TODO: Implement specific notification
                            // notificationService.notifyValidation(internship);
                            success = true;
                            message = "Internship validated";
                        } else {
                            message = "Not authorized to validate";
                        }
                        break;

                    case REJECT:
                        if (user.getRole() == Role.ADMIN
                                || (user.getRole() == Role.INSTRUCTOR
                                && internship.getInstructor() != null
                                && internship.getInstructor().getId().equals(userId))) {

                            internship.setStatus(InternshipStatus.REJECTED);
                            if (request.getRejectionReason() != null) {
                                // TODO: Add rejectionReason field to Internship model
                                // internship.setRejectionReason(request.getRejectionReason());
                            }
                            internshipRepository.save(internship);

                            // Log activity
                            // TODO: Implement specific logging
                            // activityLogService.logRejection(internship, user, request.getRejectionReason());
                            // Send notification
                            // TODO: Implement specific notification
                            // notificationService.notifyRejection(internship);
                            success = true;
                            message = "Internship rejected";
                        } else {
                            message = "Not authorized to reject";
                        }
                        break;

                    case DELETE:
                        if (user.getRole() == Role.ADMIN
                                || (internship.getStudent().getId().equals(userId)
                                && internship.getStatus() == InternshipStatus.PENDING)) {

                            // Log activity before deletion
                            // TODO: Implement specific logging
                            // activityLogService.logDeletion(internship, user);
                            internshipRepository.delete(internship);
                            success = true;
                            message = "Internship deleted";
                        } else {
                            message = "Not authorized to delete";
                        }
                        break;
                }

                results.add(new BulkOperationResponse.OperationResult(internshipId, success, message));
                if (success) {
                    successCount++;
                } else {
                    failureCount++;
                }

            } catch (Exception e) {
                results.add(new BulkOperationResponse.OperationResult(
                        internshipId, false, "Error: " + e.getMessage()
                ));
                failureCount++;
            }
        }

        String summaryMessage = String.format(
                "Bulk operation completed: %d succeeded, %d failed out of %d requested",
                successCount, failureCount, request.getInternshipIds().size()
        );

        return new BulkOperationResponse(
                request.getInternshipIds().size(),
                successCount,
                failureCount,
                results,
                summaryMessage
        );
    }

    private boolean canUpdateStatus(User user, Internship internship, InternshipStatus newStatus) {
        if (user.getRole() == Role.ADMIN) {
            return true;
        }

        if (user.getRole() == Role.INSTRUCTOR) {
            return internship.getInstructor() != null
                    && internship.getInstructor().getId().equals(user.getId());
        }

        if (user.getRole() == Role.STUDENT) {
            return internship.getStudent().getId().equals(user.getId())
                    && (internship.getStatus() == InternshipStatus.PENDING
                    || internship.getStatus() == InternshipStatus.REJECTED);
        }

        return false;
    }
}
