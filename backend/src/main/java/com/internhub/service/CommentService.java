package com.internhub.service;

import com.internhub.dto.CommentDTO;
import com.internhub.dto.CommentRequest;
import com.internhub.exception.ResourceNotFoundException;
import com.internhub.exception.UnauthorizedException;
import com.internhub.model.Comment;
import com.internhub.model.Internship;
import com.internhub.model.User;
import com.internhub.repository.CommentRepository;
import com.internhub.repository.InternshipRepository;
import com.internhub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing comments on internships.
 */
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final InternshipRepository internshipRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;

    @Autowired
    public CommentService(CommentRepository commentRepository,
            InternshipRepository internshipRepository,
            UserRepository userRepository,
            NotificationService notificationService,
            ActivityLogService activityLogService) {
        this.commentRepository = commentRepository;
        this.internshipRepository = internshipRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.activityLogService = activityLogService;
    }

    /**
     * Add a new comment to an internship.
     */
    @Transactional
    public CommentDTO addComment(Long internshipId, String userEmail, CommentRequest request) {
        Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Internship not found with id: " + internshipId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Comment comment = new Comment();
        comment.setInternship(internship);
        comment.setUser(user);
        comment.setContent(request.getContent());

        // Handle reply to another comment
        if (request.getParentCommentId() != null) {
            Comment parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found with id: " + request.getParentCommentId()));
            comment.setParentComment(parentComment);
        }

        Comment savedComment = commentRepository.save(comment);

        // Notify relevant users
        notifyCommentAdded(internship, user, savedComment);

        // Log activity
        String action = request.getParentCommentId() != null ? "Replied to comment" : "Added comment";
        activityLogService.logActivity(user.getEmail(), ActivityLogService.ACTION_COMMENT_ADD,
                "COMMENT", savedComment.getId(),
                action + " on internship: " + internship.getTitle());

        return convertToDTO(savedComment);
    }

    /**
     * Get all comments for an internship (hierarchical structure).
     */
    @Transactional(readOnly = true)
    public List<CommentDTO> getInternshipComments(Long internshipId) {
        if (!internshipRepository.existsById(internshipId)) {
            throw new ResourceNotFoundException("Internship not found with id: " + internshipId);
        }

        List<Comment> topLevelComments = commentRepository.findTopLevelCommentsByInternshipId(internshipId);
        return topLevelComments.stream()
                .map(this::convertToDTOWithReplies)
                .collect(Collectors.toList());
    }

    /**
     * Update a comment (only by the author).
     */
    @Transactional
    public CommentDTO updateComment(Long commentId, String userEmail, CommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        // Check if user is the author
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You can only edit your own comments");
        }

        comment.setContent(request.getContent());
        Comment updatedComment = commentRepository.save(comment);

        // Log activity
        activityLogService.logActivity(user.getEmail(), ActivityLogService.ACTION_COMMENT_UPDATE,
                "COMMENT", updatedComment.getId(),
                "Updated comment on internship: " + comment.getInternship().getTitle());

        return convertToDTO(updatedComment);
    }

    /**
     * Delete a comment (by author or admin).
     */
    @Transactional
    public void deleteComment(Long commentId, String userEmail) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        // Check if user is the author or admin
        if (!comment.getUser().getId().equals(user.getId()) && !user.getRole().name().equals("ADMIN")) {
            throw new UnauthorizedException("You can only delete your own comments or be an admin");
        }

        // Log activity before deletion
        activityLogService.logActivity(user.getEmail(), ActivityLogService.ACTION_COMMENT_DELETE,
                "COMMENT", comment.getId(),
                "Deleted comment on internship: " + comment.getInternship().getTitle());

        commentRepository.delete(comment);
    }

    /**
     * Get comment count for an internship.
     */
    @Transactional(readOnly = true)
    public long getCommentCount(Long internshipId) {
        return commentRepository.countByInternshipId(internshipId);
    }

    /**
     * Convert Comment entity to DTO.
     */
    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setInternshipId(comment.getInternship().getId());
        dto.setUserId(comment.getUser().getId());
        dto.setUserFirstName(comment.getUser().getFirstName());
        dto.setUserLastName(comment.getUser().getLastName());
        dto.setUserRole(comment.getUser().getRole().name());
        dto.setParentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null);
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        dto.setEdited(comment.isEdited());
        return dto;
    }

    /**
     * Convert Comment to DTO with nested replies.
     */
    private CommentDTO convertToDTOWithReplies(Comment comment) {
        CommentDTO dto = convertToDTO(comment);

        // Load replies
        List<Comment> replies = commentRepository.findRepliesByParentCommentId(comment.getId());
        dto.setReplies(replies.stream()
                .map(this::convertToDTOWithReplies)
                .collect(Collectors.toList()));
        dto.setReplyCount(replies.size());

        return dto;
    }

    /**
     * Notify relevant users when a comment is added.
     */
    private void notifyCommentAdded(Internship internship, User commenter, Comment comment) {
        // Notify internship owner (student)
        if (!internship.getStudent().getId().equals(commenter.getId())) {
            notificationService.notifyNewComment(
                    internship.getStudent(),
                    commenter,
                    internship,
                    comment.getId(),
                    comment.getParentComment() != null
            );
        }

        // If it's a reply, notify the parent comment author
        if (comment.getParentComment() != null) {
            User parentAuthor = comment.getParentComment().getUser();
            if (!parentAuthor.getId().equals(commenter.getId())
                    && !parentAuthor.getId().equals(internship.getStudent().getId())) {
                notificationService.notifyNewComment(
                        parentAuthor,
                        commenter,
                        internship,
                        comment.getId(),
                        true
                );
            }
        }

        // Notify assigned instructor if exists and not the commenter
        if (internship.getInstructor() != null
                && !internship.getInstructor().getId().equals(commenter.getId())
                && !internship.getInstructor().getId().equals(internship.getStudent().getId())) {
            notificationService.notifyNewComment(
                    internship.getInstructor(),
                    commenter,
                    internship,
                    comment.getId(),
                    false
            );
        }
    }
}
