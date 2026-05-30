package com.mlops.pacientes.jpa.repository;

import com.mlops.pacientes.jpa.entity.PatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientRepository extends JpaRepository<PatientEntity, Long> {

    List<PatientEntity> findByAgeBetween(Integer firstAge, Integer secondAge);

}
