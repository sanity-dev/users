package com.sanity.dto;

import jakarta.validation.constraints.Email;

public class UpdatePersonaDto {
    private String nombre;
    
    @Email(message = "El correo debe ser válido")
    private String correo;
    
    private String contraseña;
    private String telefono;
    private String cedula;

    // Constructores
    public UpdatePersonaDto() {}

    public UpdatePersonaDto(String nombre, String correo, String contraseña, String telefono, String cedula) {
        this.nombre = nombre;
        this.correo = correo;
        this.contraseña = contraseña;
        this.telefono = telefono;
        this.cedula = cedula;
    }

    // Getters y Setters
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

    public String getContraseña() {
        return contraseña;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
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
}
