# LUCY Content LMS

Java Spring service for week 1-2 of LUCY: accept uploaded Word curriculum files, digitize their lesson levels into PostgreSQL, and expose level-based LMS APIs.

Authentication/authorization:

- The service self-verifies LUCY access tokens with the same HS256 shared secret used by Identity/Gateway/Realtime.
- `POST /api/imports/docx` requires role `Pro` or `Super`.
- Read-only curriculum endpoints remain public for the current frontend flow.
- `X-User-*` headers from the Gateway are not trusted as an authorization source; JWT claims are the source of truth.

## Architecture

The codebase is organized by the `curriculum` business module:

- `curriculum.api`: REST controllers and HTTP boundary.
- `curriculum.application`: use cases and orchestration services.
- `curriculum.application.dto`: response DTOs returned by the API.
- `curriculum.domain.model`: JPA-backed curriculum entities and enums.
- `curriculum.infrastructure.docx`: DOCX parsing and source-file extraction.
- `curriculum.infrastructure.persistence`: Spring Data repositories.

The API layer calls application services. Application services coordinate persistence and DOCX infrastructure, keeping controllers free of repository logic.

The files under `doc/` are sample fixtures only. Runtime data is expected to come from user uploads through the import API, not from bundled source-code files.

## Run locally

```powershell
docker compose up -d postgres
mvn spring-boot:run
```

The service uses PostgreSQL by default. The included `docker-compose.yml` starts a local PostgreSQL instance for development.
Secrets and local runtime settings can be stored in `.env`; this file is ignored by git. To load it in PowerShell before running the app:

```powershell
Get-Content .env | ForEach-Object {
  if ($_ -and $_ -notmatch '^#') {
    $key, $value = $_ -split '=', 2
    [Environment]::SetEnvironmentVariable($key, $value, 'Process')
  }
}
mvn spring-boot:run
```

Useful URLs:

- `GET http://localhost:8080/api/health`
- `GET http://localhost:8080/api/languages`
- `GET http://localhost:8080/api/documents`
- `GET http://localhost:8080/api/levels?language=ENGLISH`
- `GET http://localhost:8080/api/level-content?language=JAPANESE&level=61`
- `POST http://localhost:8080/api/imports/docx` with multipart field `files`
- `http://localhost:8080/swagger-ui.html`

Example DOCX upload:

```powershell
curl.exe -X POST http://localhost:8080/api/imports/docx `
  -F "files=@doc/Chinese - level 1-30.docx" `
  -F "files=@doc/Eng - STAGE 1 (LEVELS 1-30).docx"
```

## Database

Default local database settings match `docker-compose.yml`:

Environment variables:

- `LUCY_DB_URL` defaults to `jdbc:postgresql://localhost:5432/lucy_lms`
- `LUCY_DB_USERNAME` defaults to `lucy`
- `LUCY_DB_PASSWORD` defaults to `lucy`
- `LUCY_JWT_ISSUER` defaults to `lucy.identity`
- `LUCY_JWT_AUDIENCE` defaults to `lucy.clients`
- `LUCY_JWT_SIGNING_KEY` must match Identity/Gateway/Realtime
- `LUCY_JPA_DDL_AUTO` defaults to `update`
- `LUCY_MAX_FILE_SIZE` defaults to `25MB`
- `LUCY_MAX_REQUEST_SIZE` defaults to `100MB`

For a real hosted database, set the first three variables before running:

```powershell
$env:LUCY_DB_URL="jdbc:postgresql://<host>:5432/<database>"
$env:LUCY_DB_USERNAME="<username>"
$env:LUCY_DB_PASSWORD="<password>"
mvn spring-boot:run
```

## Data model

- `source_documents`: one row per imported Word file.
- `lesson_levels`: one row per parsed level in a source document.
- `lesson_blocks`: ordered paragraph-level content for each level.

The importer is idempotent by file name: an uploaded file already present in `source_documents` is skipped.
