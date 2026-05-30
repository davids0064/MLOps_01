package com.mlops.pacientes.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Response(
        @JsonProperty("first_name") String firstName,
        @JsonProperty("second_name") String secondName,
        @JsonProperty("first_last_name") String firstLastName,
        @JsonProperty("second_last_name") String secondLastName,
        @JsonProperty("gender") String gender,
        @JsonProperty("age") Integer age,
        @JsonProperty("habits") List<String> habits,
        @JsonProperty("prediction") String prediction
) {
}
