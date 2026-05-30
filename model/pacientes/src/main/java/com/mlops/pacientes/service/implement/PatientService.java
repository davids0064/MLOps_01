package com.mlops.pacientes.service.implement;

import com.mlops.pacientes.dto.PredictionsReport;
import com.mlops.pacientes.dto.Request;
import com.mlops.pacientes.dto.Response;
import com.mlops.pacientes.jpa.entity.PatientEntity;
import com.mlops.pacientes.jpa.repository.PatientHabitRepository;
import com.mlops.pacientes.jpa.repository.PatientRepository;
import com.mlops.pacientes.service.IPatientService;
import com.mlops.pacientes.service.PredictionRegistryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class PatientService implements IPatientService {

    private final PatientRepository patientRepository;
    private final PatientHabitRepository patientHabitRepository;
    private final PredictionRegistryService predictionRegistryService;

    public List<Map<String, Integer>> predict(Request request) {
        Map<String, Integer> predictionCount = initializeCount();

        getFilteredPatients(request).stream()
                .map(this::buildResponse)
                .forEach(response -> {
                    predictionRegistryService.register(response);
                    predictionCount.merge(response.prediction(), 1, Integer::sum);
                });

        List<Map<String, Integer>> result = new ArrayList<>();
        predictionCount.forEach((prediction, count) -> result.add(Map.of(prediction, count)));
        return result;
    }

    public List<Response> predictionDetail(Request request) {
        return getFilteredPatients(request).stream()
                .map(this::buildResponse)
                .peek(predictionRegistryService::register)
                .toList();
    }

    public PredictionsReport predictionsReport() {
        return predictionRegistryService.generateReport();
    }

    private List<PatientEntity> getFilteredPatients(Request request) {
        List<PatientEntity> patients = new ArrayList<>();
        List<PatientEntity> allPatients = patientRepository.findAll();
        Logger.getLogger("Attempting to retrieve patients, total: " + allPatients.size());
        if (request.age() != null && request.age().size() >= 2) {
            patients = patientRepository.findByAgeBetween(request.age().get(0), request.age().get(1));
        }
        if (request.habits() != null && !request.habits().isEmpty()) {
            List<Long> idsByHabits = patientHabitRepository.findPatientIdsByHabits(request.habits());

            if (patients.isEmpty() && (request.age() == null || request.age().isEmpty())) {
                patients = patientRepository.findAllById(idsByHabits);
            } else {
                patients = patients.stream()
                        .filter(p -> idsByHabits.contains(p.getPatientId()))
                        .toList();
            }
        }

        if (patients.isEmpty()
                && (request.age() == null || request.age().isEmpty())
                && (request.habits() == null || request.habits().isEmpty())) {
            patients = allPatients;
        }

        return patients;
    }

    private List<String> getHabitNames(PatientEntity patient) {
        return patientHabitRepository.findByPatient(patient).stream()
                .map(hp -> hp.getHabit().getHabitName())
                .toList();
    }

    private Response buildResponse(PatientEntity patient) {
        return new Response(
                patient.getFirstName(),
                patient.getSecondName(),
                patient.getFirstLastName(),
                patient.getSecondLastName(),
                patient.getGender(),
                patient.getAge(),
                getHabitNames(patient),
                generatePrediction(patient)
        );
    }

    private Map<String, Integer> initializeCount() {
        Map<String, Integer> predictionCount = new LinkedHashMap<>();
        PredictionRegistryService.CATEGORIES.forEach(category -> predictionCount.put(category, 0));
        return predictionCount;
    }

    private String generatePrediction(PatientEntity patient) {
        int count = patientHabitRepository.findByPatient(patient).stream()
                .filter(hp -> !hp.getHabit().isGood())
                .toList()
                .size();
        if (count == 0) return "NOT ILL";
        if (count == 1) return "MILD ILLNESS";
        if (count == 2) return "ACUTE ILLNESS";
        if (count >= 3 && patient.getAge() >= 70) return "TERMINAL ILLNESS";
        if (count == 3) return "CHRONIC ILLNESS";
        return "TERMINAL ILLNESS";
    }
}
