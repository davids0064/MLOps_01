package com.mlops.pacientes.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record PredictionsReport(
        @JsonProperty("total_by_category") Map<String, Integer> totalByCategory,
        @JsonProperty("last_predictions") List<PredictionRecord> lastPredictions,
        @JsonProperty("last_prediction_date") String lastPredictionDate
) {
}
