package io.github.oldgrot.dndcombattracker.bootstrap;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.time.Duration;
import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class ApplicationSmokeTest {
    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17.7");

    private static ConfigurableApplicationContext context;
    private static int port;

    // Boot 4's SpringExtension requires JUnit 6. Start the real application directly
    // to retain Issue #1's JUnit 5 requirement without downgrading Spring Boot.
    @BeforeAll
    static void startApplication() {
        context = SpringApplication.run(CombatTrackerApplication.class,
                "--server.port=0",
                "--spring.datasource.url=" + POSTGRES.getJdbcUrl(),
                "--spring.datasource.username=" + POSTGRES.getUsername(),
                "--spring.datasource.password=" + POSTGRES.getPassword());
        port = context.getEnvironment().getRequiredProperty("local.server.port", Integer.class);
    }

    @AfterAll
    static void stopApplication() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    void contextStartsWithFreshPostgresAndFlyway() throws Exception {
        assertThat(context.getBean(CombatTrackerApplication.class)).isNotNull();
        Flyway flyway = context.getBean(Flyway.class);
        assertThat(flyway.validateWithResult().validationSuccessful).isTrue();
        assertThat(flyway.info().pending()).isEmpty();
        try (Connection connection = context.getBean(DataSource.class).getConnection();
             var statement = connection.createStatement();
             var rows = statement.executeQuery("select count(*) from flyway_schema_history")) {
            assertThat(rows.next()).isTrue();
            assertThat(rows.getInt(1)).isZero();
        }
    }

    @Test
    void healthReportsUpWithoutExposingDetails() throws Exception {
        var response = get("/actuator/health");
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("{\"status\":\"UP\"}");
    }

    @Test
    void otherActuatorEndpointsAreNotExposed() throws Exception {
        for (String endpoint : new String[]{"info", "env", "metrics", "beans"}) {
            assertThat(get("/actuator/" + endpoint).statusCode()).isEqualTo(404);
        }
    }

    private HttpResponse<String> get(String path) throws Exception {
        try (var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()) {
            return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                    .timeout(Duration.ofSeconds(5)).GET().build(), HttpResponse.BodyHandlers.ofString());
        }
    }
}
