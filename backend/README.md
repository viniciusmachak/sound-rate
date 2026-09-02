# Soundrate Backend

This module contains the Spring Boot API for Soundrate.

## What The Backend Does

The API is responsible for:

- authenticating users with JWTs
- managing user profiles, avatars, and passwords
- storing ratings, reviews, likes, follows, and listen-later entries
- integrating with Deezer for album and artist metadata
- sending account-related emails

## Stack

- Java 21
- Spring Boot 3.5
- Spring Security
- Spring Data JPA / Hibernate
- PostgreSQL
- SendGrid
- Cloudinary
- Springdoc OpenAPI

## Profiles

The backend uses profile-specific configuration:

- `local`: local development, Swagger enabled, schema auto-update enabled
- `docker`: container runtime profile
- `test`: test profile using H2

Default profile:

- `local`

## Configuration Files

- `src/main/resources/application.properties`
  Base configuration shared by all environments
- `src/main/resources/application-local.properties`
  Local development overrides
- `src/main/resources/application-docker.properties`
  Docker runtime overrides
- `src/test/resources/application-test.properties`
  Test-only configuration

## Required Environment Variables

The backend expects these values to be available from the root `.env` file or the environment:

```env
POSTGRES_DB=sound_rate_db
POSTGRES_USER=sound_rate_user
POSTGRES_PASSWORD=change-me

JWT_SECRET=replace-with-a-long-random-secret
CLOUDINARY_URL=cloudinary://<key>:<secret>@<cloud-name>
SENDGRID_API_KEY=SG.xxxxx
```

## Local Development

### 1. Start PostgreSQL

The local backend profile expects PostgreSQL on `localhost:5433`. The Compose
database is internal-only by default, so publish its port explicitly when running
the backend outside Docker:

```bash
docker compose run --rm -p 5433:5432 db
```

### 2. Run the API

```bash
cd backend
sh mvnw spring-boot:run
```

The API will start on:

- `http://localhost:8080`

Swagger will be available at:

- `http://localhost:8080/swagger-ui/index.html`

## Docker Runtime

From the repository root:

```bash
docker compose up --build
```

In Docker:

- the backend runs with the `docker` profile
- the backend connects to the database at `db:5432`

## Common API Areas

The main route groups are:

- `/api/v1/auth`
  Register, login, forgot password, reset password
- `/api/v1/users`
  Public profiles, followers/following, current-user account updates
- `/api/v1/albums`
  Album details, album reviews, dashboard queries, album likes
- `/api/v1/artists`
  Artist details and artist albums
- `/api/v1/ratings`
  Album and track ratings for the authenticated user
- `/api/v1/reviews`
  Review creation, updates, deletion, and review likes
- `/api/v1/listen-later`
  Authenticated user listen-later list
- `/api/v1/search`
  Search endpoints

## Security Model

- public read endpoints are exposed for user profiles, albums, artists, and search
- write operations require a valid Bearer token
- ownership checks are enforced for user-owned resources such as reviews and account updates
- unauthenticated protected requests return `401`
- unauthorized ownership violations return `403`

## External Integrations

### Deezer

The backend fetches album and artist metadata from the Deezer API through `DeezerService`.

### Cloudinary

Avatar uploads are sent to Cloudinary through `StorageService`.

### SendGrid

The backend sends:

- welcome emails
- password reset emails
- account deletion emails

## Testing

Run the backend test suite with:

```bash
cd backend
sh mvnw test
```

The suite currently covers:

- service-layer mutations
- protected endpoint auth behavior
- auth flows
- validation and ownership rules
- error response sanitization

## Useful Commands

Run the app:

```bash
sh mvnw spring-boot:run
```

Run tests:

```bash
sh mvnw test
```

Package the jar:

```bash
sh mvnw package
```

Build the backend Docker image from the module directory:

```bash
docker build -t soundrate-backend .
```
