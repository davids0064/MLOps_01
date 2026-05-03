package com.mlops.pacientes.service.implement;

import com.mlops.pacientes.dto.Request;
import com.mlops.pacientes.dto.Response;
import com.mlops.pacientes.jpa.repository.HabitoPacienteRepository;
import com.mlops.pacientes.jpa.repository.HabitoRepository;
import com.mlops.pacientes.jpa.repository.PacienteRepository;
import com.mlops.pacientes.service.IPacienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PacienteService implements IPacienteService {

    private final PacienteRepository pacienteRepository;
    private final HabitoRepository habitoRepository;
    private final HabitoPacienteRepository habitoPacienteRepository;

    public Response predecir(Request request) {


    }

}
