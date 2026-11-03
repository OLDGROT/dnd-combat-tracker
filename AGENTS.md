# Development guidelines

- Пишите сообщения коммитов, заголовки и описания PR, а также комментарии к PR и коммитам на русском языке.
- Use Java 21 and the committed Maven Wrapper. Run `./mvnw -B clean verify` before a PR; Docker must be running for Testcontainers. Do not silently skip tests.
- Read the issue and acceptance criteria first. Limit changes to the requested issue.
- Work in a dedicated feature branch, open a PR targeting `main`, and leave merging to a human. Never push directly to `main`.
- Keep one deployable modular monolith. Organize future business modules by capability beneath `io.github.oldgrot.dndcombattracker`, with internal domain/application/infrastructure boundaries as needed. Bootstrap wiring belongs in `bootstrap`.
- Do not introduce speculative domain classes, game rules, frontend, authentication, messaging, caches, or distributed infrastructure.
- Keep configuration in environment variables; never commit credentials or local `.env` files. Expose only actuator health without diagnostic details.
- Test real behavior and database integration; do not substitute an in-memory database for PostgreSQL or disable Flyway to pass tests.
- Update README when commands or configuration change. PRs must state design choices, actual checks and limitations.

## Проектные навыки

Проектные навыки — это повторно используемые инструкции, а не автономные агенты. Используйте навык, только когда пользователь явно называет его или просит выполнить соответствующий процесс. Не запускайте навыки автоматически. Одна Issue должна создавать одну сфокусированную ветку и один PR; решение о слиянии принимает человек после ревью.

- `implement-issue`: реализовать одну GitHub Issue, проверить результат и подготовить PR. Пример: `$implement-issue Выполни issue #12 и создай PR.`
- `test-feature`: проверить указанную ветку или PR и предоставить доказательства без изменения production-кода, если пользователь это отдельно не согласовал. Пример: `$test-feature Проверь PR #12, включая негативные и граничные сценарии.`
- `review-pr`: независимо проверить PR в режиме read-only. Пример: `$review-pr Проведи ревью PR #12.`

Не заявляйте об успешной проверке, если команда не запускалась или её результат не был получен. Указывайте команды, число тестов, ошибки, пропуски и ограничения среды. Навыки не разрешают merge, auto-merge, approve или прямой push в `main`.
