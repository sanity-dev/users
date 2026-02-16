package com.sanity.dto;

import com.sanity.model.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RegisterDto {
    
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe ser válido")
    private String correo;
    
    @NotBlank(message = "La contraseña es obligatoria")
    private String contraseña;
    
    private String telefono;
    
    @NotBlank(message = "La cédula es obligatoria")
    private String cedula;
    
    @NotNull(message = "El tipo de usuario es obligatorio")
    private TipoUsuario tipoUsuario;

    // Constructores
    public RegisterDto() {}

    public RegisterDto(String nombre, String correo, String contraseña, String telefono, String cedula, TipoUsuario tipoUsuario) {
        this.nombre = nombre;
        this.correo = correo;
        this.contraseña = contraseña;
        this.telefono = telefono;
        this.cedula = cedula;
        this.tipoUsuario = tipoUsuario;
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

    public TipoUsuario getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(TipoUsuario tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }
}
