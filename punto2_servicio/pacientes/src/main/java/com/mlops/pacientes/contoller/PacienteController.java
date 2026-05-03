package com.mlops.pacientes.contoller;

import com.mlops.pacientes.service.IPacienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/paciente")
@RequiredArgsConstructor
public class PacienteController {

    private final IPacienteService iPacienteService;

    @GetMapping("test")
    public String test(){
        return "test";
    }

}
