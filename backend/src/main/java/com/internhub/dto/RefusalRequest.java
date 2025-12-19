package com.internhub.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for refusing an internship with mandatory comment. Used by instructor
 * endpoints.
 */
public class RefusalRequest {

    @NotBlank(message = "Refusal comment is mandatory")
    private String refusalComment;

    // Constructors
    public RefusalRequest() {
    }

    public RefusalRequest(String refusalComment) {
        this.refusalComment = refusalComment;
    }

    // Getters and Setters
    public String getRefusalComment() {
        return refusalComment;
    }

    public void setRefusalComment(String refusalComment) {
        this.refusalComment = refusalComment;
    }
}
