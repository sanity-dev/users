package com.sanity.controller;

import com.sanity.dto.AuthResponseDto;
import com.sanity.dto.GoogleLoginDto;
import com.sanity.dto.LoginDto;
import com.sanity.dto.RegisterDto;
import com.sanity.dto.TherapistRegisterDto;
import com.sanity.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterDto request) {
        return ResponseEntity.ok(authService.register(request));
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginDto request) {
        return ResponseEntity.ok(authService.login(request));
    }
    @PostMapping("/google")
    public ResponseEntity<AuthResponseDto> loginWithGoogle(@Valid @RequestBody GoogleLoginDto request) {
        return ResponseEntity.ok(authService.loginWithGoogle(request));
    }
    
    @PostMapping("/register-therapist")
    public ResponseEntity<AuthResponseDto> registerTherapist(@Valid @RequestBody TherapistRegisterDto request) {
        return ResponseEntity.ok(authService.registerTherapist(request));
    }
}
