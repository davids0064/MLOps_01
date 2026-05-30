package com.mlops.pacientes.contoller;

import com.mlops.pacientes.dto.PredictionsReport;
import com.mlops.pacientes.dto.Request;
import com.mlops.pacientes.dto.Response;
import com.mlops.pacientes.service.IPatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/health-model/predictions")
@RequiredArgsConstructor
@Tag(
        name = "Patient predictions",
        description = "Operations to query the estimated disease level of patients by age and habits."
)
public class PatientController {

    private final IPatientService patientService;

    @PostMapping("detail")
    @Operation(
            summary = "Query prediction detail by patient",
            description = "Returns patients that match the given filters along with their habits and individual prediction."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Optional filters by age range and habit names.",
            required = true,
            content = @Content(schema = @Schema(implementation = Request.class))
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Prediction detail generated successfully.",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = @Content)
    })
    public List<Response> predictionDetail(@RequestBody @Validated Request request) throws Throwable {
        return patientService.predictionDetail(request);
    }

    @PostMapping
    @Operation(
            summary = "Query predictions summary",
            description = "Returns the number of patients per disease level for the given filters."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Optional filters by age range and habit names.",
            required = true,
            content = @Content(schema = @Schema(implementation = Request.class))
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Predictions summary generated successfully.",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Map.class)))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = @Content)
    })
    public List<Map<String, Integer>> predict(@RequestBody @Validated Request request) {
        return patientService.predict(request);
    }

    @GetMapping("report")
    @Operation(
            summary = "Query historical predictions report",
            description = "Returns totals by category, the last 5 predictions and the date of the last registered prediction."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Predictions report generated successfully.",
                    content = @Content(schema = @Schema(implementation = PredictionsReport.class))
            )
    })
    public PredictionsReport predictionsReport() {
        return patientService.predictionsReport();
    }
}
