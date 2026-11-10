package io.github.oldgrot.dndcombattracker.app.domain;

public record HitPoints(
        int max,
        int current,
        int temporary
) {
    public HitPoints {
        if (max <= 0) {
            throw new IllegalArgumentException("Max HP must be greater than zero");
        }

        if (current < 0 || current > max) {
            throw new IllegalArgumentException("Current HP must be between zero and max HP");
        }

        if (temporary < 0) {
            throw new IllegalArgumentException("Temporary HP cannot be negative");
        }
    }
}
