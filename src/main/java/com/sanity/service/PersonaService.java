package com.sanity.service;

import com.sanity.dto.*;
import com.sanity.model.*;
import com.sanity.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonaService {
    
    private final PersonaRepository personaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TerapeutaRepository terapeutaRepository;
    private final FichaProfesionalRepository fichaProfesionalRepository;
    private final PasswordEncoder passwordEncoder;
    
    public List<PersonaDto> getAllPersonas() {
        return personaRepository.findAll().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    public PersonaDto getPersonaById(Integer id) {
        Persona persona = personaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Persona no encontrada con id: " + id));
        return convertToDto(persona);
    }
    
    @Transactional
    public PersonaDto updatePersonaBasica(Integer id, UpdatePersonaDto request) {
        Persona persona = personaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Persona no encontrada con id: " + id));
        
        // Validar que el correo no esté en uso por otra persona
        if (request.getCorreo() != null && !persona.getCorreo().equals(request.getCorreo()) && 
            personaRepository.existsByCorreo(request.getCorreo())) {
            throw new RuntimeException("El correo ya está en uso");
        }
        
        // Validar que la cédula no esté en uso por otra persona
        if (request.getCedula() != null && !persona.getCedula().equals(request.getCedula()) && 
            personaRepository.existsByCedula(request.getCedula())) {
            throw new RuntimeException("La cédula ya está en uso");
        }
        
        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            persona.setNombre(request.getNombre());
        }
        if (request.getCorreo() != null && !request.getCorreo().isBlank()) {
            persona.setCorreo(request.getCorreo());
        }
        if (request.getContraseña() != null && !request.getContraseña().isBlank()) {
            persona.setContraseña(passwordEncoder.encode(request.getContraseña()));
        }
        if (request.getTelefono() != null) {
            persona.setTelefono(request.getTelefono());
        }
        if (request.getCedula() != null && !request.getCedula().isBlank()) {
            persona.setCedula(request.getCedula());
        }
        
        persona = personaRepository.save(persona);
        return convertToDto(persona);
    }
    
    @Transactional
    public PersonaDto updateUsuario(Integer id, UpdateUsuarioDto request) {
        Persona persona = personaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Persona no encontrada con id: " + id));
        
        if (!(persona instanceof Usuario)) {
            throw new RuntimeException("La persona con id " + id + " no es un usuario");
        }
        
        Usuario usuario = (Usuario) persona;
        
        if (request.getContactoEmergencia() != null) {
            usuario.setContactoEmergencia(request.getContactoEmergencia());
        }
        if (request.getTelefonoContactoEmergencia() != null) {
            usuario.setTelefonoContactoEmergencia(request.getTelefonoContactoEmergencia());
        }
        if (request.getMensajeEmergencia() != null) {
            usuario.setMensajeEmergencia(request.getMensajeEmergencia());
        }
        if (request.getTelefonoApoyoAlternativo() != null) {
            usuario.setTelefonoApoyoAlternativo(request.getTelefonoApoyoAlternativo());
        }
        
        usuario = usuarioRepository.save(usuario);
        return convertToDto(usuario);
    }
    
    @Transactional
    public PersonaDto updateTerapeuta(Integer id, UpdateTerapeutaDto request) {
        Persona persona = personaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Persona no encontrada con id: " + id));
        
        if (!(persona instanceof Terapeuta)) {
            throw new RuntimeException("La persona con id " + id + " no es un terapeuta");
        }
        
        Terapeuta terapeuta = (Terapeuta) persona;
        
        // Validar tarjeta profesional si se está actualizando
        if (request.getTarjetaProfesional() != null && 
            !request.getTarjetaProfesional().equals(terapeuta.getTarjetaProfesional())) {
            if (terapeutaRepository.existsByTarjetaProfesional(request.getTarjetaProfesional())) {
                throw new RuntimeException("La tarjeta profesional ya está registrada");
            }
            terapeuta.setTarjetaProfesional(request.getTarjetaProfesional());
        }
        
        // Actualizar o crear ficha profesional
        if (request.getCertificadoTituloProfesional() != null || 
            request.getCertificadoExpLaboral() != null) {
            
            FichaProfesional ficha = terapeuta.getFichaProfesional();
            
            if (ficha == null) {
                ficha = new FichaProfesional();
                ficha.setTerapeuta(terapeuta);
            }
            
            if (request.getCertificadoTituloProfesional() != null) {
                ficha.setCertificadoTituloProfesional(request.getCertificadoTituloProfesional());
            }
            if (request.getCertificadoExpLaboral() != null) {
                ficha.setCertificadoExpLaboral(request.getCertificadoExpLaboral());
            }
            if (request.getCertificadoEspecializacionMaestria() != null) {
                ficha.setCertificadoEspecializacionMaestria(request.getCertificadoEspecializacionMaestria());
            }
            
            fichaProfesionalRepository.save(ficha);
        }
        
        terapeuta = terapeutaRepository.save(terapeuta);
        return convertToDto(terapeuta);
    }
    
    @Transactional
    public PersonaDto updateFotoPerfil(Integer id, String fotoUrl) {
        Persona persona = personaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Persona no encontrada con id: " + id));
        persona.setFotoPerfilUrl(fotoUrl);
        persona = personaRepository.save(persona);
        return convertToDto(persona);
    }

    @Transactional
    public void deletePersona(Integer id, String contraseña) {
        Persona persona = personaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Persona no encontrada con id: " + id));
            
        if (contraseña == null || !passwordEncoder.matches(contraseña, persona.getContraseña())) {
            throw new RuntimeException("Contraseña incorrecta");
        }
        
        personaRepository.deleteById(id);
    }
    
    private PersonaDto convertToDto(Persona persona) {
        PersonaDto dto = new PersonaDto();
        dto.setIdPersona(persona.getIdPersona());
        dto.setNombre(persona.getNombre());
        dto.setCorreo(persona.getCorreo());
        dto.setTelefono(persona.getTelefono());
        dto.setCedula(persona.getCedula());
        dto.setTipoUsuario(persona.getTipoUsuario());
        dto.setFotoPerfilUrl(persona.getFotoPerfilUrl());
        
        if (persona instanceof Usuario) {
            Usuario usuario = (Usuario) persona;
            dto.setContactoEmergencia(usuario.getContactoEmergencia());
            dto.setTelefonoContactoEmergencia(usuario.getTelefonoContactoEmergencia());
            dto.setMensajeEmergencia(usuario.getMensajeEmergencia());
            dto.setTelefonoApoyoAlternativo(usuario.getTelefonoApoyoAlternativo());
        } else if (persona instanceof Terapeuta) {
            Terapeuta terapeuta = (Terapeuta) persona;
            dto.setTarjetaProfesional(terapeuta.getTarjetaProfesional());
        }
        
        return dto;
    }
}