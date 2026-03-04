package com.sanity.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@PrimaryKeyJoinColumn(name = "id_usuario")
public class Usuario extends Persona {
    
    @Column(name = "contacto_emergencia", length = 100)
    private String contactoEmergencia;
    
    @Column(name = "telefono_contacto_emergencia", length = 100)
    private String telefonoContactoEmergencia;

    @Column(name = "mensaje_emergencia", length = 500)
    private String mensajeEmergencia;

    @Column(name = "telefono_apoyo_alternativo", length = 20)
    private String telefonoApoyoAlternativo;
}
