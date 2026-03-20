package com.sanity.dto;

public class UpdateUsuarioDto {
    private String contactoEmergencia;
    private String telefonoContactoEmergencia;
    private String mensajeEmergencia;
    private String telefonoApoyoAlternativo;

    // Constructores
    public UpdateUsuarioDto() {}

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

    public String getMensajeEmergencia() {
        return mensajeEmergencia;
    }

    public void setMensajeEmergencia(String mensajeEmergencia) {
        this.mensajeEmergencia = mensajeEmergencia;
    }

    public String getTelefonoApoyoAlternativo() {
        return telefonoApoyoAlternativo;
    }

    public void setTelefonoApoyoAlternativo(String telefonoApoyoAlternativo) {
        this.telefonoApoyoAlternativo = telefonoApoyoAlternativo;
    }
}
