package com.sanity.service;

import com.sanity.dto.*;
import com.sanity.model.*;
import com.sanity.repository.*;
import jakarta.validation.Valid;
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
        
        // Validar cédula solo si viene en el request
        if (request.getCedula() != null && !request.getCedula().isBlank()) {
            if (personaRepository.existsByCedula(request.getCedula())) {
                throw new RuntimeException("La cédula ya está registrada");
            }
        }
        
        // Asignar automáticamente TipoUsuario.USUARIO si no viene especificado
        TipoUsuario tipoUsuario = request.getTipoUsuario() != null ? 
            request.getTipoUsuario() : TipoUsuario.USUARIO;
        
        Persona persona;
        
        if (tipoUsuario == TipoUsuario.USUARIO) {
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
        dto.setFotoPerfilUrl(persona.getFotoPerfilUrl());
        
        if (persona instanceof Usuario) {
            Usuario usuario = (Usuario) persona;
            dto.setContactoEmergencia(usuario.getContactoEmergencia());
            dto.setTelefonoContactoEmergencia(usuario.getTelefonoContactoEmergencia());
        } else if (persona instanceof Terapeuta) {
            Terapeuta terapeuta = (Terapeuta) persona;
            dto.setTarjetaProfesional(terapeuta.getTarjetaProfesional());
        }
        
        return dto;
    }
    private final GoogleAuthService googleAuthService;

    public AuthResponseDto loginWithGoogle(GoogleLoginDto request) {
        // Obtener info completa del usuario de Google (nombre, correo, foto)
        GoogleUserInfo googleUser = googleAuthService.obtenerInfoUsuario(request.getToken());

        // Busca o crea el usuario
        Persona persona = personaRepository.findByCorreo(googleUser.getEmail())
                .orElseGet(() -> {
                    Usuario usuario = new Usuario();
                    usuario.setNombre(googleUser.getName() != null ? googleUser.getName() : "Usuario Google");
                    usuario.setCorreo(googleUser.getEmail());
                    usuario.setContraseña(passwordEncoder.encode("google-oauth-" + System.nanoTime()));
                    usuario.setTipoUsuario(TipoUsuario.USUARIO);
                    usuario.setFotoPerfilUrl(googleUser.getPicture());

                    return usuarioRepository.save(usuario);
                });

        // Actualizar nombre y foto si el usuario ya existe pero no los tiene
        boolean updated = false;
        if ((persona.getNombre() == null || persona.getNombre().equals("Usuario Google")) && googleUser.getName() != null) {
            persona.setNombre(googleUser.getName());
            updated = true;
        }
        if (persona.getFotoPerfilUrl() == null && googleUser.getPicture() != null) {
            persona.setFotoPerfilUrl(googleUser.getPicture());
            updated = true;
        }
        if (updated) {
            personaRepository.save(persona);
        }

        String token = jwtService.generateToken(persona.getCorreo());
        PersonaDto personaDto = convertToDto(persona);

        return new AuthResponseDto(token, personaDto);
    }
    
    @Transactional
    public AuthResponseDto registerTherapist(TherapistRegisterDto request) {
        // Validaciones
        if (personaRepository.existsByCorreo(request.getCorreo())) {
            throw new RuntimeException("El correo ya está registrado");
        }
        
        if (personaRepository.existsByCedula(request.getCedula())) {
            throw new RuntimeException("La cédula ya está registrada");
        }
        
        // Validar que la tarjeta profesional no esté registrada (si es necesario)
        if (terapeutaRepository.existsByTarjetaProfesional(request.getTarjetaProfesional())) {
            throw new RuntimeException("El número de tarjeta profesional ya está registrado");
        }
        
        Terapeuta terapeuta = new Terapeuta();
        terapeuta.setNombre(request.getNombre());
        terapeuta.setCorreo(request.getCorreo());
        terapeuta.setContraseña(passwordEncoder.encode(request.getContraseña()));
        terapeuta.setCedula(request.getCedula());
        terapeuta.setTarjetaProfesional(request.getTarjetaProfesional());
        terapeuta.setTelefono(request.getTelefono());
        terapeuta.setTipoUsuario(TipoUsuario.TERAPEUTA);
        
        terapeuta = terapeutaRepository.save(terapeuta);
        
        String token = jwtService.generateToken(terapeuta.getCorreo());
        PersonaDto personaDto = convertToDto(terapeuta);
        
        return new AuthResponseDto(token, personaDto);
    }
}