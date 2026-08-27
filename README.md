# Soundrate

Soundrate is a full-stack music discovery and review application. Users can register, manage a profile, rate albums and tracks, write reviews, follow other users, and maintain personal lists such as liked albums and listen-later items.

This repository is a monorepo with two application folders:

- `backend/`: Spring Boot REST API
- `frontend/`: Angular single-page application

## Stack

### Backend
- Java 21
- Spring Boot 3.5
- Spring Security with JWT
- Spring Data JPA / Hibernate
- PostgreSQL
- SendGrid
- Cloudinary
- Springdoc OpenAPI

### Frontend
- Angular 21 LTS
- TypeScript 5.9
- Angular Material
- RxJS
- Nginx for container delivery

### Infrastructure
- Docker
- Docker Compose

## Main Features

- JWT-based authentication with register, login, forgot-password, and reset-password flows
- User profile management, password changes, avatar upload, and soft account deletion
- Album and track ratings
- Album reviews with likes and ownership checks
- Social features including follows and album likes
- Deezer-backed album and artist data
- Search across users, albums, and artists

## Repository Layout

```text
.
├── backend/
│   ├── src/main/java/...
│   ├── src/main/resources/
│   └── README.md
├── frontend/
│   ├── src/
│   └── README.md
└── docker-compose.yml
```

## Prerequisites

- Java 21
- Node.js 24.19 LTS with npm 11.17
- Docker and Docker Compose

## Environment Variables

Create a root `.env` file before running the backend in Docker.

Example:

```env
POSTGRES_DB=sound_rate_db
POSTGRES_USER=sound_rate_user
POSTGRES_PASSWORD=change-me

JWT_SECRET=replace-with-a-long-random-secret

CLOUDINARY_URL=cloudinary://<key>:<secret>@<cloud-name>
SENDGRID_API_KEY=SG.xxxxx
```

Notes:

- The backend loads `.env` from the repository root in local development.
- `CLOUDINARY_URL` and `SENDGRID_API_KEY` are required if you use avatar upload or email flows.

## Running With Docker

The Compose file exposes three services:

- `db`: PostgreSQL on host port `5433`
- `backend`: API on `http://localhost:8080`
- `frontend`: UI on `http://localhost:4200`

Start the full stack:

```bash
docker compose up --build
```

Start only the database:

```bash
docker compose up db
```

## Running Locally

### Backend

```bash
cd backend
sh mvnw spring-boot:run
```

By default, the backend uses the `local` Spring profile.

Expected local ports:

- PostgreSQL: `localhost:5433`
- API: `localhost:8080`

### Frontend

```bash
cd frontend
npm ci
npm start
```

The Angular dev server runs on `http://localhost:4200` and proxies `/api/v1` to the backend.

## API Documentation

Swagger is enabled in the `local` profile and disabled by default elsewhere.

When the backend is running locally, the docs are available at:

- `http://localhost:8080/swagger-ui/index.html`

## Testing

Backend tests:

```bash
cd backend
sh mvnw test
```

The backend test suite uses H2 and includes unit and integration coverage for authentication, protected endpoints, validation, ownership rules, and error sanitization.

## Additional Documentation

- Backend details: [backend/README.md](backend/README.md)
- Frontend details: [frontend/README.md](frontend/README.md)
