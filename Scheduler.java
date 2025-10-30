package com.example.batchapp.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@ConditionalOnProperty(
    name = "scheduler.enabled", 
    havingValue = "true", 
    matchIfMissing = true
)
public class AccountProcessorScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(AccountProcessorScheduler.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Scheduled(cron = "${scheduler.cron:*/30 * * * * ?}")
    public void processAccounts() {
        logger.info("===== Scheduler triggered at: {} =====", 
                    LocalDateTime.now().format(formatter));
        
        // Your business logic will go here
        
        logger.info("===== Scheduler execution completed =====");
    }
}