package com.sanity.dto;

public class UpdateTerapeutaDto {
    private String tarjetaProfesional;
    private String certificadoTituloProfesional;
    private String certificadoExpLaboral;
    private String certificadoEspecializacionMaestria;

    // Constructores
    public UpdateTerapeutaDto() {}

    public UpdateTerapeutaDto(String nTarjetaProfesional, String certificadoTituloProfesional, 
                              String certificadoExpLaboral, String certificadoEspecializacionMaestria) {
        this.tarjetaProfesional = nTarjetaProfesional;
        this.certificadoTituloProfesional = certificadoTituloProfesional;
        this.certificadoExpLaboral = certificadoExpLaboral;
        this.certificadoEspecializacionMaestria = certificadoEspecializacionMaestria;
    }

    // Getters y Setters
    public String getTarjetaProfesional() {
        return tarjetaProfesional;
    }

    public void setTarjetaProfesional(String tarjetaProfesional) {
        this.tarjetaProfesional = tarjetaProfesional;
    }

    public String getCertificadoTituloProfesional() {
        return certificadoTituloProfesional;
    }

    public void setCertificadoTituloProfesional(String certificadoTituloProfesional) {
        this.certificadoTituloProfesional = certificadoTituloProfesional;
    }

    public String getCertificadoExpLaboral() {
        return certificadoExpLaboral;
    }

    public void setCertificadoExpLaboral(String certificadoExpLaboral) {
        this.certificadoExpLaboral = certificadoExpLaboral;
    }

    public String getCertificadoEspecializacionMaestria() {
        return certificadoEspecializacionMaestria;
    }

    public void setCertificadoEspecializacionMaestria(String certificadoEspecializacionMaestria) {
        this.certificadoEspecializacionMaestria = certificadoEspecializacionMaestria;
    }
}