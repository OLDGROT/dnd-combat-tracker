--liquibase formatted sql

--changeset oldgrot:002-create-combatant-table
--preconditions onFail:MARK_RAN
CREATE TABLE Combatants
(
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    initiative INTEGER NOT NULL,
    position_x INTEGER NOT NULL,
    position_y INTEGER not null,
    combat_id UUID NOT NULL,
    max_hp INTEGER NOT NULL ,
    current_hp INTEGER NOT NULL,
    temporary_hp INTEGER NOT NULL DEFAULT 0,
    surprised BOOLEAN DEFAULT FALSE
)

--rollback