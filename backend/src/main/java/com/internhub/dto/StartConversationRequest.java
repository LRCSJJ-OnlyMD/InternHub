package com.internhub.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartConversationRequest {

    @NotNull(message = "Other user ID is required")
    private Long otherUserId;

    @NotNull(message = "Internship ID is required")
    private Long internshipId;

    // Optional initial message
    private String initialMessage;
}
