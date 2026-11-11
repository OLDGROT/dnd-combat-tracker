package io.github.oldgrot.dndcombattracker.bootstrap;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.time.Duration;
import javax.sql.DataSource;

import liquibase.integration.spring.SpringLiquibase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ApplicationSmokeIT {
    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17.7");

    @Autowired
    private ConfigurableApplicationContext context;

    @LocalServerPort
    private int port;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Test
    void contextStartsWithFreshPostgresAndLiquibase() throws Exception {
        assertThat(context.getBean(CombatTrackerApplication.class)).isNotNull();
        try (Connection connection = context.getBean(DataSource.class).getConnection();
             var statement = connection.createStatement();
             var rows = statement.executeQuery("""
                     select count(*), min(exectype)
                     from databasechangelog
                     where id = '000-bootstrap' and author = 'dnd-combat-tracker'
                     """)) {
            assertThat(rows.next()).isTrue();
            assertThat(rows.getInt(1)).isOne();
            assertThat(rows.getString(2)).isEqualTo("EXECUTED");
        }
    }

    @Test
    void repeatedLiquibaseRunDoesNotApplyBootstrapChangeSetAgain() throws Exception {
        context.getBean(SpringLiquibase.class).afterPropertiesSet();

        try (Connection connection = context.getBean(DataSource.class).getConnection();
             var statement = connection.createStatement();
             var rows = statement.executeQuery("""
                     select count(*)
                     from databasechangelog
                     where id = '000-bootstrap' and author = 'dnd-combat-tracker'
                     """)) {
            assertThat(rows.next()).isTrue();
            assertThat(rows.getInt(1)).isOne();
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
