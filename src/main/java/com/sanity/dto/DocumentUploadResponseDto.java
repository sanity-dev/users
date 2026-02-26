package com.sanity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadResponseDto {

    private boolean success;
    private String message;
    private String documentId;

    public static DocumentUploadResponseDto ok(String message, Long documentId) {
        return new DocumentUploadResponseDto(true, message, documentId != null ? documentId.toString() : null);
    }

    public static DocumentUploadResponseDto error(String message) {
        return new DocumentUploadResponseDto(false, message, null);
    }
}
