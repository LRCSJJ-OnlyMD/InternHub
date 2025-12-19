package com.internhub.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.internhub.dto.CommentDTO;
import com.internhub.dto.CommentRequest;
import com.internhub.service.CommentService;

import jakarta.validation.Valid;

/**
 * REST controller for comment operations.
 */
@RestController
@RequestMapping("/api/internships/{internshipId}/comments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * Add a comment to an internship. POST
     * /api/internships/{internshipId}/comments
     */
    @PostMapping
    public ResponseEntity<CommentDTO> addComment(
            @PathVariable Long internshipId,
            @Valid @RequestBody CommentRequest request,
            Authentication authentication) {
        String userEmail = authentication.getName();
        CommentDTO comment = commentService.addComment(internshipId, userEmail, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    /**
     * Get all comments for an internship. GET
     * /api/internships/{internshipId}/comments
     */
    @GetMapping
    public ResponseEntity<List<CommentDTO>> getComments(@PathVariable Long internshipId) {
        List<CommentDTO> comments = commentService.getInternshipComments(internshipId);
        return ResponseEntity.ok(comments);
    }

    /**
     * Update a comment. PUT
     * /api/internships/{internshipId}/comments/{commentId}
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDTO> updateComment(
            @PathVariable Long internshipId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest request,
            Authentication authentication) {
        String userEmail = authentication.getName();
        CommentDTO comment = commentService.updateComment(commentId, userEmail, request);
        return ResponseEntity.ok(comment);
    }

    /**
     * Delete a comment. DELETE
     * /api/internships/{internshipId}/comments/{commentId}
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long internshipId,
            @PathVariable Long commentId,
            Authentication authentication) {
        String userEmail = authentication.getName();
        commentService.deleteComment(commentId, userEmail);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get comment count for an internship. GET
     * /api/internships/{internshipId}/comments/count
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getCommentCount(@PathVariable Long internshipId) {
        long count = commentService.getCommentCount(internshipId);
        return ResponseEntity.ok(Map.of("count", count));
    }
}
