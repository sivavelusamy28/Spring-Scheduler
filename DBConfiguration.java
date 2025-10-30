package com.example.batchapp.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class DBConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(DBConfiguration.class);
    
    private final PostgresDataSourceProperties postgresProperties;
    
    public DBConfiguration(PostgresDataSourceProperties postgresProperties) {
        this.postgresProperties = postgresProperties;
    }
    
    @Primary
    @Bean(name = "postgresDataSource", destroyMethod = "close")
    public HikariDataSource postgresDataSource() {
        logger.info("Initializing PostgreSQL HikariCP DataSource");
        
        HikariConfig config = new HikariConfig();
        
        // Basic connection properties
        config.setJdbcUrl(postgresProperties.getJdbcUrl());
        config.setUsername(postgresProperties.getUsername());
        config.setPassword(postgresProperties.getPassword());
        config.setDriverClassName(postgresProperties.getDriverClassName());
        
        // Hikari pool properties
        config.setMaximumPoolSize(postgresProperties.getHikari().getMaximumPoolSize());
        config.setMinimumIdle(postgresProperties.getHikari().getMinimumIdle());
        config.setConnectionTimeout(postgresProperties.getHikari().getConnectionTimeout());
        config.setIdleTimeout(postgresProperties.getHikari().getIdleTimeout());
        config.setMaxLifetime(postgresProperties.getHikari().getMaxLifetime());
        config.setPoolName(postgresProperties.getHikari().getPoolName());
        config.setAutoCommit(postgresProperties.getHikari().isAutoCommit());
        
        // Additional recommended settings
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        
        // Connection test query
        config.setConnectionTestQuery("SELECT 1");
        
        // Leak detection threshold (in milliseconds) - helps identify connection leaks
        config.setLeakDetectionThreshold(60000); // 60 seconds
        
        logger.info("PostgreSQL HikariCP configuration: Pool Name={}, Max Pool Size={}, Min Idle={}",
                config.getPoolName(), config.getMaximumPoolSize(), config.getMinimumIdle());
        
        HikariDataSource dataSource = new HikariDataSource(config);
        
        logger.info("PostgreSQL HikariCP DataSource initialized successfully");
        
        return dataSource;
    }
    
    @Primary
    @Bean(name = "postgresJdbcTemplate")
    public JdbcTemplate postgresJdbcTemplate(DataSource postgresDataSource) {
        logger.info("Creating PostgreSQL JdbcTemplate");
        return new JdbcTemplate(postgresDataSource);
    }
    
    @Primary
    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(DataSource postgresDataSource) {
        logger.info("Creating PostgreSQL TransactionManager");
        return new DataSourceTransactionManager(postgresDataSource);
    }
}
