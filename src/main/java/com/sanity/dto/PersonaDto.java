package com.sanity.dto;

import com.sanity.model.TipoUsuario;

public class PersonaDto {
    private Integer idPersona;
    private String nombre;
    private String correo;
    private String telefono;
    private String cedula;
    private TipoUsuario tipoUsuario;
    
    // Campos de Usuario
    private String contactoEmergencia;
    private String telefonoContactoEmergencia;
    
    // Campos de Terapeuta
    private String tarjetaProfesional;

    // Constructores
    public PersonaDto() {}

    public PersonaDto(Integer idPersona, String nombre, String correo, String telefono, String cedula, 
                      TipoUsuario tipoUsuario, String contactoEmergencia, String telefonoContactoEmergencia, 
                      String tarjetaProfesional) {
        this.idPersona = idPersona;
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.cedula = cedula;
        this.tipoUsuario = tipoUsuario;
        this.contactoEmergencia = contactoEmergencia;
        this.telefonoContactoEmergencia = telefonoContactoEmergencia;
        this.tarjetaProfesional = tarjetaProfesional;
    }

    // Getters y Setters
    public Integer getIdPersona() {
        return idPersona;
    }

    public void setIdPersona(Integer idPersona) {
        this.idPersona = idPersona;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public TipoUsuario getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(TipoUsuario tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

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

    public String getTarjetaProfesional() {
        return tarjetaProfesional;
    }

    public void setTarjetaProfesional(String tarjetaProfesional) {
        this.tarjetaProfesional = tarjetaProfesional;
    }
}