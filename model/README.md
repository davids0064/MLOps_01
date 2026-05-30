# Patient Health Prediction API

REST API built with Spring Boot that estimates a patient's disease level based on their registered lifestyle habits.

The service loads **563 synthetic patients** on startup (ages 18–90, mixed genders) and assigns each a set of habits from the catalog below. No external data source or trained model file is required.

---

## Prediction logic

The disease level is determined by counting how many **bad habits** a patient has:

| Bad habit count | Age condition | Prediction         |
|-----------------|---------------|--------------------|
| 0               | —             | `NOT ILL`          |
| 1               | —             | `MILD ILLNESS`     |
| 2               | —             | `ACUTE ILLNESS`    |
| 3               | < 70          | `CHRONIC ILLNESS`  |
| 3               | ≥ 70          | `TERMINAL ILLNESS` |
| ≥ 4             | —             | `TERMINAL ILLNESS` |

---

## Habit catalog

The following habit names are valid for the `habits` filter:

**Good habits**
- `Regular physical activity`
- `Balanced diet`
- `Sleep 7 to 8 hours`
- `Frequent hydration`
- `Preventive medical checkups`

**Bad habits**
- `Tobacco use`
- `Frequent alcohol consumption`
- `Sedentary lifestyle`
- `High sugar intake`
- `Poor sleep hygiene`

---

## Running the service

```bash
cd model/pacientes
docker compose up -d
```

The API is available at `http://localhost:8081`.  
Swagger UI: `http://localhost:8081/swagger-ui/index.html`

---

## Endpoints

Base path: `/api/health-model/predictions`

| Method | Path      | Description                                       |
|--------|-----------|---------------------------------------------------|
| `POST` | `/`       | Count of patients per disease level (summary)     |
| `POST` | `/detail` | Per-patient prediction with habits                |
| `GET`  | `/report` | Historical report: totals, last 5, last date      |

All `POST` endpoints accept the same optional request body:

```json
{
  "age": [minAge, maxAge],
  "habits": ["habit name", "..."]
}
```

Both filters are optional and combinable. Omitting both returns results for all patients.

---

## `POST /api/health-model/predictions` — Predictions summary

Returns a list where each entry maps a disease category to its patient count for the given filters.

**All patients (no filters)**
```bash
curl -s -X POST http://localhost:8081/api/health-model/predictions \
  -H "Content-Type: application/json" \
  -d '{}'
```

**Patients aged 40–60**
```bash
curl -s -X POST http://localhost:8081/api/health-model/predictions \
  -H "Content-Type: application/json" \
  -d '{"age": [40, 60]}'
```

**Patients with a specific bad habit**
```bash
curl -s -X POST http://localhost:8081/api/health-model/predictions \
  -H "Content-Type: application/json" \
  -d '{"habits": ["Tobacco use"]}'
```

**Combined filter: age range and habit**
```bash
curl -s -X POST http://localhost:8081/api/health-model/predictions \
  -H "Content-Type: application/json" \
  -d '{"age": [30, 50], "habits": ["Sedentary lifestyle", "High sugar intake"]}'
```

**Example response**
```json
[
  {"NOT ILL": 112},
  {"MILD ILLNESS": 114},
  {"ACUTE ILLNESS": 112},
  {"CHRONIC ILLNESS": 113},
  {"TERMINAL ILLNESS": 112}
]
```

---

## `POST /api/health-model/predictions/detail` — Per-patient prediction

Returns each patient that matches the filters along with their full profile, habit list, and individual prediction.

**All patients**
```bash
curl -s -X POST http://localhost:8081/api/health-model/predictions/detail \
  -H "Content-Type: application/json" \
  -d '{}'
```

**Elderly patients (≥ 70) with bad habits**
```bash
curl -s -X POST http://localhost:8081/api/health-model/predictions/detail \
  -H "Content-Type: application/json" \
  -d '{"age": [70, 90], "habits": ["Poor sleep hygiene"]}'
```

**Example response**
```json
[
  {
    "first_name": "Maria",
    "second_name": "Fernanda",
    "first_last_name": "Garcia",
    "second_last_name": "Jimenez",
    "gender": "Female",
    "age": 18,
    "habits": ["Regular physical activity", "Sleep 7 to 8 hours", "Preventive medical checkups"],
    "prediction": "NOT ILL"
  }
]
```

---

## `GET /api/health-model/predictions/report` — Historical report

Returns an accumulated report of all predictions registered across previous calls to the summary and detail endpoints.

```bash
curl -s http://localhost:8081/api/health-model/predictions/report
```

**Example response**
```json
{
  "total_by_category": {
    "NOT ILL": 224,
    "MILD ILLNESS": 228,
    "ACUTE ILLNESS": 224,
    "CHRONIC ILLNESS": 226,
    "TERMINAL ILLNESS": 224
  },
  "last_predictions": [
    {
      "date": "2026-05-29T14:32:01",
      "patient": "Carlos Alberto Garcia Jimenez",
      "gender": "Male",
      "age": 22,
      "prediction": "MILD ILLNESS"
    }
  ],
  "last_prediction_date": "2026-05-29T14:32:01"
}
```

> The report accumulates entries from every call to `POST /` and `POST /detail`. It persists across requests within the same container lifetime (file-backed log).
