package com.mlops.pacientes.service;

import com.mlops.pacientes.dto.PredictionsReport;
import com.mlops.pacientes.dto.Request;
import com.mlops.pacientes.dto.Response;
import com.mlops.pacientes.jpa.entity.HabitEntity;
import com.mlops.pacientes.jpa.entity.PatientEntity;
import com.mlops.pacientes.jpa.entity.PatientHabitEntity;
import com.mlops.pacientes.jpa.repository.PatientHabitRepository;
import com.mlops.pacientes.jpa.repository.PatientRepository;
import com.mlops.pacientes.service.implement.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;
    @Mock
    private PatientHabitRepository patientHabitRepository;
    @Mock
    private PredictionRegistryService predictionRegistryService;

    @InjectMocks
    private PatientService patientService;

    private PatientEntity patient;
    private PatientHabitEntity goodHabitRelation;
    private PatientHabitEntity badHabitRelation;

    @BeforeEach
    void setUp() {
        HabitEntity goodHabit = new HabitEntity();
        goodHabit.setHabitId(1L);
        goodHabit.setHabitName("Regular physical activity");
        goodHabit.setGood(true);

        HabitEntity badHabit = new HabitEntity();
        badHabit.setHabitId(2L);
        badHabit.setHabitName("Tobacco use");
        badHabit.setGood(false);

        patient = new PatientEntity();
        patient.setPatientId(1L);
        patient.setFirstName("John");
        patient.setSecondName("Michael");
        patient.setFirstLastName("Smith");
        patient.setSecondLastName("Johnson");
        patient.setAge(35);
        patient.setGender("Male");

        goodHabitRelation = new PatientHabitEntity();
        goodHabitRelation.setPatient(patient);
        goodHabitRelation.setHabit(goodHabit);

        badHabitRelation = new PatientHabitEntity();
        badHabitRelation.setPatient(patient);
        badHabitRelation.setHabit(badHabit);
    }

    // ── predict ───────────────────────────────────────────────────────────────

    @Test
    void predict_withNoFilters_returnsAllPatientsGroupedByCategory() {
        when(patientRepository.findAll()).thenReturn(List.of(patient));
        when(patientHabitRepository.findByPatient(patient)).thenReturn(List.of(goodHabitRelation));

        List<Map<String, Integer>> result = patientService.predict(new Request(null, null));

        assertThat(result).hasSize(PredictionRegistryService.CATEGORIES.size());
        int notIllCount = result.stream()
                .filter(m -> m.containsKey("NOT ILL"))
                .mapToInt(m -> m.get("NOT ILL"))
                .sum();
        assertThat(notIllCount).isEqualTo(1);
        verify(predictionRegistryService).register(any(Response.class));
    }

    @Test
    void predict_withAgeFilter_queriesRepositoryByAgeRange() {
        when(patientRepository.findAll()).thenReturn(List.of(patient));
        when(patientRepository.findByAgeBetween(30, 40)).thenReturn(List.of(patient));
        when(patientHabitRepository.findByPatient(patient)).thenReturn(List.of(badHabitRelation));

        patientService.predict(new Request(List.of(30, 40), null));

        verify(patientRepository).findByAgeBetween(30, 40);
        verify(predictionRegistryService).register(any(Response.class));
    }

    @Test
    void predict_withHabitsFilter_queriesRepositoryByHabitIds() {
        when(patientRepository.findAll()).thenReturn(List.of(patient));
        when(patientHabitRepository.findPatientIdsByHabits(List.of("Tobacco use"))).thenReturn(List.of(1L));
        when(patientRepository.findAllById(List.of(1L))).thenReturn(List.of(patient));
        when(patientHabitRepository.findByPatient(patient)).thenReturn(List.of(badHabitRelation));

        patientService.predict(new Request(null, List.of("Tobacco use")));

        verify(patientHabitRepository).findPatientIdsByHabits(List.of("Tobacco use"));
        verify(patientRepository).findAllById(List.of(1L));
    }

    @Test
    void predict_withAgeAndHabitsFilter_returnsIntersectionOfBothFilters() {
        when(patientRepository.findAll()).thenReturn(List.of(patient));
        when(patientRepository.findByAgeBetween(30, 40)).thenReturn(List.of(patient));
        when(patientHabitRepository.findPatientIdsByHabits(List.of("Tobacco use"))).thenReturn(List.of(1L));
        when(patientHabitRepository.findByPatient(patient)).thenReturn(List.of(badHabitRelation));

        List<Map<String, Integer>> result = patientService.predict(
                new Request(List.of(30, 40), List.of("Tobacco use")));

        assertThat(result).isNotEmpty();
        verify(patientRepository).findByAgeBetween(30, 40);
        verify(patientHabitRepository).findPatientIdsByHabits(List.of("Tobacco use"));
    }

    @Test
    void predict_withHabitsMatchingNoPatient_returnsZeroCountsAndNoRegistration() {
        when(patientRepository.findAll()).thenReturn(List.of(patient));
        when(patientHabitRepository.findPatientIdsByHabits(List.of("Unknown habit"))).thenReturn(List.of());
        when(patientRepository.findAllById(List.of())).thenReturn(List.of());

        List<Map<String, Integer>> result = patientService.predict(
                new Request(null, List.of("Unknown habit")));

        int total = result.stream().mapToInt(m -> m.values().iterator().next()).sum();
        assertThat(total).isZero();
        verify(predictionRegistryService, never()).register(any());
    }

    // ── predictionDetail ──────────────────────────────────────────────────────

    @Test
    void predictionDetail_withNoFilters_returnsFullyMappedResponse() {
        when(patientRepository.findAll()).thenReturn(List.of(patient));
        when(patientHabitRepository.findByPatient(patient)).thenReturn(List.of(goodHabitRelation));

        List<Response> result = patientService.predictionDetail(new Request(null, null));

        assertThat(result).hasSize(1);
        Response r = result.get(0);
        assertThat(r.firstName()).isEqualTo("John");
        assertThat(r.secondName()).isEqualTo("Michael");
        assertThat(r.firstLastName()).isEqualTo("Smith");
        assertThat(r.secondLastName()).isEqualTo("Johnson");
        assertThat(r.age()).isEqualTo(35);
        assertThat(r.gender()).isEqualTo("Male");
        assertThat(r.habits()).containsExactly("Regular physical activity");
        assertThat(r.prediction()).isEqualTo("NOT ILL");
        verify(predictionRegistryService).register(r);
    }

    @Test
    void predictionDetail_withAgeFilter_returnsOnlyMatchingPatients() {
        PatientEntity youngPatient = new PatientEntity();
        youngPatient.setPatientId(2L);
        youngPatient.setFirstName("Alice");
        youngPatient.setSecondName(null);
        youngPatient.setFirstLastName("Brown");
        youngPatient.setSecondLastName(null);
        youngPatient.setAge(22);
        youngPatient.setGender("Female");

        when(patientRepository.findAll()).thenReturn(List.of(patient, youngPatient));
        when(patientRepository.findByAgeBetween(20, 30)).thenReturn(List.of(youngPatient));
        when(patientHabitRepository.findByPatient(youngPatient)).thenReturn(List.of());

        List<Response> result = patientService.predictionDetail(new Request(List.of(20, 30), null));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).age()).isEqualTo(22);
    }

    @Test
    void predictionDetail_withHabitsFilter_returnsOnlyMatchingPatients() {
        when(patientRepository.findAll()).thenReturn(List.of(patient));
        when(patientHabitRepository.findPatientIdsByHabits(List.of("Tobacco use"))).thenReturn(List.of(1L));
        when(patientRepository.findAllById(List.of(1L))).thenReturn(List.of(patient));
        when(patientHabitRepository.findByPatient(patient)).thenReturn(List.of(badHabitRelation));

        List<Response> result = patientService.predictionDetail(
                new Request(null, List.of("Tobacco use")));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).habits()).containsExactly("Tobacco use");
    }

    // ── predictionsReport ────────────────────────────────────────────────────

    @Test
    void predictionsReport_delegatesToRegistryAndReturnsItsResult() {
        PredictionsReport expected = new PredictionsReport(Map.of(), List.of(), null);
        when(predictionRegistryService.generateReport()).thenReturn(expected);

        PredictionsReport result = patientService.predictionsReport();

        assertThat(result).isSameAs(expected);
        verify(predictionRegistryService).generateReport();
    }

    // ── generatePrediction (tested via predictionDetail) ─────────────────────

    @Test
    void generatePrediction_zeroBadHabits_returnsNotIll() {
        when(patientRepository.findAll()).thenReturn(List.of(patient));
        when(patientHabitRepository.findByPatient(patient)).thenReturn(List.of(goodHabitRelation));

        String prediction = patientService.predictionDetail(new Request(null, null)).get(0).prediction();

        assertThat(prediction).isEqualTo("NOT ILL");
    }

    @Test
    void generatePrediction_oneBadHabit_returnsMildIllness() {
        when(patientRepository.findAll()).thenReturn(List.of(patient));
        when(patientHabitRepository.findByPatient(patient)).thenReturn(List.of(badHabitRelation));

        String prediction = patientService.predictionDetail(new Request(null, null)).get(0).prediction();

        assertThat(prediction).isEqualTo("MILD ILLNESS");
    }

    @Test
    void generatePrediction_twoBadHabits_returnsAcuteIllness() {
        when(patientRepository.findAll()).thenReturn(List.of(patient));
        when(patientHabitRepository.findByPatient(patient)).thenReturn(buildBadHabitRelations(patient, 2));

        String prediction = patientService.predictionDetail(new Request(null, null)).get(0).prediction();

        assertThat(prediction).isEqualTo("ACUTE ILLNESS");
    }

    @Test
    void generatePrediction_threeBadHabitsAgeLessThan70_returnsChronicIllness() {
        patient.setAge(60);
        when(patientRepository.findAll()).thenReturn(List.of(patient));
        when(patientHabitRepository.findByPatient(patient)).thenReturn(buildBadHabitRelations(patient, 3));

        String prediction = patientService.predictionDetail(new Request(null, null)).get(0).prediction();

        assertThat(prediction).isEqualTo("CHRONIC ILLNESS");
    }

    @Test
    void generatePrediction_threeBadHabitsAgeExactly70_returnsTerminalIllness() {
        patient.setAge(70);
        when(patientRepository.findAll()).thenReturn(List.of(patient));
        when(patientHabitRepository.findByPatient(patient)).thenReturn(buildBadHabitRelations(patient, 3));

        String prediction = patientService.predictionDetail(new Request(null, null)).get(0).prediction();

        assertThat(prediction).isEqualTo("TERMINAL ILLNESS");
    }

    @Test
    void generatePrediction_fourOrMoreBadHabits_returnsTerminalIllness() {
        patient.setAge(40);
        when(patientRepository.findAll()).thenReturn(List.of(patient));
        when(patientHabitRepository.findByPatient(patient)).thenReturn(buildBadHabitRelations(patient, 4));

        String prediction = patientService.predictionDetail(new Request(null, null)).get(0).prediction();

        assertThat(prediction).isEqualTo("TERMINAL ILLNESS");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private List<PatientHabitEntity> buildBadHabitRelations(PatientEntity patient, int count) {
        List<PatientHabitEntity> relations = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            HabitEntity habit = new HabitEntity();
            habit.setHabitId((long) (100 + i));
            habit.setHabitName("Bad habit " + i);
            habit.setGood(false);
            PatientHabitEntity rel = new PatientHabitEntity();
            rel.setPatient(patient);
            rel.setHabit(habit);
            relations.add(rel);
        }
        return relations;
    }
}
