package io.github.oldgrot.dndcombattracker.app.domain.exception;

public class OperationNotFoundException extends RuntimeException {
    public OperationNotFoundException(String message) {
        super(message);
    }
}
