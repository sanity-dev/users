package com.sanity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FaceVerificationResultDto {

    private boolean success;
    private boolean faceMatch;
    private float similarity;
    private String estado;
    private String message;
    private String selfieUrl;

    public static FaceVerificationResultDto match(float similarity, String selfieUrl) {
        FaceVerificationResultDto dto = new FaceVerificationResultDto();
        dto.setSuccess(true);
        dto.setFaceMatch(true);
        dto.setSimilarity(similarity);
        dto.setEstado("VERIFICADO");
        dto.setMessage("Verificación facial exitosa. Similitud: " + String.format("%.1f", similarity) + "%");
        dto.setSelfieUrl(selfieUrl);
        return dto;
    }

    public static FaceVerificationResultDto noMatch(float similarity, String selfieUrl) {
        FaceVerificationResultDto dto = new FaceVerificationResultDto();
        dto.setSuccess(true);
        dto.setFaceMatch(false);
        dto.setSimilarity(similarity);
        dto.setEstado("RECHAZADO");
        dto.setMessage("El rostro no coincide con el documento. Similitud: " + String.format("%.1f", similarity) + "%");
        dto.setSelfieUrl(selfieUrl);
        return dto;
    }

    public static FaceVerificationResultDto noFaceDetected(String detail) {
        FaceVerificationResultDto dto = new FaceVerificationResultDto();
        dto.setSuccess(false);
        dto.setFaceMatch(false);
        dto.setSimilarity(0);
        dto.setEstado("RECHAZADO");
        dto.setMessage("No se detectó un rostro. " + detail);
        return dto;
    }

    public static FaceVerificationResultDto error(String message) {
        FaceVerificationResultDto dto = new FaceVerificationResultDto();
        dto.setSuccess(false);
        dto.setFaceMatch(false);
        dto.setSimilarity(0);
        dto.setEstado("ERROR");
        dto.setMessage(message);
        return dto;
    }
}
