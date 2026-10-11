package com.houseease.homeservice.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${spring.datasource.url}")
    private String configuredUrl;

    @Value("${spring.datasource.username:postgres}")
    private String configuredUsername;

    @Value("${spring.datasource.password:postgres}")
    private String configuredPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();

        // Render and other cloud providers frequently provide DATABASE_URL in postgresql:// or postgres:// format
        String envDbUrl = System.getenv("DATABASE_URL");
        String activeUrl = (envDbUrl != null && !envDbUrl.isBlank()) ? envDbUrl : configuredUrl;

        if (activeUrl != null && (activeUrl.startsWith("postgres://") || activeUrl.startsWith("postgresql://"))) {
            try {
                URI uri = new URI(activeUrl);
                String userInfo = uri.getUserInfo();
                if (userInfo != null && userInfo.contains(":")) {
                    String[] parts = userInfo.split(":", 2);
                    dataSource.setUsername(parts[0]);
                    dataSource.setPassword(parts[1]);
                } else if (userInfo != null) {
                    dataSource.setUsername(userInfo);
                } else {
                    dataSource.setUsername(configuredUsername);
                    dataSource.setPassword(configuredPassword);
                }

                int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                String host = uri.getHost();
                String path = uri.getPath(); // e.g. /homeservice
                String query = uri.getQuery();
                String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path + (query != null ? "?" + query : "");

                log.info("Configured JDBC datasource from standard URI format: {}", host + ":" + port + path);
                dataSource.setJdbcUrl(jdbcUrl);
            } catch (Exception e) {
                log.warn("Failed to parse DATABASE_URL as URI, applying fallback: {}", e.getMessage());
                String fallbackJdbcUrl = activeUrl.replace("postgres://", "jdbc:postgresql://")
                                                  .replace("postgresql://", "jdbc:postgresql://");
                dataSource.setJdbcUrl(fallbackJdbcUrl);
                dataSource.setUsername(configuredUsername);
                dataSource.setPassword(configuredPassword);
            }
        } else {
            dataSource.setJdbcUrl(activeUrl);
            dataSource.setUsername(configuredUsername);
            dataSource.setPassword(configuredPassword);
        }

        dataSource.setDriverClassName("org.postgresql.Driver");
        return dataSource;
    }
}
