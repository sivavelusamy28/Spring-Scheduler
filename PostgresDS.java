package com.example.batchapp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "datasource.postgres")
public class PostgresDataSourceProperties {
    private String jdbcUrl;
    private String username;
    private String password;
    private String driverClassName;
    private HikariProperties hikari = new HikariProperties();
    
    @Data
    public static class HikariProperties {
        private int maximumPoolSize = 10;
        private int minimumIdle = 5;
        private long connectionTimeout = 30000;
        private long idleTimeout = 600000;
        private long maxLifetime = 1800000;
        private String poolName = "PostgresHikariPool";
        private boolean autoCommit = true;
    }
}
