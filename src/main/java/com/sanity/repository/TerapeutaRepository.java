package com.sanity.repository;

import com.sanity.model.Terapeuta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TerapeutaRepository extends JpaRepository<Terapeuta, Integer> {
    boolean existsByTarjetaProfesional(String tarjetaProfesional);
}