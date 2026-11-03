# Development guidelines

- Пишите сообщения коммитов, заголовки и описания PR, а также комментарии к PR и коммитам на русском языке.
- Use Java 21 and the committed Maven Wrapper. Run `./mvnw -B clean verify` before a PR; Docker must be running for Testcontainers. Do not silently skip tests.
- Read the issue and acceptance criteria first. Limit changes to the requested issue.
- Work in a dedicated feature branch, open a PR targeting `main`, and leave merging to a human. Never push directly to `main`.
- Keep one deployable modular monolith. Organize future business modules by capability beneath `io.github.oldgrot.dndcombattracker`, with internal domain/application/infrastructure boundaries as needed. Bootstrap wiring belongs in `bootstrap`.
- Do not introduce speculative domain classes, game rules, frontend, authentication, messaging, caches, or distributed infrastructure.
- Keep configuration in environment variables; never commit credentials or local `.env` files. Expose only actuator health without diagnostic details.
- Test real behavior and database integration; do not substitute an in-memory database for PostgreSQL or disable Liquibase to pass tests.
- Update README when commands or configuration change. PRs must state design choices, actual checks and limitations.
