package com.mlops.pacientes.service.implement;

import com.mlops.pacientes.dto.Request;
import com.mlops.pacientes.dto.Response;
import com.mlops.pacientes.jpa.entity.HabitoEntity;
import com.mlops.pacientes.jpa.entity.PacienteEntity;
import com.mlops.pacientes.jpa.repository.HabitoPacienteRepository;
import com.mlops.pacientes.jpa.repository.HabitoRepository;
import com.mlops.pacientes.jpa.repository.PacienteRepository;
import com.mlops.pacientes.service.IPacienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PacienteService implements IPacienteService {

    private final PacienteRepository pacienteRepository;
    private final HabitoPacienteRepository habitoPacienteRepository;

    public List<Response> predecir(Request request) {
        List<PacienteEntity> pacientes = new ArrayList<>();
        if (request.edad() != null && request.edad().size() >= 2) {
            pacientes = pacienteRepository.findByEdadBetween(request.edad().get(0), request.edad().get(1));
        }
        if (request.habitos() != null && !request.habitos().isEmpty()) {
            List<Long> idsPorHabitos = habitoPacienteRepository.findIdsPacientesByHabitos(request.habitos());

            if (pacientes.isEmpty() && (request.edad() == null || request.edad().isEmpty())) {
                pacientes = pacienteRepository.findAllById(idsPorHabitos);
            } else {
                pacientes = pacientes.stream()
                        .filter(p -> idsPorHabitos.contains(p.getIdPaciente()))
                        .toList();
            }
        }

        return pacientes.stream()
                .map(p -> new Response(
                        p.getPrimerNombre(),
                        p.getSegundoNombre(),
                        p.getPrimerApellido(),
                        p.getSegundoApellido(),
                        p.getGenero(),
                        p.getEdad(),
                        obtenerNombresHabitos(p),
                        generarPrediccion(p)
                ))
                .toList();
    }

    private List<String> obtenerNombresHabitos(PacienteEntity paciente) {
        return habitoPacienteRepository.findByPaciente(paciente).stream()
                .map(hp -> hp.getHabito().getNombreHabito())
                .toList();
    }

    private String generarPrediccion(PacienteEntity p) {
        int count = obtenerNombresHabitos(p).size();
        if (count == 0) return "NO ENFERMO";
        if (count == 1) return "ENFERMEDAD LEVE";
        if (count == 2) return "ENFERMEDAD AGUDA";
        return "ENFERMEDAD CRÓNICA";
    }
}