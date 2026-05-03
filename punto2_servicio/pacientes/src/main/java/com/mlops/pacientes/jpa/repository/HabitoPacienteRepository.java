package com.mlops.pacientes.jpa.repository;

import com.mlops.pacientes.jpa.entity.HabitoPacienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HabitoPacienteRepository extends JpaRepository<HabitoPacienteEntity, Long> {
}
