package com.mlops.pacientes.contoller;

import com.mlops.pacientes.dto.Request;
import com.mlops.pacientes.dto.Response;
import com.mlops.pacientes.service.IPacienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/predecir")
@RequiredArgsConstructor
public class PacienteController {

    private IPacienteService iPacienteService;

    @PostMapping
    public List<Response> predecir(@Validated @RequestBody Request request) throws Throwable{
        return iPacienteService.predecir(request);
    }

}
