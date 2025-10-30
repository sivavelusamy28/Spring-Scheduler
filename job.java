package com.example.batchapp.batch.reader;

import com.example.batchapp.model.ProcessTrackerDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Configuration
public class ProcessTrackerReaderConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(ProcessTrackerReaderConfig.class);
    
    private final ProcessTrackerRowMapper rowMapper;
    
    public ProcessTrackerReaderConfig(ProcessTrackerRowMapper rowMapper) {
        this.rowMapper = rowMapper;
    }
    
    @Bean
    @StepScope
    public JdbcCursorItemReader<ProcessTrackerDTO> ProcessTrackerReader(
            @Qualifier("postgresDataSource") DataSource postgresDataSource,
            @Value("${batch.reader.timestamp1-threshold-seconds:30}") int thresholdSeconds,
            @Value("${batch.reader.fetch-size:1000}") int fetchSize) {
        
        logger.info("Initializing ProcessTrackerReader with timestamp1 threshold: {} seconds, fetch-size: {}", 
                   thresholdSeconds, fetchSize);
        
        LocalDateTime cutoffTime = LocalDateTime.now().minusSeconds(thresholdSeconds);
        logger.info("Reading records with timestamp1_ts < {}", cutoffTime);
        
        String sql = "SELECT account_nbr, " +
                     "       last_processed_transaction_ts, " +
                     "       process_ts, " +
                     "       timestamp1_ts " +
                     "FROM x " +
                     "WHERE timestamp1_ts < ? " +
                     "AND timestamp1_ts IS NOT NULL " +
                     "ORDER BY timestamp1_ts, account_nbr";
        
        return new JdbcCursorItemReaderBuilder<ProcessTrackerDTO>()
                .name("ProcessTrackerReader")
                .dataSource(postgresDataSource)
                .sql(sql)
                .preparedStatementSetter((ps) -> {
                    ps.setTimestamp(1, Timestamp.valueOf(cutoffTime));
                })
                .fetchSize(fetchSize)
                .rowMapper(rowMapper)  // Inject the row mapper
                .build();
    }
}
