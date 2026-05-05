# Recipe Discovery Frontend

React + Vite frontend that consumes the existing Spring Boot API.

## Setup

- Backend is expected at `http://localhost:8080` by default.
- Override with `VITE_API_BASE_URL` if needed.

## Run (dev)

```powershell
npm install
npm run dev
```

## Build

```powershell
npm run build
npm run preview
```

## Smoke Check (optional)

Requires a valid JWT token. Set `AUTH_TOKEN` and optionally `API_BASE_URL`.

```powershell
$env:AUTH_TOKEN="<paste token>"
$env:API_BASE_URL="http://localhost:8080"
npm run smoke
```

## Notes

- Ingredient input uses a static name-to-ID list from the database seed.
- Inventory listing is not available in the current API; UI focuses on add/update/remove.
- Diet filtering is client-side and depends on recipe payload including `dietClassifications`.

