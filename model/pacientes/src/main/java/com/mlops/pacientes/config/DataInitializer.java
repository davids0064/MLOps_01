package com.mlops.pacientes.config;

import com.mlops.pacientes.jpa.entity.HabitEntity;
import com.mlops.pacientes.jpa.entity.PatientHabitEntity;
import com.mlops.pacientes.jpa.entity.PatientEntity;
import com.mlops.pacientes.jpa.repository.PatientHabitRepository;
import com.mlops.pacientes.jpa.repository.HabitRepository;
import com.mlops.pacientes.jpa.repository.PatientRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class DataInitializer {

    private static final int TOTAL_PATIENTS = 563;

    private static final String[] FIRST_NAMES_FEMALE = {
            "Maria", "Ana", "Laura", "Sofia", "Camila", "Valentina", "Isabella", "Paula", "Daniela", "Natalia"
    };

    private static final String[] SECOND_NAMES_FEMALE = {
            "Fernanda", "Patricia", "Carolina", "Alejandra", "Marcela", "Lucia", "Gabriela", "Elena", "Adriana", "Victoria"
    };

    private static final String[] FIRST_NAMES_MALE = {
            "Carlos", "Juan", "Luis", "Pedro", "Andres", "Jorge", "Diego", "Fernando", "Ricardo", "Miguel"
    };

    private static final String[] SECOND_NAMES_MALE = {
            "Alberto", "David", "Enrique", "Felipe", "Sebastian", "Eduardo", "Rafael", "Antonio", "Mauricio", "Santiago"
    };

    private static final String[] FIRST_LAST_NAMES = {
            "Garcia", "Rodriguez", "Martinez", "Lopez", "Gonzalez", "Perez", "Sanchez", "Ramirez", "Torres", "Flores",
            "Rivera", "Gomez", "Diaz", "Vargas", "Morales", "Castro", "Ortiz", "Rojas", "Mendoza", "Herrera"
    };

    private static final String[] SECOND_LAST_NAMES = {
            "Jimenez", "Moreno", "Alvarez", "Romero", "Ruiz", "Navarro", "Medina", "Cruz", "Reyes", "Silva",
            "Paredes", "Campos", "Vega", "Molina", "Aguilar", "Cortes", "Leon", "Guerrero", "Salazar", "Pena"
    };

    @Bean
    CommandLineRunner loadInitialData(
            PatientRepository patientRepository,
            HabitRepository habitRepository,
            PatientHabitRepository patientHabitRepository
    ) {
        return args -> {
            if (patientRepository.count() > 0 || habitRepository.count() > 0) {
                return;
            }

            List<HabitEntity> habits = habitRepository.saveAll(createHabits());
            List<PatientEntity> patients = patientRepository.saveAll(createPatients());
            patientHabitRepository.saveAll(createRelationships(patients, habits));
        };
    }

    private List<HabitEntity> createHabits() {
        List<HabitEntity> habits = new ArrayList<>();
        habits.add(createHabit("Regular physical activity", true));
        habits.add(createHabit("Balanced diet", true));
        habits.add(createHabit("Sleep 7 to 8 hours", true));
        habits.add(createHabit("Frequent hydration", true));
        habits.add(createHabit("Preventive medical checkups", true));
        habits.add(createHabit("Tobacco use", false));
        habits.add(createHabit("Frequent alcohol consumption", false));
        habits.add(createHabit("Sedentary lifestyle", false));
        habits.add(createHabit("High sugar intake", false));
        habits.add(createHabit("Poor sleep hygiene", false));
        return habits;
    }

    private HabitEntity createHabit(String name, boolean good) {
        HabitEntity habit = new HabitEntity();
        habit.setHabitName(name);
        habit.setGood(good);
        return habit;
    }

    private List<PatientEntity> createPatients() {
        List<PatientEntity> patients = new ArrayList<>();
        for (int i = 0; i < TOTAL_PATIENTS; i++) {
            boolean isFemale = i % 2 == 0;
            PatientEntity patient = new PatientEntity();
            patient.setFirstName(isFemale
                    ? FIRST_NAMES_FEMALE[i % FIRST_NAMES_FEMALE.length]
                    : FIRST_NAMES_MALE[i % FIRST_NAMES_MALE.length]);
            patient.setSecondName(isFemale
                    ? SECOND_NAMES_FEMALE[(i * 3) % SECOND_NAMES_FEMALE.length]
                    : SECOND_NAMES_MALE[(i * 3) % SECOND_NAMES_MALE.length]);
            patient.setFirstLastName(FIRST_LAST_NAMES[(i * 5) % FIRST_LAST_NAMES.length]);
            patient.setSecondLastName(SECOND_LAST_NAMES[(i * 7) % SECOND_LAST_NAMES.length]);
            patient.setAge(18 + (i * 4) % 73);
            patient.setGender(isFemale ? "Female" : "Male");
            patients.add(patient);
        }
        return patients;
    }

    private List<PatientHabitEntity> createRelationships(List<PatientEntity> patients, List<HabitEntity> habits) {
        List<PatientHabitEntity> relationships = new ArrayList<>();
        for (int i = 0; i < patients.size(); i++) {
            PatientEntity patient = patients.get(i);
            int habitProfile = i % 5;
            if (habitProfile == 0) {
                relationships.add(createRelationship(patient, habits.get(i % 5)));
                relationships.add(createRelationship(patient, habits.get((i + 2) % 5)));
                relationships.add(createRelationship(patient, habits.get((i + 4) % 5)));
            } else if (habitProfile == 1) {
                relationships.add(createRelationship(patient, habits.get(i % 5)));
                relationships.add(createRelationship(patient, habits.get((i + 2) % 5)));
                relationships.add(createRelationship(patient, habits.get(5 + (i + 4) % 5)));
            } else if (habitProfile == 2) {
                relationships.add(createRelationship(patient, habits.get(i % 5)));
                relationships.add(createRelationship(patient, habits.get(5 + (i + 2) % 5)));
                relationships.add(createRelationship(patient, habits.get(5 + (i + 4) % 5)));
            } else if (habitProfile == 3) {
                relationships.add(createRelationship(patient, habits.get(5 + i % 5)));
                relationships.add(createRelationship(patient, habits.get(5 + (i + 2) % 5)));
                relationships.add(createRelationship(patient, habits.get(5 + (i + 4) % 5)));
            } else {
                relationships.add(createRelationship(patient, habits.get(5 + i % 5)));
                relationships.add(createRelationship(patient, habits.get(5 + (i + 2) % 5)));
                relationships.add(createRelationship(patient, habits.get(5 + (i + 4) % 5)));
            }
        }
        return relationships;
    }

    private PatientHabitEntity createRelationship(PatientEntity patient, HabitEntity habit) {
        PatientHabitEntity relationship = new PatientHabitEntity();
        relationship.setPatient(patient);
        relationship.setHabit(habit);
        return relationship;
    }
}
