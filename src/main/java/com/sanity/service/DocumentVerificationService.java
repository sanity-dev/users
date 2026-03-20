package com.sanity.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import com.sanity.dto.DocumentVerificationResultDto;
import com.sanity.model.DocumentoTerapeuta;
import com.sanity.model.Terapeuta;
import com.sanity.repository.DocumentoTerapeutaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentVerificationService {

    private final ImageAnnotatorClient visionClient;
    private final Storage storage;
    @Qualifier("gcsBucketName")
    private final String bucketName;
    private final DocumentoTerapeutaRepository documentoRepository;

    /**
     * Verifica un documento extrayendo texto con OCR y comparando con datos del terapeuta.
     */
    public DocumentVerificationResultDto verifyDocument(DocumentoTerapeuta documento, Terapeuta terapeuta) {
        try {
            // 1. Extraer texto del documento usando Vision API
            String extractedText = extractText(documento.getUrlStorage(), documento.getContentType());

            if (extractedText == null || extractedText.isBlank()) {
                documento.setEstado("PENDIENTE");
                documento.setMotivoRechazo("No se pudo extraer texto del documento. Asegúrate de subir un documento legible.");
                documentoRepository.save(documento);
                return DocumentVerificationResultDto.pending(
                        "No se pudo extraer texto del documento.");
            }

            // Guardar texto extraído
            documento.setTextoExtraido(extractedText);

            // 2. Normalizar el texto extraído
            String normalizedText = normalize(extractedText);

            // 3. Verificar según el tipo de documento
            String tipoDoc = documento.getTipoDocumento();
            DocumentVerificationResultDto result;

            switch (tipoDoc) {
                case "tarjeta_profesional":
                    result = verifyTarjetaProfesional(normalizedText, terapeuta);
                    break;
                case "titulos":
                    result = verifyTitulo(normalizedText, terapeuta);
                    break;
                case "identificacion":
                    result = verifyIdentificacion(normalizedText, terapeuta);
                    break;
                default:
                    result = DocumentVerificationResultDto.pending(
                            "Tipo de documento no reconocido para verificación automática.");
                    break;
            }

            // 4. Actualizar estado del documento
            documento.setEstado(result.getEstado());
            documento.setMotivoRechazo(result.getMotivoRechazo());
            documentoRepository.save(documento);

            return result;

        } catch (Exception e) {
            System.err.println("❌ Error en verificación de documento: " + e.getMessage());
            e.printStackTrace();
            documento.setEstado("PENDIENTE");
            documento.setMotivoRechazo("Error técnico en la verificación automática: " + e.getMessage());
            documentoRepository.save(documento);
            return DocumentVerificationResultDto.pending(
                    "Error técnico en la verificación. Se revisará manualmente.");
        }
    }

    /**
     * Extrae texto de un documento. Usa método diferente según el tipo:
     * - PDF: batchAnnotateFiles (soporta PDF nativamente)
     * - Imágenes (JPG, PNG): batchAnnotateImages con bytes
     */
    private String extractText(String imageUrl, String contentType) throws Exception {
        // Obtener el objectName del URL
        // URL format: https://storage.googleapis.com/BUCKET/PATH
        String objectName = imageUrl.replace(
                "https://storage.googleapis.com/" + bucketName + "/", "");

        System.out.println("🔍 Vision API - Descargando archivo de GCS: " + objectName);

        // Descargar bytes del archivo desde GCS
        Blob blob = storage.get(bucketName, objectName);
        if (blob == null) {
            System.err.println("⚠️ No se encontró el archivo en GCS: " + objectName);
            return null;
        }
        byte[] fileBytes = blob.getContent();
        System.out.println("� Archivo descargado: " + fileBytes.length + " bytes, tipo: " + contentType);

        // Determinar si es PDF o imagen
        boolean isPdf = contentType != null && contentType.toLowerCase().contains("pdf");

        if (isPdf) {
            return extractTextFromPdf(fileBytes);
        } else {
            return extractTextFromImage(fileBytes);
        }
    }

    /**
     * Extrae texto de una imagen (JPG, PNG) usando batchAnnotateImages.
     */
    private String extractTextFromImage(byte[] imageBytes) throws Exception {
        Image image = Image.newBuilder()
                .setContent(ByteString.copyFrom(imageBytes))
                .build();

        Feature feature = Feature.newBuilder()
                .setType(Feature.Type.DOCUMENT_TEXT_DETECTION)
                .build();

        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(feature)
                .setImage(image)
                .build();

        BatchAnnotateImagesResponse response = visionClient.batchAnnotateImages(
                List.of(request));

        if (response.getResponsesList().isEmpty()) {
            return null;
        }

        AnnotateImageResponse imageResponse = response.getResponses(0);

        if (imageResponse.hasError()) {
            System.err.println("⚠️ Vision API error (imagen): " + imageResponse.getError().getMessage());
            return null;
        }

        String text = imageResponse.getFullTextAnnotation().getText();
        System.out.println("✅ Vision API - Texto extraído de imagen (" + (text != null ? text.length() : 0) + " caracteres)");
        return text;
    }

    /**
     * Extrae texto de un PDF usando batchAnnotateFiles (soporta PDF nativamente).
     */
    private String extractTextFromPdf(byte[] pdfBytes) throws Exception {
        InputConfig inputConfig = InputConfig.newBuilder()
                .setMimeType("application/pdf")
                .setContent(ByteString.copyFrom(pdfBytes))
                .build();

        Feature feature = Feature.newBuilder()
                .setType(Feature.Type.DOCUMENT_TEXT_DETECTION)
                .build();

        AnnotateFileRequest fileRequest = AnnotateFileRequest.newBuilder()
                .setInputConfig(inputConfig)
                .addFeatures(feature)
                .build();

        BatchAnnotateFilesResponse response = visionClient.batchAnnotateFiles(
                List.of(fileRequest));

        if (response.getResponsesList().isEmpty()) {
            return null;
        }

        AnnotateFileResponse fileResponse = response.getResponses(0);

        if (fileResponse.hasError()) {
            System.err.println("⚠️ Vision API error (PDF): " + fileResponse.getError().getMessage());
            return null;
        }

        // Concatenar texto de todas las páginas del PDF
        StringBuilder allText = new StringBuilder();
        for (AnnotateImageResponse pageResponse : fileResponse.getResponsesList()) {
            if (pageResponse.hasFullTextAnnotation()) {
                allText.append(pageResponse.getFullTextAnnotation().getText());
                allText.append("\n");
            }
        }

        String text = allText.toString().trim();
        System.out.println("✅ Vision API - Texto extraído de PDF (" + text.length() + " caracteres, "
                + fileResponse.getResponsesCount() + " páginas)");
        return text;
    }

    /**
     * Verifica la tarjeta profesional: busca número de tarjeta, nombre y cédula.
     */
    private DocumentVerificationResultDto verifyTarjetaProfesional(String normalizedText, Terapeuta terapeuta) {
        boolean nombreFound = containsName(normalizedText, terapeuta.getNombre());
        boolean cedulaFound = terapeuta.getCedula() != null
                && normalizedText.contains(normalize(terapeuta.getCedula()));
        boolean tarjetaFound = terapeuta.getTarjetaProfesional() != null
                && normalizedText.contains(normalize(terapeuta.getTarjetaProfesional()));

        if (nombreFound && cedulaFound && tarjetaFound) {
            return DocumentVerificationResultDto.success();
        }

        List<String> reasons = new ArrayList<>();
        if (!tarjetaFound)
            reasons.add("Número de tarjeta profesional no encontrado en el documento");
        if (!nombreFound)
            reasons.add("Nombre del terapeuta no encontrado en el documento");
        if (!cedulaFound)
            reasons.add("Número de cédula no encontrado en el documento");

        return DocumentVerificationResultDto.failure(
                String.join(". ", reasons) + ".",
                nombreFound, cedulaFound, tarjetaFound);
    }

    /**
     * Verifica el título/diploma: busca cédula y nombre.
     */
    private DocumentVerificationResultDto verifyTitulo(String normalizedText, Terapeuta terapeuta) {
        boolean nombreFound = containsName(normalizedText, terapeuta.getNombre());
        boolean cedulaFound = terapeuta.getCedula() != null
                && normalizedText.contains(normalize(terapeuta.getCedula()));

        if (nombreFound && cedulaFound) {
            return DocumentVerificationResultDto.success();
        }

        List<String> reasons = new ArrayList<>();
        if (!nombreFound)
            reasons.add("Nombre del terapeuta no encontrado en el título");
        if (!cedulaFound)
            reasons.add("Número de cédula no encontrado en el título");

        return DocumentVerificationResultDto.failure(
                String.join(". ", reasons) + ".",
                nombreFound, cedulaFound, false);
    }

    /**
     * Verifica el documento de identificación: busca cédula y nombre.
     */
    private DocumentVerificationResultDto verifyIdentificacion(String normalizedText, Terapeuta terapeuta) {
        boolean nombreFound = containsName(normalizedText, terapeuta.getNombre());
        boolean cedulaFound = terapeuta.getCedula() != null
                && normalizedText.contains(normalize(terapeuta.getCedula()));

        if (nombreFound && cedulaFound) {
            return DocumentVerificationResultDto.success();
        }

        List<String> reasons = new ArrayList<>();
        if (!nombreFound)
            reasons.add("Nombre no encontrado en el documento de identificación");
        if (!cedulaFound)
            reasons.add("Número de cédula no encontrado en el documento de identificación");

        return DocumentVerificationResultDto.failure(
                String.join(". ", reasons) + ".",
                nombreFound, cedulaFound, false);
    }

    /**
     * Verifica si el nombre completo (o partes significativas) aparece en el texto.
     */
    private boolean containsName(String normalizedText, String fullName) {
        if (fullName == null || fullName.isBlank())
            return false;

        String normalizedName = normalize(fullName);

        if (normalizedText.contains(normalizedName)) {
            return true;
        }

        String[] parts = normalizedName.split("\\s+");
        if (parts.length <= 2) {
            for (String part : parts) {
                if (part.length() >= 3 && !normalizedText.contains(part)) {
                    return false;
                }
            }
            return true;
        }

        int matchCount = 0;
        for (String part : parts) {
            if (part.length() >= 3 && normalizedText.contains(part)) {
                matchCount++;
            }
        }
        return matchCount >= 2;
    }

    /**
     * Normaliza texto: minúsculas, sin tildes, sin caracteres especiales extra.
     */
    private String normalize(String text) {
        if (text == null)
            return "";
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return normalized.toLowerCase().trim();
    }
}
