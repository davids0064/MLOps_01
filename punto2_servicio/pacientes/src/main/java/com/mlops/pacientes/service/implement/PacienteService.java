package com.mlops.pacientes.service.implement;

import com.mlops.pacientes.dto.Request;
import com.mlops.pacientes.dto.Response;
import com.mlops.pacientes.jpa.entity.PacienteEntity;
import com.mlops.pacientes.jpa.repository.HabitoPacienteRepository;
import com.mlops.pacientes.jpa.repository.PacienteRepository;
import com.mlops.pacientes.service.IPacienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class PacienteService implements IPacienteService {

    private final PacienteRepository pacienteRepository;
    private final HabitoPacienteRepository habitoPacienteRepository;

    public List<Map<String, Integer>> predecir(Request request) {
        Map<String, Integer> conteoPorPrediccion = new LinkedHashMap<>();
        conteoPorPrediccion.put("NO ENFERMO", 0);
        conteoPorPrediccion.put("ENFERMEDAD LEVE", 0);
        conteoPorPrediccion.put("ENFERMEDAD AGUDA", 0);
        conteoPorPrediccion.put("ENFERMEDAD CRÓNICA", 0);

        obtenerPacientesFiltrados(request).stream()
                .map(this::generarPrediccion)
                .forEach(prediccion -> conteoPorPrediccion.merge(prediccion, 1, Integer::sum));

        List<Map<String, Integer>> resultado = new ArrayList<>();
        conteoPorPrediccion.forEach((prediccion, cantidad) -> resultado.add(Map.of(prediccion, cantidad)));
        return resultado;
    }

    public List<Response> detallePrediccion(Request request) {
        return obtenerPacientesFiltrados(request).stream()
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

    private List<PacienteEntity> obtenerPacientesFiltrados(Request request) {
        List<PacienteEntity> pacientes = new ArrayList<>();
        List<PacienteEntity> pacientesPorGenero = pacienteRepository.findAll();
        Logger.getLogger("Intentando recuperar artículos para IDs: " + pacientesPorGenero.size());
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

        if (pacientes.isEmpty()
                && (request.edad() == null || request.edad().isEmpty())
                && (request.habitos() == null || request.habitos().isEmpty())) {
            pacientes = pacientesPorGenero;
        }

        return pacientes;
    }

    private List<String> obtenerNombresHabitos(PacienteEntity paciente) {
        return habitoPacienteRepository.findByPaciente(paciente).stream()
                .map(hp -> hp.getHabito().getNombreHabito())
                .toList();
    }

    private String generarPrediccion(PacienteEntity p) {
        int count = habitoPacienteRepository.findByPaciente(p).stream()
                .filter(hp -> !hp.getHabito().isBueno())
                .toList()
                .size();
        if (count == 0) return "NO ENFERMO";
        if (count == 1) return "ENFERMEDAD LEVE";
        if (count == 2) return "ENFERMEDAD AGUDA";
        return "ENFERMEDAD CRÓNICA";
    }
}
