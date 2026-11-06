package io.github.oldgrot.dndcombattracker.bootstrap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.sql.DataSource;

import liquibase.integration.spring.SpringLiquibase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class DemoSchemaIntegrationTest {
    private static final String FOREIGN_KEY_VIOLATION = "23503";
    private static final String CHECK_VIOLATION = "23514";
    private static final String UNIQUE_VIOLATION = "23505";

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17.7");

    @Autowired
    private DataSource dataSource;

    @Autowired
    private SpringLiquibase liquibase;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @BeforeEach
    void clearDemoData() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.executeUpdate("delete from combatants");
            statement.executeUpdate("delete from combats");
        }
    }

    // Проверяет создание таблиц demo-схемы и всех ожидаемых ограничений Liquibase на чистой PostgreSQL.
    @Test
    void liquibaseCreatesExpectedTablesAndConstraints() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            assertThat(readFirstColumn(connection, """
                    select table_name
                    from information_schema.tables
                    where table_schema = 'public'
                      and table_name in ('combats', 'combatants')
                    """))
                    .containsExactlyInAnyOrder("combats", "combatants");

            assertThat(readFirstColumn(connection, """
                    select constraint_name
                    from information_schema.table_constraints
                    where table_schema = 'public'
                      and table_name in ('combats', 'combatants')
                    """))
                    .contains(
                            "combats_pkey",
                            "combatants_pkey",
                            "combatants_name_key",
                            "fk_combatants_combat",
                            "chk_combatants_max_hp",
                            "chk_combatants_current_hp",
                            "chk_combatants_temporary_hp",
                            "uq_combatants_position");
        }
    }

    // Проверяет сохранение корректных Combat и Combatant и чтение связанных данных через JDBC.
    @Test
    void validCombatAndCombatantCanBeCreatedAndRead() throws SQLException {
        UUID combatId = insertCombat("Тестовый бой");
        UUID combatantId = insertCombatant(combatId, "Воин", 18, 2, 3, 20, 15, 4);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     select c.name, cb.name, cb.current_hp, cb.temporary_hp
                     from combats c
                     join combatants cb on cb.combat_id = c.id
                     where c.id = ? and cb.id = ?
                     """)) {
            statement.setObject(1, combatId);
            statement.setObject(2, combatantId);
            try (ResultSet rows = statement.executeQuery()) {
                assertThat(rows.next()).isTrue();
                assertThat(rows.getString(1)).isEqualTo("Тестовый бой");
                assertThat(rows.getString(2)).isEqualTo("Воин");
                assertThat(rows.getInt(3)).isEqualTo(15);
                assertThat(rows.getInt(4)).isEqualTo(4);
            }
        }
    }

    // Проверяет отклонение Combatant со ссылкой на несуществующий Combat по SQLState внешнего ключа.
    @Test
    void foreignKeyRejectsUnknownCombat() {
        SQLException error = catchThrowableOfType(
                SQLException.class,
                () -> insertCombatant(UUID.randomUUID(), "Без боя", 10, 0, 0, 10, 10, 0));

        assertThat((Object) error).isNotNull();
        assertThat(error.getSQLState()).isEqualTo(FOREIGN_KEY_VIOLATION);
    }

    // Проверяет точные текущие CHECK-правила для max_hp, current_hp и temporary_hp по SQLState PostgreSQL.
    @Test
    void hpChecksRejectValuesOutsideCurrentMigrationRules() throws SQLException {
        UUID combatId = insertCombat("Проверка HP");

        assertCheckViolation(() -> insertCombatant(combatId, "Минус max", 10, 0, 0, -1, 0, 0));
        assertCheckViolation(() -> insertCombatant(combatId, "Минус current", 10, 1, 0, 10, -1, 0));
        assertCheckViolation(() -> insertCombatant(combatId, "Current выше max", 10, 2, 0, 10, 11, 0));
        assertCheckViolation(() -> insertCombatant(combatId, "Минус temporary", 10, 3, 0, 10, 10, -1));
    }

    // Проверяет допустимый граничный случай нулевых max_hp, current_hp и temporary_hp согласно текущей миграции.
    @Test
    void hpChecksAllowZeroBoundaryValues() throws SQLException {
        UUID combatId = insertCombat("Нулевые HP");

        UUID combatantId = insertCombatant(combatId, "Нулевой", 10, 0, 0, 0, 0, 0);

        assertThat(combatantId).isNotNull();
    }

    // Проверяет запрет одинаковых координат у двух Combatant внутри одного Combat по SQLState уникальности.
    @Test
    void uniquePositionRejectsSameCoordinatesInOneCombat() throws SQLException {
        UUID combatId = insertCombat("Один бой");
        insertCombatant(combatId, "Первый", 10, 4, 5, 10, 10, 0);

        SQLException error = catchThrowableOfType(
                SQLException.class,
                () -> insertCombatant(combatId, "Второй", 9, 4, 5, 12, 12, 0));

        assertThat((Object) error).isNotNull();
        assertThat(error.getSQLState()).isEqualTo(UNIQUE_VIOLATION);
    }

    // Проверяет допустимость одинаковых координат у Combatant, принадлежащих разным Combat.
    @Test
    void uniquePositionAllowsSameCoordinatesInDifferentCombats() throws SQLException {
        UUID firstCombatId = insertCombat("Первый бой");
        UUID secondCombatId = insertCombat("Второй бой");

        UUID firstCombatantId = insertCombatant(firstCombatId, "Первый участник", 10, 7, 8, 10, 10, 0);
        UUID secondCombatantId = insertCombatant(secondCombatId, "Второй участник", 10, 7, 8, 10, 10, 0);

        assertThat(firstCombatantId).isNotEqualTo(secondCombatantId);
    }

    // Проверяет, что повторный запуск Liquibase не добавляет повторные записи выполненных changesets.
    @Test
    void repeatedLiquibaseRunDoesNotReapplyChangeSets() throws Exception {
        int appliedBefore = countAppliedChangeSets();

        liquibase.afterPropertiesSet();

        assertThat(appliedBefore).isPositive();
        assertThat(countAppliedChangeSets()).isEqualTo(appliedBefore);
    }

    private UUID insertCombat(String name) throws SQLException {
        UUID id = UUID.randomUUID();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     insert into combats (id, name, created_at, started)
                     values (?, ?, now(), now())
                     """)) {
            statement.setObject(1, id);
            statement.setString(2, name);
            statement.executeUpdate();
        }
        return id;
    }

    private UUID insertCombatant(
            UUID combatId,
            String name,
            int initiative,
            int positionX,
            int positionY,
            int maxHp,
            int currentHp,
            int temporaryHp) throws SQLException {
        UUID id = UUID.randomUUID();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     insert into combatants (
                         id, name, initiative, position_x, position_y, combat_id,
                         max_hp, current_hp, temporary_hp, surprised
                     ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, false)
                     """)) {
            statement.setObject(1, id);
            statement.setString(2, name);
            statement.setInt(3, initiative);
            statement.setInt(4, positionX);
            statement.setInt(5, positionY);
            statement.setObject(6, combatId);
            statement.setInt(7, maxHp);
            statement.setInt(8, currentHp);
            statement.setInt(9, temporaryHp);
            statement.executeUpdate();
        }
        return id;
    }

    private void assertCheckViolation(SqlOperation operation) {
        SQLException error = catchThrowableOfType(SQLException.class, operation::run);
        assertThat((Object) error).isNotNull();
        assertThat(error.getSQLState()).isEqualTo(CHECK_VIOLATION);
    }

    private int countAppliedChangeSets() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             var statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("select count(*) from databasechangelog")) {
            assertThat(rows.next()).isTrue();
            return rows.getInt(1);
        }
    }

    private Set<String> readFirstColumn(Connection connection, String sql) throws SQLException {
        Set<String> values = new HashSet<>();
        try (var statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(sql)) {
            while (rows.next()) {
                values.add(rows.getString(1));
            }
        }
        return values;
    }

    @FunctionalInterface
    private interface SqlOperation {
        void run() throws SQLException;
    }
}
