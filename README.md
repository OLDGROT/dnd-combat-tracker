# D&D Combat Tracker — backend bootstrap

Issue #1 establishes infrastructure for a future independent combat domain service within a DM tools ecosystem. This is one deployable modular monolith. It has no combat entities, game rules, business REST API, frontend, authentication, or distributed infrastructure.

## Prerequisites

- JDK 21 (`java -version`)
- Docker Engine with Docker Compose v2; the daemon must be running for both local PostgreSQL and automated tests
- Internet access to Maven Central and Docker Hub on the first build

Maven 3.9.11 is downloaded by the committed Maven Wrapper (SHA-256 verified); no global Maven installation is needed. Windows users can use `mvnw.cmd` instead of `./mvnw`.

## Start locally

From the repository root, in one shell:

```sh
# Generate a local password, or set DB_PASSWORD securely yourself.
export DB_PASSWORD="$(openssl rand -hex 24)"
docker compose up -d --wait postgres
./mvnw spring-boot:run
```

In another shell:

```sh
curl --fail http://localhost:8080/actuator/health
# {"status":"UP"}
```

Stop the application with Ctrl-C. Stop PostgreSQL with `docker compose down`. Data persists in the named volume. `docker compose down -v` permanently deletes local database data; use it only when you intentionally want a fresh database. If retaining the volume, retain the same password securely: changing `DB_PASSWORD` does not change an existing database user's password.

For a packaged application:

```sh
./mvnw --batch-mode --no-transfer-progress clean verify
# Keep DB_PASSWORD exported and PostgreSQL running.
java -jar target/dnd-combat-tracker-0.0.1-SNAPSHOT.jar
```

### Configuration

| Variable | Default | Purpose |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/dnd_combat_tracker` | JDBC URL |
| `DB_USERNAME` | `dnd` | Database user |
| `DB_PASSWORD` | Empty in the app; required by Compose | Supply a password securely; none is committed |
| `DB_PORT` | `5432` | Compose host port only; if changed, also set `DB_URL` |
| `SERVER_PORT` | `8080` | Application HTTP port |

Compose binds PostgreSQL to loopback only. Its database/user are `dnd_combat_tracker`/`dnd`. The application fails startup if the database is unavailable or credentials are wrong. Export variables for the Java process. Compose reads an optional ignored `.env`, but Java does not automatically load that file. Do not commit `.env` files or credentials. Local defaults are for development; use externally provisioned credentials and database settings for deployment.

## Tests and CI

```sh
./mvnw --batch-mode --no-transfer-progress clean verify
```

This compiles production and test code, runs JUnit Jupiter tests, and packages the executable JAR. Tests start a disposable PostgreSQL 17.7 container with generated credentials; they do not use the Compose database or require `DB_PASSWORD`. Docker is required: missing Docker is a test failure, not a silent skip.

The smoke suite checks application context startup against a fresh database, Flyway validation and empty schema history, an HTTP health response of `UP` without details, and non-exposure of other actuator endpoints. GitHub Actions runs the same build on pull requests targeting `main` and supports manual dispatch. No credentials are required by CI. Human review and merging remain the maintainer's responsibility.

## Design decisions

- Java 21 and released Spring Boot 4.1.2; Spring Boot manages compatible JDBC, validation, Flyway, and Testcontainers versions. JUnit Jupiter is explicitly pinned to 5.14.4 to honor Issue #1's JUnit 5 requirement (Boot 4 defaults to JUnit 6). The smoke suite starts and closes the real Spring application programmatically because Boot 4's SpringExtension requires JUnit 6.
- `io.github.oldgrot.dndcombattracker.bootstrap` contains application wiring. The root package is the component-scan boundary. Future capability modules should be siblings of `bootstrap`, with internal domain/application/infrastructure boundaries when needed. Empty speculative modules and multiple deployables are deliberately avoided.
- JDBC supplies the connection pool without introducing an ORM or speculative entities. PostgreSQL is used in development and tests to avoid in-memory database differences.
- Flyway runs on startup. There are no application migrations yet because there is no application schema. Flyway creates its own history table on a fresh database; missing migration locations are allowed explicitly. The first real schema change should add a versioned migration under `src/main/resources/db/migration`. No baseline migration or automatic baselining of existing schemas is needed.
- Only actuator health is enabled and exposed. Health includes the database check but never returns component details. Validation is available for future input boundaries without introducing business endpoints.
- No Kafka, Redis, Kubernetes, discovery, gateway, or authentication is included.

See [AGENTS.md](AGENTS.md) for development constraints and the PR template for review evidence.
