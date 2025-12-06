package com.sanity.service;

import com.sanity.dto.*;
import com.sanity.model.*;
import com.sanity.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final PersonaRepository personaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TerapeutaRepository terapeutaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    
    @Transactional
    public AuthResponseDto register(RegisterDto request) {
        // Validaciones
        if (personaRepository.existsByCorreo(request.getCorreo())) {
            throw new RuntimeException("El correo ya está registrado");
        }
        
        if (personaRepository.existsByCedula(request.getCedula())) {
            throw new RuntimeException("La cédula ya está registrada");
        }
        
        Persona persona;
        
        if (request.getTipoUsuario() == TipoUsuario.USUARIO) {
            Usuario usuario = new Usuario();
            usuario.setNombre(request.getNombre());
            usuario.setCorreo(request.getCorreo());
            usuario.setContraseña(passwordEncoder.encode(request.getContraseña()));
            usuario.setTelefono(request.getTelefono());
            usuario.setCedula(request.getCedula());
            usuario.setTipoUsuario(TipoUsuario.USUARIO);
            
            persona = usuarioRepository.save(usuario);
            
        } else {
            Terapeuta terapeuta = new Terapeuta();
            terapeuta.setNombre(request.getNombre());
            terapeuta.setCorreo(request.getCorreo());
            terapeuta.setContraseña(passwordEncoder.encode(request.getContraseña()));
            terapeuta.setTelefono(request.getTelefono());
            terapeuta.setCedula(request.getCedula());
            terapeuta.setTipoUsuario(TipoUsuario.TERAPEUTA);
            
            persona = terapeutaRepository.save(terapeuta);
        }
        
        String token = jwtService.generateToken(persona.getCorreo());
        PersonaDto personaDto = convertToDto(persona);
        
        return new AuthResponseDto(token, personaDto);
    }
    
    public AuthResponseDto login(LoginDto request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getCorreo(),
                request.getContraseña()
            )
        );
        
        Persona persona = personaRepository.findByCorreo(request.getCorreo())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        String token = jwtService.generateToken(persona.getCorreo());
        PersonaDto personaDto = convertToDto(persona);
        
        return new AuthResponseDto(token, personaDto);
    }
    
    private PersonaDto convertToDto(Persona persona) {
        PersonaDto dto = new PersonaDto();
        dto.setIdPersona(persona.getIdPersona());
        dto.setNombre(persona.getNombre());
        dto.setCorreo(persona.getCorreo());
        dto.setTelefono(persona.getTelefono());
        dto.setCedula(persona.getCedula());
        dto.setTipoUsuario(persona.getTipoUsuario());
        
        if (persona instanceof Usuario) {
            Usuario usuario = (Usuario) persona;
            dto.setContactoEmergencia(usuario.getContactoEmergencia());
            dto.setTelefonoContactoEmergencia(usuario.getTelefonoContactoEmergencia());
        } else if (persona instanceof Terapeuta) {
            Terapeuta terapeuta = (Terapeuta) persona;
            dto.setNTarjetaProfesional(terapeuta.getNTarjetaProfesional());
        }
        
        return dto;
    }
}