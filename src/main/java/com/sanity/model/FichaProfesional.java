package com.sanity.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ficha_profesional")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FichaProfesional {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ficha")
    private Integer idFicha;
    
    @OneToOne
    @JoinColumn(name = "id_terapeuta", nullable = false)
    private Terapeuta terapeuta;
    
    @Column(name = "certificado_titulo_profesional", nullable = false)
    private String certificadoTituloProfesional;
    
    @Column(name = "certificado_exp_laboral", nullable = false)
    private String certificadoExpLaboral;
    
    @Column(name = "certificado_especializacion_maestria")
    private String certificadoEspecializacionMaestria;
}