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
    private String verificationStatus;
    private String motivoRechazo;

    public static DocumentUploadResponseDto ok(String message, Long documentId,
            String verificationStatus, String motivoRechazo) {
        DocumentUploadResponseDto dto = new DocumentUploadResponseDto();
        dto.setSuccess(true);
        dto.setMessage(message);
        dto.setDocumentId(documentId != null ? documentId.toString() : null);
        dto.setVerificationStatus(verificationStatus);
        dto.setMotivoRechazo(motivoRechazo);
        return dto;
    }

    public static DocumentUploadResponseDto error(String message) {
        DocumentUploadResponseDto dto = new DocumentUploadResponseDto();
        dto.setSuccess(false);
        dto.setMessage(message);
        return dto;
    }
}
