package com.mlops.pacientes.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI patientsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Patient Prediction API")
                        .description("Service to query patients, habits and estimated disease levels.")
                        .version("1.0.0"));
    }
}
