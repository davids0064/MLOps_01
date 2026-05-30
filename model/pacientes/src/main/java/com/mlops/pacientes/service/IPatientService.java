package com.mlops.pacientes.service;

import com.mlops.pacientes.dto.PredictionsReport;
import com.mlops.pacientes.dto.Request;
import com.mlops.pacientes.dto.Response;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

public interface IPatientService {

    List<Map<String, Integer>> predict(@RequestBody @Validated Request request);
    List<Response> predictionDetail(Request request) throws Throwable;
    PredictionsReport predictionsReport();

}
