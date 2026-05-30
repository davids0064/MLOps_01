package com.mlops.pacientes.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Request(
        @JsonProperty("age") List<Integer> age,
        @JsonProperty("habits") List<String> habits
) {
}
