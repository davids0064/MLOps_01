package com.mlops.pacientes.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PredictionRecord(
        @JsonProperty("date") String date,
        @JsonProperty("patient") String patient,
        @JsonProperty("gender") String gender,
        @JsonProperty("age") Integer age,
        @JsonProperty("prediction") String prediction
) {
}
