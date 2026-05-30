package com.mlops.pacientes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mlops.pacientes.dto.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PatientControllerIntegrationTest {

    @TempDir
    static Path tempDir;

    @DynamicPropertySource
    static void overrideReportPath(DynamicPropertyRegistry registry) {
        registry.add("patients.report.path",
                () -> tempDir.resolve("predictions.log").toString());
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE = "/api/health-model/predictions";

    // ── POST /predictions (summary) ───────────────────────────────────────────

    @Test
    void predict_withNoFilters_returns200WithExactlyFiveCategories() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    void predict_withAgeFilter_returns200WithFiveCategories() throws Exception {
        String body = objectMapper.writeValueAsString(new Request(List.of(20, 40), null));

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    void predict_withHabitsFilter_returns200WithFiveCategories() throws Exception {
        String body = objectMapper.writeValueAsString(new Request(null, List.of("Tobacco use")));

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    void predict_withCombinedFilters_returns200WithFiveCategories() throws Exception {
        String body = objectMapper.writeValueAsString(
                new Request(List.of(30, 50), List.of("Tobacco use")));

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
    }

    // ── POST /predictions/detail ──────────────────────────────────────────────

    @Test
    void predictionDetail_withNoFilters_returns200WithSnakeCaseResponseFields() throws Exception {
        mockMvc.perform(post(BASE + "/detail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].first_name").exists())
                .andExpect(jsonPath("$[0].second_name").exists())
                .andExpect(jsonPath("$[0].first_last_name").exists())
                .andExpect(jsonPath("$[0].second_last_name").exists())
                .andExpect(jsonPath("$[0].gender").exists())
                .andExpect(jsonPath("$[0].age").isNumber())
                .andExpect(jsonPath("$[0].habits").isArray())
                .andExpect(jsonPath("$[0].prediction").value(
                        anyOf(equalTo("NOT ILL"), equalTo("MILD ILLNESS"), equalTo("ACUTE ILLNESS"),
                                equalTo("CHRONIC ILLNESS"), equalTo("TERMINAL ILLNESS"))));
    }

    @Test
    void predictionDetail_withAgeFilter_returnsOnlyPatientsInAgeRange() throws Exception {
        String body = objectMapper.writeValueAsString(new Request(List.of(18, 25), null));

        mockMvc.perform(post(BASE + "/detail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].age",
                        everyItem(allOf(greaterThanOrEqualTo(18), lessThanOrEqualTo(25)))));
    }

    @Test
    void predictionDetail_withHabitsFilter_returnsOnlyMatchingPatients() throws Exception {
        String body = objectMapper.writeValueAsString(
                new Request(null, List.of("Regular physical activity")));

        mockMvc.perform(post(BASE + "/detail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void predictionDetail_withCombinedFilters_returnsIntersection() throws Exception {
        String body = objectMapper.writeValueAsString(
                new Request(List.of(30, 60), List.of("Tobacco use")));

        mockMvc.perform(post(BASE + "/detail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].age",
                        everyItem(allOf(greaterThanOrEqualTo(30), lessThanOrEqualTo(60)))));
    }

    @Test
    void predictionDetail_withNonExistentHabit_returnsEmptyList() throws Exception {
        String body = objectMapper.writeValueAsString(
                new Request(null, List.of("Nonexistent habit")));

        mockMvc.perform(post(BASE + "/detail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── GET /predictions/report ───────────────────────────────────────────────

    @Test
    void predictionsReport_afterPredictions_returns200WithFullReportStructure() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        mockMvc.perform(get(BASE + "/report"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_by_category").isMap())
                .andExpect(jsonPath("$.last_predictions").isArray())
                .andExpect(jsonPath("$.last_prediction_date").isNotEmpty())
                .andExpect(jsonPath("$.total_by_category['NOT ILL']").isNumber())
                .andExpect(jsonPath("$.total_by_category['MILD ILLNESS']").isNumber())
                .andExpect(jsonPath("$.total_by_category['ACUTE ILLNESS']").isNumber())
                .andExpect(jsonPath("$.total_by_category['CHRONIC ILLNESS']").isNumber())
                .andExpect(jsonPath("$.total_by_category['TERMINAL ILLNESS']").isNumber());
    }

    @Test
    void predictionsReport_lastPredictionsContainExpectedFields() throws Exception {
        mockMvc.perform(post(BASE + "/detail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        mockMvc.perform(get(BASE + "/report"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.last_predictions[0].date").exists())
                .andExpect(jsonPath("$.last_predictions[0].patient").exists())
                .andExpect(jsonPath("$.last_predictions[0].gender").exists())
                .andExpect(jsonPath("$.last_predictions[0].age").isNumber())
                .andExpect(jsonPath("$.last_predictions[0].prediction").exists());
    }
}
