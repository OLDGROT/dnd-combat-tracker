package io.github.oldgrot.dndcombattracker.bootstrap;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.assertj.core.api.Assertions.assertThat;

class CombatTrackerApplicationTest {
    @Test
    void applicationScansProjectRootPackage() {
        var annotation = CombatTrackerApplication.class.getAnnotation(SpringBootApplication.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.scanBasePackages()).containsExactly("io.github.oldgrot.dndcombattracker");
    }
}
