package com.mlops.pacientes.service;

import com.mlops.pacientes.dto.PredictionRecord;
import com.mlops.pacientes.dto.PredictionsReport;
import com.mlops.pacientes.dto.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PredictionRegistryServiceTest {

    @TempDir
    Path tempDir;

    private PredictionRegistryService service() {
        return new PredictionRegistryService(tempDir.resolve("predictions.log").toString());
    }

    // ── register ──────────────────────────────────────────────────────────────

    @Test
    void register_createsFileAndWritesPipeDelimitedLine() throws IOException {
        PredictionRegistryService svc = service();

        svc.register(response("John", "Michael", "Smith", "Johnson", "Male", 35, "MILD ILLNESS"));

        Path logFile = tempDir.resolve("predictions.log");
        assertThat(logFile).exists();
        List<String> lines = Files.readAllLines(logFile, StandardCharsets.UTF_8);
        assertThat(lines).hasSize(1);
        assertThat(lines.get(0))
                .contains("John Michael Smith Johnson")
                .contains("Male")
                .contains("35")
                .contains("MILD ILLNESS");
    }

    @Test
    void register_appendsEachCallAsNewLine() throws IOException {
        PredictionRegistryService svc = service();

        svc.register(response("Alice", null, "Brown", null, "Female", 25, "NOT ILL"));
        svc.register(response("Bob", null, "White", null, "Male", 50, "MILD ILLNESS"));

        List<String> lines = Files.readAllLines(tempDir.resolve("predictions.log"), StandardCharsets.UTF_8);
        assertThat(lines).hasSize(2);
        assertThat(lines.get(0)).contains("Alice Brown");
        assertThat(lines.get(1)).contains("Bob White");
    }

    @Test
    void register_omitsNullNameParts_fromFullName() throws IOException {
        PredictionRegistryService svc = service();

        svc.register(response("Ana", null, "Lopez", null, "Female", 30, "NOT ILL"));

        String line = Files.readAllLines(tempDir.resolve("predictions.log"), StandardCharsets.UTF_8).get(0);
        assertThat(line).contains("Ana Lopez").doesNotContain("null");
    }

    // ── generateReport ────────────────────────────────────────────────────────

    @Test
    void generateReport_withNoLogFile_returnsAllCategoriesAtZeroAndNullDate() {
        PredictionRegistryService svc = new PredictionRegistryService(
                tempDir.resolve("nonexistent.log").toString());

        PredictionsReport report = svc.generateReport();

        assertThat(report.lastPredictions()).isEmpty();
        assertThat(report.lastPredictionDate()).isNull();
        PredictionRegistryService.CATEGORIES.forEach(cat ->
                assertThat(report.totalByCategory()).containsEntry(cat, 0));
    }

    @Test
    void generateReport_countsCategoriesCorrectly() {
        PredictionRegistryService svc = service();

        svc.register(response("NOT ILL"));
        svc.register(response("NOT ILL"));
        svc.register(response("NOT ILL"));
        svc.register(response("MILD ILLNESS"));
        svc.register(response("ACUTE ILLNESS"));

        PredictionsReport report = svc.generateReport();

        assertThat(report.totalByCategory())
                .containsEntry("NOT ILL", 3)
                .containsEntry("MILD ILLNESS", 1)
                .containsEntry("ACUTE ILLNESS", 1)
                .containsEntry("CHRONIC ILLNESS", 0)
                .containsEntry("TERMINAL ILLNESS", 0);
    }

    @Test
    void generateReport_returnsLastFive_whenMoreThanFiveExist() {
        PredictionRegistryService svc = service();
        for (int i = 0; i < 8; i++) {
            svc.register(response("NOT ILL"));
        }

        PredictionsReport report = svc.generateReport();

        assertThat(report.lastPredictions()).hasSize(5);
    }

    @Test
    void generateReport_returnsAll_whenFiveOrFewerExist() {
        PredictionRegistryService svc = service();
        svc.register(response("NOT ILL"));
        svc.register(response("MILD ILLNESS"));

        PredictionsReport report = svc.generateReport();

        assertThat(report.lastPredictions()).hasSize(2);
    }

    @Test
    void generateReport_setsLastPredictionDateFromLastEntry() {
        PredictionRegistryService svc = service();
        svc.register(response("NOT ILL"));

        PredictionsReport report = svc.generateReport();

        assertThat(report.lastPredictionDate()).isNotNull().isNotBlank();
    }

    @Test
    void generateReport_lastPredictionMatchesLastRegisteredEntry() {
        PredictionRegistryService svc = service();
        svc.register(response("NOT ILL"));
        svc.register(response("TERMINAL ILLNESS"));

        List<PredictionRecord> last = svc.generateReport().lastPredictions();

        assertThat(last.get(last.size() - 1).prediction()).isEqualTo("TERMINAL ILLNESS");
    }

    @Test
    void generateReport_preservesCategoryOrderFromDefinition() {
        PredictionRegistryService svc = service();
        svc.register(response("NOT ILL"));

        PredictionsReport report = svc.generateReport();

        List<String> keys = List.copyOf(report.totalByCategory().keySet());
        assertThat(keys).containsExactlyElementsOf(PredictionRegistryService.CATEGORIES);
    }

    @Test
    void generateReport_corruptLine_parsedWithUnknownPrediction() throws IOException {
        Path logFile = tempDir.resolve("predictions.log");
        Files.writeString(logFile, "incomplete|data\n", StandardCharsets.UTF_8);

        PredictionsReport report = service().generateReport();

        assertThat(report.lastPredictions()).hasSize(1);
        assertThat(report.lastPredictions().get(0).prediction()).isEqualTo("UNKNOWN");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Response response(String prediction) {
        return response("John", null, "Doe", null, "Male", 30, prediction);
    }

    private Response response(String firstName, String secondName, String firstLastName,
                               String secondLastName, String gender, Integer age, String prediction) {
        return new Response(firstName, secondName, firstLastName, secondLastName,
                gender, age, List.of(), prediction);
    }
}
