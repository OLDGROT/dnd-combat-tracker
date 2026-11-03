package io.github.oldgrot.dndcombattracker.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "io.github.oldgrot.dndcombattracker")
public class CombatTrackerApplication {
    public static void main(String[] args) {
        SpringApplication.run(CombatTrackerApplication.class, args);
    }
}
