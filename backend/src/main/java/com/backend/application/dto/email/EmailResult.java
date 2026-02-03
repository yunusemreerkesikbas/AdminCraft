package com.backend.application.dto.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailResult {

    private boolean success;
    private String messageId;
    private String errorMessage;

    public static EmailResult success(String messageId) {
        return EmailResult.builder()
                .success(true)
                .messageId(messageId)
                .build();
    }

    public static EmailResult failure(String errorMessage) {
        return EmailResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}
