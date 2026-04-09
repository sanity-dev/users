package com.sanity.controller;

import com.sanity.dto.DocumentUploadResponseDto;
import com.sanity.dto.FaceVerificationResultDto;
import com.sanity.model.DocumentoTerapeuta;
import com.sanity.service.DocumentService;
import com.sanity.service.FaceVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final FaceVerificationService faceVerificationService;

    /**
     * POST /api/documents/upload
     * Recibe un archivo multipart y el tipo de documento.
     * El terapeuta se identifica por el JWT (correo en el SecurityContext).
     * Ahora incluye verificación automática con Vision API.
     */
    @PostMapping("/upload")
    public ResponseEntity<DocumentUploadResponseDto> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType,
            Authentication authentication) {

        try {
            String correo = authentication.getName();
            DocumentoTerapeuta documento = documentService.uploadDocument(correo, file, documentType);

            return ResponseEntity.ok(
                    DocumentUploadResponseDto.ok(
                            "Documento subido y verificado automáticamente",
                            documento.getId(),
                            documento.getEstado(),
                            documento.getMotivoRechazo()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    DocumentUploadResponseDto.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    DocumentUploadResponseDto.error("Error al subir el documento: " + e.getMessage()));
        }
    }

    /**
     * POST /api/documents/verify-face
     * Recibe una selfie (imagen) y compara el rostro con el documento de identidad
     * del terapeuta.
     * El terapeuta se identifica por el JWT.
     */
    @PostMapping("/verify-face")
    public ResponseEntity<FaceVerificationResultDto> verifyFace(
            @RequestParam("selfie") MultipartFile selfie,
            Authentication authentication) {

        try {
            // Validar que sea una imagen
            String contentType = selfie.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(
                        FaceVerificationResultDto.error("El archivo debe ser una imagen (JPG o PNG)."));
            }

            String correo = authentication.getName();
            FaceVerificationResultDto result = faceVerificationService.verifyFace(correo, selfie);

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    FaceVerificationResultDto.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    FaceVerificationResultDto.error("Error en la verificación facial: " + e.getMessage()));
        }
    }

    /**
     * GET /api/documents/verification-status
     * Retorna el estado de verificación de los documentos del terapeuta
     * autenticado.
     */
    @GetMapping("/verification-status")
    public ResponseEntity<Map<String, Object>> getVerificationStatus(Authentication authentication) {
        try {
            String correo = authentication.getName();
            Map<String, Object> status = documentService.getVerificationStatus(correo);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "Error al obtener el estado: " + e.getMessage()));
        }
    }
}
