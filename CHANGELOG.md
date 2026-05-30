# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

---

## [1.0.0] - 2026-05-29

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

<!-- releases futuras se agregan encima de esta línea -->
