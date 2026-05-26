# MLOps — Unidad 2: Pipeline de Predicción de Enfermedades

## Contexto del problema

En el campo de la medicina existe una gran cantidad de información sobre pacientes, sin embargo, para enfermedades **huérfanas** (poco comunes) los datos disponibles son escasos. Se requiere un sistema capaz de predecir, a partir de los síntomas de un paciente, si es posible que sufra alguna enfermedad, tanto para patologías comunes (datos abundantes) como huérfanas (datos limitados).

## Propósito del repositorio

Este repositorio contiene las entregas de la **Unidad 2** del curso de Machine Learning Operations (MLOps). El objetivo es proponer y documentar un pipeline de ML completo (end-to-end) para abordar el problema descrito, y complementarlo con un servicio funcional que ilustra conceptos de predicción y exposición de resultados mediante una API REST.

## Estructura del repositorio

```
precciones-mlops-U2/
├── punto1_pipeline/
│   └── description.md        # Descripción del pipeline ML end-to-end
└── punto2_servicio/
    └── pacientes/            # Servicio Spring Boot de predicción de pacientes
        ├── Dockerfile
        ├── compose.yaml
        ├── README.md         # Documentación del servicio y endpoints
        └── src/
```

## Puntos de la entrega

### Punto 1 — Pipeline ML

Documento que describe el pipeline completo de ML para predecir enfermedades comunes y huérfanas, cubriendo:

- **Diseño**: restricciones, limitaciones y tipos de datos
- **Desarrollo**: fuentes de datos, preprocesamiento, modelos propuestos (XGBoost, few-shot learning, transfer learning) y estrategias de validación
- **Producción**: despliegue con Docker + DockerHub + servicio cloud, monitoreo de data/model drift y flujo de re-entrenamiento continuo

> Ver: [`punto1_pipeline/description.md`](punto1_pipeline/description.md)

### Punto 2 — Servicio de predicción

API REST construida con **Spring Boot** que, dado un conjunto de pacientes con hábitos registrados, predice su nivel de enfermedad. Expone tres endpoints principales:

| Endpoint | Descripción |
|---|---|
| `POST /api/predecir` | Resumen de predicciones por categoría |
| `POST /api/predecir/detalle` | Predicción individual por paciente |
| `GET /api/predecir/reporte` | Historial acumulado de predicciones |

El servicio está contenedorizado y puede levantarse localmente con Docker Compose.

> Ver: [`punto2_servicio/pacientes/README.md`](punto2_servicio/pacientes/README.md)

## Tecnologías utilizadas

| Componente | Tecnología |
|---|---|
| Servicio backend | Spring Boot 3, Spring Data JPA |
| Base de datos | H2 (en memoria) |
| Documentación API | Springdoc OpenAPI / Swagger |
| Contenedorización | Docker / Docker Compose |

## Ejecución rápida (Punto 2)

```bash
cd punto2_servicio/pacientes
docker compose up -d
```

Swagger UI disponible en: `http://localhost:8081/swagger-ui/index.html`



## Nota
Inicialmente la colaboración al repositorio entre los integrantes del team se realizó mediante un fork al repositorio.
<img width="1607" height="877" alt="image" src="https://github.com/user-attachments/assets/ce9e55a0-6739-43ad-a7c2-2a1276d8296b" />
