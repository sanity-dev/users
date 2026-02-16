package com.sanity.repository;

import com.sanity.model.FichaProfesional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FichaProfesionalRepository extends JpaRepository<FichaProfesional, Integer> {
}