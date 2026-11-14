package io.github.oldgrot.dndcombattracker.app.domain.exception;

public class InvalidCombatStateException extends RuntimeException {
    public InvalidCombatStateException(String message) {
        super(message);
    }
}
