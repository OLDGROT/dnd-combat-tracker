--liquibase formatted sql

--changeset oldgrot:001-create-combat-table
--preconditions onFail:MARK_RAN
CREATE TABLE Combats
(
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    round_number INTEGER NOT NULL DEFAULT 1,
    current_combatant_id UUID,

    version BIGINT NOT NULL DEFAULT 0,

    created_at TIMESTAMPTZ NOT NULL,
    started TIMESTAMPTZ NOT NULL,
    finished TIMESTAMPTZ
),

--rollback DROP TABLE combats;