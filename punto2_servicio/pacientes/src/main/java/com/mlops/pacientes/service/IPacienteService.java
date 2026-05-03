package com.mlops.pacientes.service;

import com.mlops.pacientes.dto.Request;
import com.mlops.pacientes.dto.Response;

import java.util.List;

public interface IPacienteService {

    List<Response> predecir(Request request) throws Throwable;

}
