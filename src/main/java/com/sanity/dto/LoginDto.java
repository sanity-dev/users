package com.sanity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginDto {
    
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe ser válido")
    private String correo;
    
    @NotBlank(message = "La contraseña es obligatoria")
    private String contraseña;

    // Constructores
    public LoginDto() {}

    public LoginDto(String correo, String contraseña) {
        this.correo = correo;
        this.contraseña = contraseña;
    }

    // Getters y Setters
    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContraseña() {
        return contraseña;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }
}