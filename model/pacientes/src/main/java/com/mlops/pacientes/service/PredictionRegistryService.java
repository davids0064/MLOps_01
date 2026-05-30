package com.mlops.pacientes.service;

import com.mlops.pacientes.dto.PredictionRecord;
import com.mlops.pacientes.dto.PredictionsReport;
import com.mlops.pacientes.dto.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PredictionRegistryService {

    public static final List<String> CATEGORIES = List.of(
            "NOT ILL",
            "MILD ILLNESS",
            "ACUTE ILLNESS",
            "CHRONIC ILLNESS",
            "TERMINAL ILLNESS"
    );

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final Path reportPath;

    public PredictionRegistryService(
            @Value("${patients.report.path:/tmp/patients/predictions.log}") String reportPath
    ) {
        this.reportPath = Path.of(reportPath);
    }

    public synchronized void register(Response response) {
        try {
            Path parent = reportPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            String line = String.join("|",
                    DATE_FORMAT.format(LocalDateTime.now()),
                    fullName(response),
                    response.gender(),
                    String.valueOf(response.age()),
                    response.prediction()
            );
            Files.writeString(
                    reportPath,
                    line + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            throw new IllegalStateException("Could not register the prediction in the report file.", e);
        }
    }

    public synchronized PredictionsReport generateReport() {
        List<PredictionRecord> predictions = readPredictions();
        Map<String, Integer> totalByCategory = initializeCount();

        predictions.forEach(prediction ->
                totalByCategory.merge(prediction.prediction(), 1, Integer::sum));

        int from = Math.max(predictions.size() - 5, 0);
        List<PredictionRecord> lastPredictions = predictions.subList(from, predictions.size());
        String lastPredictionDate = predictions.isEmpty()
                ? null
                : predictions.get(predictions.size() - 1).date();

        return new PredictionsReport(totalByCategory, lastPredictions, lastPredictionDate);
    }

    private List<PredictionRecord> readPredictions() {
        if (!Files.exists(reportPath)) {
            return List.of();
        }

        try {
            return Files.readAllLines(reportPath, StandardCharsets.UTF_8).stream()
                    .map(this::parseLine)
                    .toList();
        } catch (IOException e) {
            throw new IllegalStateException("Could not read the predictions report file.", e);
        }
    }

    private PredictionRecord parseLine(String line) {
        String[] fields = line.split("\\|", -1);
        if (fields.length < 5) {
            return new PredictionRecord("", "", "", null, "UNKNOWN");
        }
        return new PredictionRecord(
                fields[0],
                fields[1],
                fields[2],
                Integer.valueOf(fields[3]),
                fields[4]
        );
    }

    private Map<String, Integer> initializeCount() {
        Map<String, Integer> totalByCategory = new LinkedHashMap<>();
        CATEGORIES.forEach(category -> totalByCategory.put(category, 0));
        return totalByCategory;
    }

    private String fullName(Response response) {
        List<String> names = new ArrayList<>();
        names.add(response.firstName());
        names.add(response.secondName());
        names.add(response.firstLastName());
        names.add(response.secondLastName());
        return String.join(" ", names.stream()
                .filter(name -> name != null && !name.isBlank())
                .toList());
    }
}
