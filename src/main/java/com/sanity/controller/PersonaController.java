package com.sanity.controller;

import com.sanity.dto.*;
import com.sanity.service.PersonaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/personas")
@RequiredArgsConstructor
public class PersonaController {
    
    private final PersonaService personaService;
    
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
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePersona(@PathVariable Integer id) {
        personaService.deletePersona(id);
        return ResponseEntity.noContent().build();
    }
}