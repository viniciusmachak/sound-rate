# Soundrate Frontend

This module contains the Angular single-page application for Soundrate.

## What The Frontend Does

The application is responsible for:

- authenticating users and maintaining client-side session state
- searching for users, albums, and artists
- displaying album, artist, and user profile pages
- managing ratings, reviews, likes, follows, and listen-later entries
- providing account settings, avatar upload, and password management flows
- playing Deezer track previews through the shared audio player

## Stack

- Angular 21 LTS
- TypeScript 5.9
- Angular Material and CDK
- RxJS
- CSS
- Karma and Jasmine
- Nginx for container delivery
- Node.js 24.19 LTS and npm 11.17 for development and builds

## Application Structure

The main frontend areas are:

- `src/app/pages`
  Route-level pages such as home, album details, artist details, user profiles, authentication, settings, and listen later
- `src/app/components`
  Reusable cards, lists, dialogs, ratings, layout, and audio player components
- `src/app/services`
  API access, authentication state, and audio playback
- `src/app/models`
  TypeScript models for API requests and responses
- `src/app/guards`
  Route protection for authenticated pages
- `src/app/interceptors`
  JWT attachment and authentication error handling
- `src/app/pipes`
  Shared presentation transformations

The application uses standalone Angular components and is bootstrapped from `src/main.ts`.

## Loading UI Strategy

The frontend uses loading indicators according to the shape and scope of the pending work:

- use `SkeletonLoaderComponent` for route content and collections whose final layout is known, such as album grids, profiles, album details, artist pages, listen-later content, and user lists
- use compact spinners inside buttons for form submissions and account operations; keep the surrounding form visible and disable the action while the request is pending
- use small inline spinners for compact, indeterminate operations such as header search and audio-player loading
- use optimistic updates for reversible social actions such as likes, follows, ratings, and listen-later changes, restoring the previous state and showing feedback if the API request fails
- avoid full-page spinners and avoid replacing already visible content during local actions
- skeleton animations respect the user's `prefers-reduced-motion` setting

The shared skeleton component supports `cards`, `detail`, `profile`, and `list` variants. New pages should reuse one of these variants before introducing another loading pattern.

## Configuration Files

- `angular.json`
  Angular build, development server, assets, styles, budgets, and test configuration
- `src/environments/environment.ts`
  Local development settings
- `src/environments/environment.prod.ts`
  Production settings selected during production builds
- `proxy.conf.json`
  Development proxy from `/api` to the backend at `http://localhost:8080`
- `nginx.conf`
  Container routing, API proxying, and SPA fallback configuration
- `.nvmrc`
  Node.js version used for local development

Both Angular environments use `/api/v1` as the API base URL.

## Local Development

### 1. Start The Backend

From the repository root, start the database and backend:

```bash
docker compose up db backend
```

Alternatively, follow the local development instructions in `../backend/README.md`.

The frontend expects the API on:

- `http://localhost:8080`

### 2. Install Dependencies

```bash
cd frontend
nvm use
npm ci
```

The supported toolchain is:

- Node.js `>=24.0.0 <25.0.0`
- npm `>=11.17.0 <12.0.0`

### 3. Run The Application

```bash
npm start
```

The Angular development server will start on:

- `http://localhost:4200`

Development requests under `/api` are proxied to the backend by `proxy.conf.json`.

## Docker Runtime

From the repository root:

```bash
docker compose up --build
```

In Docker:

- Angular is built with Node.js 24.19
- the production files are served by Nginx
- Nginx proxies `/api/v1` to `backend:8080`
- unknown frontend routes fall back to `index.html`
- the frontend is exposed at `http://localhost:4200`

## Application Routes

The main routes are:

- `/`
  Home dashboard and global search
- `/login`
  User login
- `/register`
  User registration
- `/forgot-password`
  Password reset request
- `/reset-password`
  Password reset completion
- `/user/:username`
  Public user profile
- `/album/:id`
  Album details, ratings, tracks, and reviews
- `/artist/:id`
  Artist details and albums
- `/settings`
  Authenticated account settings
- `/listen-later`
  Authenticated listen-later list
- `/about`
  Public information about Soundrate and its main features

## Authentication Model

- successful login and registration store the JWT and current user in `localStorage`
- `AuthService` exposes the current user as an RxJS observable
- the JWT interceptor adds the Bearer token to outgoing HTTP requests
- a `401` response received while a token is stored clears the local session and redirects to `/login`; a `403` response preserves the session
- the auth guard protects `/settings` and `/listen-later`

Backend authorization remains authoritative; frontend route guards only control client-side navigation.

## API Integration

`ApiService` communicates with the backend through the `/api/v1` base path. In local development, Angular proxies these requests to `localhost:8080`. In Docker, Nginx proxies them to the Compose `backend` service.

Album and artist metadata displayed by the frontend is supplied by the backend's Deezer integration. Avatar and account-email operations are also initiated through backend endpoints rather than direct browser integrations.

## Testing

Run the configured Karma test runner with:

```bash
npm test
```

Run it once in headless Chrome with:

```bash
npm test -- --watch=false --browsers=ChromeHeadless
```

The current regression suite covers:

- JWT attachment to API requests
- session invalidation on `401` responses
- session preservation on `403` responses
- authentication state requirements
- malformed stored-session recovery

## Useful Commands

Run the development server:

```bash
npm start
```

Create a production build:

```bash
npm run build
```

Create a development build and rebuild on changes:

```bash
npm run watch
```

Run tests:

```bash
npm test
```

Build the frontend Docker image from the module directory:

```bash
docker build -t soundrate-frontend .
```
