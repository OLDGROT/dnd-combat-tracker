--liquibase formatted sql

--changeset oldgrot:003-create-combat-combatants-fk
--preconditions onFail:MARK_RAN
ALTER TABLE combatants
    ADD CONSTRAINT fk_combatants_combat
    FOREIGN KEY (combat_id)
    REFERENCES combats(id)
    ON DELETE RESTRICT;

--rollback ALTER TABLE combatants DROP CONSTRAINT fk_combatants_combat;