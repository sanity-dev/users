package com.sanity.controller;

import com.sanity.dto.*;
import com.sanity.service.CloudStorageService;
import com.sanity.service.PersonaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/personas")
@RequiredArgsConstructor
public class PersonaController {
    
    private final PersonaService personaService;
    private final CloudStorageService cloudStorageService;
    
    @GetMapping
    public ResponseEntity<List<PersonaDto>> getAllPersonas() {
        return ResponseEntity.ok(personaService.getAllPersonas());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PersonaDto> getPersonaById(@PathVariable Integer id) {
        return ResponseEntity.ok(personaService.getPersonaById(id));
    }
    
    @PutMapping("/{id}/basica")
    public ResponseEntity<PersonaDto> updatePersonaBasica(
            @PathVariable Integer id,
            @Valid @RequestBody UpdatePersonaDto request) {
        return ResponseEntity.ok(personaService.updatePersonaBasica(id, request));
    }
    
    @PutMapping("/{id}/usuario")
    public ResponseEntity<PersonaDto> updateUsuario(
            @PathVariable Integer id,
            @RequestBody UpdateUsuarioDto request) {
        return ResponseEntity.ok(personaService.updateUsuario(id, request));
    }
    
    @PutMapping("/{id}/terapeuta")
    public ResponseEntity<PersonaDto> updateTerapeuta(
            @PathVariable Integer id,
            @RequestBody UpdateTerapeutaDto request) {
        return ResponseEntity.ok(personaService.updateTerapeuta(id, request));
    }

    /**
     * POST /api/personas/{id}/foto-perfil
     * Sube una foto de perfil a GCS y guarda la URL en la entidad Persona.
     */
    @PostMapping("/{id}/foto-perfil")
    public ResponseEntity<?> uploadFotoPerfil(
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file) {
        try {
            String folder = "fotos-perfil/" + id;
            String url = cloudStorageService.uploadFile(file, folder);
            PersonaDto updated = personaService.updateFotoPerfil(id, url);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "Error al subir la foto: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePersona(@PathVariable Integer id, @RequestBody java.util.Map<String, String> body) {
        try {
            String contraseña = body.get("contraseña");
            personaService.deletePersona(id, contraseña);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
    
    // --- NUEVO: Endpoints internos para recuperación desde microservicio notificaciones ---
    @GetMapping("/exists")
    public ResponseEntity<Map<String, Boolean>> checkEmailExists(@RequestParam String email) {
        boolean exists = personaService.existsByCorreo(email);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @PutMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordInternalDto request) {
        try {
            personaService.resetPassword(request.getEmail(), request.getNuevaPassword());
            return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}