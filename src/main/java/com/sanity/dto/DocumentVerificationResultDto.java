package com.sanity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentVerificationResultDto {

    private boolean verified;
    private String estado;
    private String motivoRechazo;
    private boolean nombreEncontrado;
    private boolean cedulaEncontrada;
    private boolean tarjetaEncontrada;

    public static DocumentVerificationResultDto success() {
        DocumentVerificationResultDto dto = new DocumentVerificationResultDto();
        dto.setVerified(true);
        dto.setEstado("VERIFICADO");
        dto.setNombreEncontrado(true);
        dto.setCedulaEncontrada(true);
        dto.setTarjetaEncontrada(true);
        return dto;
    }

    public static DocumentVerificationResultDto failure(String motivo,
            boolean nombre, boolean cedula, boolean tarjeta) {
        DocumentVerificationResultDto dto = new DocumentVerificationResultDto();
        dto.setVerified(false);
        dto.setEstado("RECHAZADO");
        dto.setMotivoRechazo(motivo);
        dto.setNombreEncontrado(nombre);
        dto.setCedulaEncontrada(cedula);
        dto.setTarjetaEncontrada(tarjeta);
        return dto;
    }

    public static DocumentVerificationResultDto pending(String motivo) {
        DocumentVerificationResultDto dto = new DocumentVerificationResultDto();
        dto.setVerified(false);
        dto.setEstado("PENDIENTE");
        dto.setMotivoRechazo(motivo);
        return dto;
    }
}
