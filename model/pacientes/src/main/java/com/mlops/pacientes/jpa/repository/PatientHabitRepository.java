package com.mlops.pacientes.jpa.repository;

import com.mlops.pacientes.jpa.entity.PatientHabitEntity;
import com.mlops.pacientes.jpa.entity.PatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientHabitRepository extends JpaRepository<PatientHabitEntity, Long> {

    @Query("SELECT hp.patient.patientId FROM PatientHabitEntity hp WHERE hp.habit.habitName IN :names")
    List<Long> findPatientIdsByHabits(@Param("names") List<String> names);

    List<PatientHabitEntity> findByPatient(PatientEntity patient);

}
