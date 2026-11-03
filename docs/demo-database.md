# Demo PostgreSQL — план первого вертикального среза

**Назначение:** проверить на практике целостность, конкурентность и восстановление D&D Combat Tracker. Не пытаться сразу реализовать весь игровой движок. Это **логический черновик**, не готовый SQL/DDL.

## Предлагаемые таблицы

| Таблица | PK | Главное содержимое | Связи / ограничения |
| --- | --- | --- | --- |
| `combats` | `id UUID` | name, status, round_number, current_combatant_id, turn_sequence, turn_state, version, created/started/finished_at | status допустим; version ≥ 0; текущий участник из того же боя |
| `combatants` | `id UUID` | combat_id, character_id nullable, name, type, death_policy, initiative_total, turn_position, max/current/temporary_hp, life_state, save_successes/failures, surprised, turn_start_count, turn_end_count | FK combat_id; UNIQUE(combat_id, turn_position) для назначенных позиций; HP и счётчики в диапазоне |
| `condition_instances` | `id UUID` | combatant_id, condition_type, source, status, applied context, optional time expiry (expiration_combatant_id, START/END, target_occurrence), optional conditional expiry | FK к владельцу; ссылки на время только в рамках того же Combat; несколько однотипных активных экземпляров разрешены |
| `pending_resolutions` **(гипотеза)** | `id UUID` | combat_id, combatant_id, turn_sequence, kind, source reference, status, result | FK того же Combat; уникальность логической обязательной проверки; результат переживает рестарт |
| `combat_events` | `id UUID` | combat_id, command_id, sequence_number, event_type, event_version, payload JSONB, round/turn context, occurred_at | UNIQUE(combat_id, sequence_number); append-only на уровне приложения |
| `processed_commands` | `command_id UUID` | combat_id (для CreateCombat может появиться в рамках операции), command_type, request_fingerprint, result/status, created/completed_at | ID глобально уникален; согласованность отпечатка и результата; одна транзакция со state/events |

`TurnQueue`, `GameMoment`, `HitPoints`, `DeathSaveState` — объекты домена, **не отдельные таблицы**. Отдельной `initiative_entries` нет.

### Уточнения перед физическим DDL

- FK `combats.current_combatant_id` не должен разрешать ссылку на чужой бой. Из-за циклической зависимости `combats ↔ combatants` продумать порядок создания, nullable до старта или отложенную проверку; **не отключать FK**.
- `turn_position` nullable до старта; непрерывность порядка и полноту готовности к StartCombat обеспечивает домен, UNIQUE — PostgreSQL.
- `condition_instances` должна поддержать **время И/ИЛИ условие**; целевой счётчик относится к START либо END конкретного `expiration_combatant_id`. Если вариант с nullable-полями станет запутанным, изменить схему по результатам demo.
- Не делать автоматический каскадный delete Combat → events/processed_commands: завершённый бой и аудит сохраняются. Для MVP предпочтительно запрещать физическое удаление исторического Combat.
- Не хранить пароли, глобальные листы персонажей, классовые характеристики и произвольные SQL-триггеры правил D&D.

## Минимальные индексы

1. Участники по `combat_id` и уникальность позиции внутри боя.
2. Состояния по владельцу и активности.
3. События по `combat_id, sequence_number`.
4. Уникальность `processed_commands.command_id`.
5. Неразрешённые проверки по `combat_id, turn_sequence, status` (при выборе таблицы).

## Проверочные сценарии (integration tests на настоящей PostgreSQL)

| # | Сценарий | Проверка |
| --- | --- | --- |
| 1 | Создать Combat и добавить 3 участников | FK, допустимые HP, nullable characterId |
| 2 | Назначить порядок и начать бой | Уникальная позиция, первый раунд, текущий участник |
| 3 | AdvanceTurn и переход через границу кольца | Верный currentCombatantId, roundNumber и turn counters |
| 4 | Погибший в очереди и DYING при 0 HP | DEAD пропускается без фиктивного старта хода; DYING получает death save |
| 5 | Нанести урон, стабилизировать и снова нанести урон | Согласованность HP/lifeState/save counters в одном commit |
| 6 | Два источника Poisoned | Истечение одного не удаляет другой |
| 7 | Истечение в END следующего хода источника | Счётчик именно нужного Combatant; если его ход не наступает — истечения нет |
| 8 | Прервать приложение на AWAITING_RESOLUTION и восстановить | Все pending-проверки и положение очереди восстановлены |
| 9 | Повторить commandId с теми же параметрами | Результат прежний, HP/очередь/события не дублируются |
| 10 | Два разных commandId одновременно меняют один Combat | Одна запись проходит, другая получает конфликт версии, нет lost update |
| 11 | Попытка ссылки на участника другого боя | PostgreSQL или доменная проверка отклоняет |
| 12 | Повторный запуск Liquibase на той же базе | ChangeSet не применяется повторно |

## Порядок реализации после принятия черновика

1. Один небольшой PR: **demo Liquibase schema**, PostgreSQL Testcontainers, тесты FK/CHECK/UNIQUE и повторного запуска миграции. Сначала доказать, что схема создаётся и ограничения работают.
2. Следующий PR: минимальная модель `Combat` + `Combatant` + `TurnQueue`, сохранение/восстановление порядка и переключение ходов.
3. Следующий PR: урон, death saves, состояния, pending-проверки, идемпотентность, журнал — небольшими вертикальными срезами, а не одной большой реализацией.
4. После наблюдения реального поведения пересмотреть поля, отказаться от ненужных таблиц и только тогда пометить схему v1 как согласованную.

**Не считается готовым:** пока нет миграции и интеграционных тестов. Этот документ описывает план проверки, а не реализованную функциональность.
