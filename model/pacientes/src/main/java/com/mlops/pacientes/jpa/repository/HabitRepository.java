package com.mlops.pacientes.jpa.repository;

import com.mlops.pacientes.jpa.entity.HabitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HabitRepository extends JpaRepository<HabitEntity, Long> {

    HabitEntity findByHabitName(String habitName);

}
