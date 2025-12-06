package com.sanity.dto;

public class AuthResponseDto {
    private String token;
    private String tipo = "Bearer";
    private PersonaDto persona;

    // Constructores
    public AuthResponseDto() {}

    public AuthResponseDto(String token, PersonaDto persona) {
        this.token = token;
        this.tipo = "Bearer";
        this.persona = persona;
    }

    public AuthResponseDto(String token, String tipo, PersonaDto persona) {
        this.token = token;
        this.tipo = tipo;
        this.persona = persona;
    }

    // Getters y Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public PersonaDto getPersona() {
        return persona;
    }

    public void setPersona(PersonaDto persona) {
        this.persona = persona;
    }
}
