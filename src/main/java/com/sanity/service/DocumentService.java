package com.sanity.service;

import com.sanity.dto.DocumentVerificationResultDto;
import com.sanity.model.DocumentoTerapeuta;
import com.sanity.model.Persona;
import com.sanity.model.Terapeuta;
import com.sanity.repository.DocumentoTerapeutaRepository;
import com.sanity.repository.PersonaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final CloudStorageService cloudStorageService;
    private final DocumentoTerapeutaRepository documentoRepository;
    private final PersonaRepository personaRepository;
    private final DocumentVerificationService verificationService;

    /**
     * Sube un documento del terapeuta a GCS, guarda metadatos en BD, y ejecuta verificación automática.
     */
    public DocumentoTerapeuta uploadDocument(String correoTerapeuta, MultipartFile file, String tipoDocumento)
            throws IOException {

        // Buscar la persona por correo
        Persona persona = personaRepository.findByCorreo(correoTerapeuta)
                .orElseThrow(() -> new RuntimeException("No se encontró el usuario con correo: " + correoTerapeuta));

        // Verificar que sea terapeuta
        if (!(persona instanceof Terapeuta)) {
            throw new RuntimeException("El usuario no es un terapeuta.");
        }

        Terapeuta terapeuta = (Terapeuta) persona;

        // Definir la carpeta en GCS
        String folder = "documentos/" + terapeuta.getIdPersona() + "/" + tipoDocumento;

        // Subir a GCS
        String url = cloudStorageService.uploadFile(file, folder);

        // Si ya existe un documento del mismo tipo, actualizarlo; si no, crear uno nuevo
        Optional<DocumentoTerapeuta> existente = documentoRepository
                .findByTerapeutaIdPersonaAndTipoDocumento(terapeuta.getIdPersona(), tipoDocumento);

        DocumentoTerapeuta documento;
        if (existente.isPresent()) {
            documento = existente.get();
            documento.setUrlStorage(url);
            documento.setNombreArchivo(file.getOriginalFilename());
            documento.setContentType(file.getContentType());
            documento.setTamanoBytes(file.getSize());
            documento.setEstado("PENDIENTE");
            documento.setFechaSubida(LocalDateTime.now());
            documento.setMotivoRechazo(null);
            documento.setTextoExtraido(null);
        } else {
            documento = new DocumentoTerapeuta();
            documento.setTerapeuta(terapeuta);
            documento.setTipoDocumento(tipoDocumento);
            documento.setNombreArchivo(file.getOriginalFilename());
            documento.setUrlStorage(url);
            documento.setContentType(file.getContentType());
            documento.setTamanoBytes(file.getSize());
            documento.setEstado("PENDIENTE");
            documento.setFechaSubida(LocalDateTime.now());
        }

        documento = documentoRepository.save(documento);

        // Ejecutar verificación automática con Vision API
        try {
            verificationService.verifyDocument(documento, terapeuta);
        } catch (Exception e) {
            System.err.println("⚠️ Error en verificación automática (el documento queda PENDIENTE): " + e.getMessage());
        }

        return documento;
    }

    /**
     * Retorna el estado de verificación de los documentos del terapeuta.
     */
    public Map<String, Object> getVerificationStatus(String correoTerapeuta) {
        Persona persona = personaRepository.findByCorreo(correoTerapeuta)
                .orElseThrow(() -> new RuntimeException("No se encontró el usuario con correo: " + correoTerapeuta));

        if (!(persona instanceof Terapeuta)) {
            throw new RuntimeException("El usuario no es un terapeuta.");
        }

        Terapeuta terapeuta = (Terapeuta) persona;

        List<DocumentoTerapeuta> documentos = documentoRepository
                .findByTerapeutaIdPersona(terapeuta.getIdPersona());

        // Determinar estado global
        String estadoGlobal = "pending";
        if (!documentos.isEmpty()) {
            boolean todosVerificados = documentos.stream()
                    .allMatch(d -> "VERIFICADO".equals(d.getEstado()));
            boolean algunoRechazado = documentos.stream()
                    .anyMatch(d -> "RECHAZADO".equals(d.getEstado()));

            if (todosVerificados) {
                estadoGlobal = "verified";
            } else if (algunoRechazado) {
                estadoGlobal = "rejected";
            }
        }

        // Mapear documentos al formato esperado por el frontend
        List<Map<String, String>> docs = documentos.stream()
                .map(d -> {
                    java.util.HashMap<String, String> map = new java.util.HashMap<>();
                    map.put("type", d.getTipoDocumento());
                    map.put("status", d.getEstado().toLowerCase());
                    map.put("uploadedAt", d.getFechaSubida().toString());
                    map.put("motivoRechazo", d.getMotivoRechazo() != null ? d.getMotivoRechazo() : "");
                    map.put("verificacionFacial", d.getVerificacionFacial() != null ? d.getVerificacionFacial() : "PENDIENTE");
                    map.put("selfieUrl", d.getSelfieUrl() != null ? d.getSelfieUrl() : "");
                    return (Map<String, String>) map;
                })
                .collect(Collectors.toList());

        // Determinar estado de verificación facial
        String estadoFacial = "pendiente";
        Optional<DocumentoTerapeuta> docIdentificacion = documentos.stream()
                .filter(d -> "identificacion".equals(d.getTipoDocumento()))
                .findFirst();
        if (docIdentificacion.isPresent() && docIdentificacion.get().getVerificacionFacial() != null) {
            estadoFacial = docIdentificacion.get().getVerificacionFacial().toLowerCase();
        }

        java.util.HashMap<String, Object> result = new java.util.HashMap<>();
        result.put("status", estadoGlobal);
        result.put("documents", docs);
        result.put("faceVerification", estadoFacial);
        return result;
    }
}
