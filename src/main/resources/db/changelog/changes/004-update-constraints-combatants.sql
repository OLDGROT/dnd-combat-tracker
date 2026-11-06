--liquibase formatted sql

--changeset oldgrot:004-update-constraints-combatants
--preconditions onFail:MARK_RAN
ALTER TABLE combatants
    ADD CONSTRAINT chk_combatants_max_hp
        CHECK (max_hp>=0);

ALTER TABLE combatants
    ADD CONSTRAINT chk_combatants_current_hp
        CHECK (current_hp BETWEEN 0 AND max_hp);

ALTER TABLE combatants
    ADD CONSTRAINT chk_combatants_temporary_hp
        CHECK (temporary_hp >= 0);

ALTER TABLE combatants
    ADD CONSTRAINT uq_combatants_position
        UNIQUE (combat_id, position_x, position_y);


--rollback ALTER TABLE combatants DROP CONSTRAINT uq_combatants_position;
--rollback ALTER TABLE combatants DROP CONSTRAINT chk_combatants_temporary_hp;
--rollback ALTER TABLE combatants DROP CONSTRAINT chk_combatants_current_hp;
--rollback ALTER TABLE combatants DROP CONSTRAINT chk_combatants_max_hp