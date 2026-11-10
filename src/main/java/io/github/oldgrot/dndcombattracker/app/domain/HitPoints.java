package io.github.oldgrot.dndcombattracker.app.domain;

import lombok.Getter;

public record HitPoints(
        int max,
        int current,
        int temporary
) {
    public HitPoints {
        if (max <= 0) {
            throw new IllegalArgumentException("Max HP must be positive");
        }

        if (current < 0 || current > max) {
            throw new IllegalArgumentException("Invalid current HP");
        }

        if (temporary < 0) {
            throw new IllegalArgumentException("Invalid temporary HP");
        }
    }
}
