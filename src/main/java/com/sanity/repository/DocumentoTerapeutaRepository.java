package com.sanity.repository;

import com.sanity.model.DocumentoTerapeuta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentoTerapeutaRepository extends JpaRepository<DocumentoTerapeuta, Long> {

    List<DocumentoTerapeuta> findByTerapeutaIdPersona(Integer idPersona);

    Optional<DocumentoTerapeuta> findByTerapeutaIdPersonaAndTipoDocumento(Integer idPersona, String tipoDocumento);
}
