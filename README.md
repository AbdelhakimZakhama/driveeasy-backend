# DriveEasy Backend

Car rental platform built as Spring Boot / Spring Cloud microservices.
Individual project, Licence Informatique et Multimédia (LIM3 B/C), module "Développement d'applications web et multimédia".

## Architecture

| Component | Technology | Port | Role |
|---|---|---|---|
| service-registry | Eureka Server | 8761 | Service discovery |
| config-server | Spring Cloud Config | 8888 | Central configuration (Git repo `driveeasy-config`) |
| vehicule-service | Spring Boot | 8081 | Fleet management |
| client-service | Spring Boot | 8082 | Customers and licence validity |
| location-service | Spring Boot + OpenFeign | 8083 | Rental lifecycle, pricing, orchestration |
| api-gateway | Spring Cloud Gateway | 8080 | Single entry point, routing, CORS, timing filter |
| zipkin | Docker `openzipkin/zipkin` | 9411 | Distributed tracing |

Clients only talk to the gateway (`/api/vehicules/**`, `/api/clients/**`, `/api/locations/**`).
location-service calls vehicule-service and client-service through Eureka (`lb://`).

## Prerequisites

- JDK 21
- Maven 3.9+
- Docker (for Zipkin)
- Git and access to the config repository: https://github.com/AbdelhakimZakhama/driveeasy-config (private)
- A GitHub personal access token with read access to that repository

## Configuration

Each service only contains its name and `spring.config.import=configserver:http://localhost:8888`.
Everything else (port, Eureka, Zipkin, business properties) is in the config repo, one file per service.

The config server reads the private repo with a token passed as an environment variable.
Never commit the token:

```bash
export GITHUB_TOKEN=<your token>
```

Services register in Eureka with `127.0.0.1` (`eureka.instance.ip-address`), which is intended for local development.

## Installation and launch

```bash
git clone https://github.com/AbdelhakimZakhama/driveeasy-backend.git
cd driveeasy-backend
export JAVA_HOME=/path/to/jdk-21
export GITHUB_TOKEN=<your token>
docker compose up -d          # Zipkin
```

Start each module in its own terminal, in this order, waiting for "Started ..." each time:

```bash
cd service-registry  && mvn spring-boot:run   # 1
cd config-server     && mvn spring-boot:run   # 2
cd vehicule-service  && mvn spring-boot:run   # 3
cd client-service    && mvn spring-boot:run   # 4
cd location-service  && mvn spring-boot:run   # 5
cd api-gateway       && mvn spring-boot:run   # 6
```

Wait about 30 seconds after the last start so Eureka caches refresh (a 503 from the gateway right after startup is normal).

Check:
- Eureka dashboard: http://localhost:8761 (5 services UP)
- Zipkin: http://localhost:9411
- Health: `http://localhost:808x/actuator/health`

## Data

No database: data lives in memory and is seeded at startup (reset at each restart).

- 6 vehicles (ids 1 to 6), all `DISPONIBLE`
- 5 clients: 1 Ben Ali (eligible), 2 Trabelsi (FIDELE), 3 Gharbi (VIP), 4 Jlassi (too young), 5 Mansouri (licence expired)

No authentication, so there are no test accounts.

## API (through the gateway, port 8080)

| Service | Endpoints |
|---|---|
| vehicules | `GET /api/vehicules` (filters: categorie, carburant, statut, prixMax), `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`, `PATCH /{id}/statut`, `GET /disponibles?dateDebut&dateFin` |
| clients | `GET /api/clients` (nom, cin, email), `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`, `GET /{id}/eligibilite`, `PATCH /{id}/categorie` |
| locations | `GET /api/locations` (paginated, filters), `GET /{id}` (enriched), `POST`, `POST /simulation`, `PATCH /{id}/depart`, `PATCH /{id}/retour`, `PATCH /{id}/annulation`, `GET /statistiques` |

HTTP codes used: 200, 201, 204, 400, 404, 409, 500 (and 503 when a downstream service is unavailable).

## Business rules

- Price = days x daily price x category coefficient (ECONOMIQUE 1.0, BERLINE 1.2, SUV 1.5) x (1 - loyalty discount: FIDELE 5 %, VIP 10 %)
- Late penalty = 1.5 x daily price x days late
- A client must be at least 21 and hold a valid licence
- A vehicle cannot be booked if in maintenance, rented, or already booked on an overlapping period
- Departure sets the vehicle to `LOUE`, return sets it back to `DISPONIBLE`; if a call fails, the previous state is restored
- FIDELE after 5 finished rentals, VIP after 15
- Unique fields: vehicle plate; client CIN, email and licence number

## Quick demo

```bash
curl -s localhost:8080/api/vehicules
curl -s localhost:8080/api/clients/1/eligibilite
curl -s -X POST localhost:8080/api/locations/simulation -H "Content-Type: application/json" \
  -d '{"clientId":2,"vehiculeId":3,"dateDebut":"2026-12-01","dateFinPrevue":"2026-12-04"}'
curl -s -X POST localhost:8080/api/locations -H "Content-Type: application/json" \
  -d '{"clientId":2,"vehiculeId":3,"dateDebut":"2026-12-01","dateFinPrevue":"2026-12-04"}'
curl -s -X PATCH localhost:8080/api/locations/1/depart
curl -s -X PATCH localhost:8080/api/locations/1/retour -H "Content-Type: application/json" \
  -d '{"kmRetour":12500,"dateRetour":"2026-12-06"}'
curl -s localhost:8080/api/locations/1
```

Every gateway response carries an `X-Response-Time-Ms` header (custom global filter).

## Limitations

- In-memory data, no persistence
- No authentication
- Vehicle mileage is not updated on return
- The Angular frontend is not included (bonus)