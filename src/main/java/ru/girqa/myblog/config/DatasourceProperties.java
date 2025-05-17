package ru.girqa.myblog.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class DatasourceProperties {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${spring.datasource.hikari.connection-timeout: 30000}")
    private int connectionTimeout;

    @Value("${spring.datasource.hikari.maximum-pool-size: 10}")
    private int maximumPoolSize;

    @Value("${spring.datasource.hikari.idle-timeout: 600000}")
    private int idleTimeout;

}
