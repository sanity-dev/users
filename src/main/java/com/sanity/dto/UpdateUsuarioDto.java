package com.sanity.dto;

public class UpdateUsuarioDto {
    private String contactoEmergencia;
    private String telefonoContactoEmergencia;

    // Constructores
    public UpdateUsuarioDto() {}

    public UpdateUsuarioDto(String contactoEmergencia, String telefonoContactoEmergencia) {
        this.contactoEmergencia = contactoEmergencia;
        this.telefonoContactoEmergencia = telefonoContactoEmergencia;
    }

    // Getters y Setters
    public String getContactoEmergencia() {
        return contactoEmergencia;
    }

    public void setContactoEmergencia(String contactoEmergencia) {
        this.contactoEmergencia = contactoEmergencia;
    }

    public String getTelefonoContactoEmergencia() {
        return telefonoContactoEmergencia;
    }

    public void setTelefonoContactoEmergencia(String telefonoContactoEmergencia) {
        this.telefonoContactoEmergencia = telefonoContactoEmergencia;
    }
}
