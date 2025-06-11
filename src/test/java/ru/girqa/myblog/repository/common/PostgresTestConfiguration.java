package ru.girqa.myblog.repository.common;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureJdbc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration
@AutoConfigureJdbc
public abstract class PostgresTestConfiguration {

    @Bean(destroyMethod = "stop")
    @ServiceConnection
    PostgreSQLContainer<?> postgreSQLContainer() {
        return new PostgreSQLContainer<>(
                "postgres:16-alpine"
        );
    }
}
