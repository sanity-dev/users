package com.sanity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TherapistRegisterDto {
    @NotBlank(message = "El nombre es requerido")
    private String nombre;
    
    @NotBlank(message = "El correo es requerido")
    @Email(message = "Formato de correo inválido")
    private String correo;
    
    @NotBlank(message = "La contraseña es requerida")
    private String contraseña;
    
    @NotBlank(message = "La cédula es requerida")
    private String cedula;
    
    @NotBlank(message = "El número de tarjeta profesional es requerido")
    private String tarjetaProfesional;
    
    @NotBlank(message = "El teléfono es requerido")
    private String telefono;
    
    @NotBlank(message = "El tipo de usuario es requerido")
    private String tipoUsuario;
}