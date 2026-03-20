package com.sanity.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "terapeuta")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@PrimaryKeyJoinColumn(name = "id_terapeuta")
public class Terapeuta extends Persona {
    
    @Column(name = "n_tarjeta_profesional", unique = true, length = 50)
    private String tarjetaProfesional;
    
    @OneToOne(mappedBy = "terapeuta", cascade = CascadeType.ALL)
    private FichaProfesional fichaProfesional;
}