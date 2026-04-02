package com.sanity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordInternalDto {
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo inválido")
    private String email;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    private String nuevaPassword;
}
