# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

---

## [1.0.0] - 2026-05-25

### Added

**Unidad 1 — Punto 1: Pipeline ML (documentación)**
- Documento `unidad_1/punto1_pipeline/description.md` con la descripción completa del pipeline de ML end-to-end para predicción de enfermedades comunes y huérfanas.
- Propuesta de arquitectura por capas: modelos para datos abundantes (XGBoost, LightGBM, MLP) y datos escasos (few-shot learning, Siamese Networks, transfer learning).
- Descripción de estrategias de preprocesamiento, validación clínica y monitoreo de data/model drift.
- Flujo de re-entrenamiento continuo con aprendizaje activo y despliegue canario.

**Unidad 1 — Punto 2: Servicio de predicción (Spring Boot)**
- API REST (`com.mlops:pacientes:1.0.0`) construida con Spring Boot 3 y Java 21.
- Endpoint `POST /api/predecir`: resumen de predicciones por categoría de nivel de enfermedad.
- Endpoint `POST /api/predecir/detalle`: predicción individual por paciente a partir de hábitos registrados.
- Endpoint `GET /api/predecir/reporte`: historial acumulado de predicciones (totales por categoría, últimas 5, fecha de última predicción).
- Persistencia en base de datos H2 en memoria con Spring Data JPA.
- Documentación interactiva con Springdoc OpenAPI / Swagger UI en `/swagger-ui/index.html`.
- `Dockerfile` y `compose.yaml` para ejecución local contenedorizada (puerto 8081).

**CI/CD — GitHub Actions**
- Workflow `main-ci-cd.yml`: ejecuta pruebas unitarias con Maven y publica la imagen Docker en GitHub Container Registry (`ghcr.io`) ante cada push a `main`.
- Workflow `pr-comment.yml`: agrega comentario automático en cada Pull Request abierto contra `main`.
- Restricción de la rama `main` (branch protection rules) para requerir PR antes de integrar cambios.

**Repositorio**
- `README.md` principal con descripción del problema, estructura del repositorio, tecnologías y guía de ejecución rápida.
- Configuración inicial del repositorio y transferencia de propiedad para habilitar GitHub Actions.

---

## [2.0.0] - 2026-05-29

### Added

**Tests — Pruebas unitarias**
- `PatientServiceTest`: 15 pruebas unitarias con Mockito para `PatientService`, cubriendo los métodos `predict`, `predictionDetail` y `predictionsReport`, incluyendo los seis escenarios de la lógica de predicción: 0 malos hábitos → `NOT ILL`, 1 → `MILD ILLNESS`, 2 → `ACUTE ILLNESS`, 3 y edad < 70 → `CHRONIC ILLNESS`, 3 y edad ≥ 70 → `TERMINAL ILLNESS`, ≥ 4 → `TERMINAL ILLNESS`.
- `PredictionRegistryServiceTest`: 11 pruebas unitarias con `@TempDir` para `PredictionRegistryService`, cubriendo escritura y acumulación de entradas en el archivo de log, generación del reporte con conteos por categoría, ventana de últimas 5 predicciones, fecha de última predicción y parseo de líneas corruptas como `UNKNOWN`.

**Tests — Pruebas de integración**
- `PatientControllerIntegrationTest`: 11 pruebas de integración con `@SpringBootTest`, `MockMvc` y `@DynamicPropertySource` para aislar el archivo de log por ejecución. Cubre los tres endpoints (`POST /detail`, `POST /`, `GET /report`) con filtros por edad, hábitos, combinado y sin filtros. Verifica campos en formato `snake_case` y rango de edades en respuesta filtrada.

### Changed

**Estructura del repositorio**
- Directorio del servicio movido de `unidad_1/punto2_servicio/pacientes` a `model/pacientes`.

**Traducción completa al inglés**
- Clases renombradas: `PacienteEntity` → `PatientEntity`, `HabitoEntity` → `HabitEntity`, `HabitoPacienteEntity` → `PatientHabitEntity`, `PacienteController` → `PatientController`, `IPacienteService` → `IPatientService`, `PacienteService` → `PatientService`, `RegistroPrediccionesService` → `PredictionRegistryService`, `PrediccionRealizada` → `PredictionRecord`, `ReportePredicciones` → `PredictionsReport`, `PacientesApplication` → `PatientsApplication`.
- Atributos de entidades y DTOs renombrados al inglés: `primerNombre/segundoNombre/primerApellido/segundoApellido` → `firstName/secondName/firstLastName/secondLastName`, `edad` → `age`, `genero` → `gender`, `nombreHabito` → `habitName`, `bueno` → `good`.
- Métodos renombrados: `predecir` → `predict`, `detallePrediccion` → `predictionDetail`, `reportePredicciones` → `predictionsReport`, `generarPrediccion` → `generatePrediction`, entre otros.
- Datos iniciales de hábitos traducidos al inglés (p. ej., `"Actividad fisica regular"` → `"Regular physical activity"`).
- Género traducido: `"Femenino"` → `"Female"`, `"Masculino"` → `"Male"`.
- Categorías de predicción traducidas: `"NO ENFERMO"` → `"NOT ILL"`, `"ENFERMEDAD LEVE"` → `"MILD ILLNESS"`, `"ENFERMEDAD AGUDA"` → `"ACUTE ILLNESS"`, `"ENFERMEDAD CRÓNICA"` → `"CHRONIC ILLNESS"`, `"ENFERMEDAD TERMINAL"` → `"TERMINAL ILLNESS"`.
- Textos de Swagger/OpenAPI, mensajes de error y documentación del código traducidos al inglés.

**API — Formato de respuesta**
- Campos de respuesta JSON cambiados a `snake_case` mediante anotaciones `@JsonProperty` en todos los records DTO: `first_name`, `second_name`, `first_last_name`, `second_last_name`, `total_by_category`, `last_predictions`, `last_prediction_date`.
- Campos de request también anotados explícitamente: `age`, `habits`.
- Propiedad de configuración renombrada: `pacientes.reporte.path` → `patients.report.path` (variable de entorno `PATIENTS_REPORT_PATH`).

**CI/CD — GitHub Actions**
- Workflows `main-ci-cd.yml` y `pr-comment.yml` actualizados con la nueva ruta `model/pacientes` (antes `unidad_1/punto2_servicio/pacientes`).
- Versión de Java actualizada de `21` a `17` en ambos workflows.

### Fixed

- `pom.xml`: propiedad `<java.version>` corregida de `21` a `17` para alinearla con la versión disponible en el entorno de desarrollo y con la configuración explícita del compilador.

---

<!-- releases futuras se agregan encima de esta línea -->
