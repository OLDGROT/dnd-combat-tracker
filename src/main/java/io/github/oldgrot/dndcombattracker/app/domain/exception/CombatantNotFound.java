package io.github.oldgrot.dndcombattracker.app.domain.exception;

public class CombatantNotFound extends RuntimeException {
    public CombatantNotFound(String message) {
        super(message);
    }
}
