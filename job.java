package com.example.batchapp.config;

import com.example.batchapp.model.processTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchJobConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(BatchJobConfig.class);
    
    @Value("${batch.chunk-size:1000}")
    private int chunkSize;
    
    @Value("${batch.thread-pool-size:5}")
    private int threadPoolSize;
    
    /**
     * Define the main batch job
     * A Job is a container for Steps
     */
    @Bean
    public Job processJob(JobRepository jobRepository,
                                 Step processStep) {
        
        logger.info("Creating processJob");
        
        return new JobBuilder("processJob", jobRepository)
                .incrementer(new RunIdIncrementer())  // Adds unique parameter to each run
                .start(processStep)  // First step to execute
                .build();
    }
    
    /**
     * Define the step
     * A Step contains the Reader, Processor, and Writer
     */
    @Bean
    public Step processStep(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager,
                                   ItemReader<processTracker> processTrackerReader,
                                   ItemProcessor<processTracker, processTracker> processProcessor,
                                   ItemWriter<processTracker> processWriter,
                                   TaskExecutor batchTaskExecutor) {
        
        logger.info("Creating processStep with chunk-size: {} and thread-pool-size: {}", 
                   chunkSize, threadPoolSize);
        
        return new StepBuilder("processStep", jobRepository)
                .<processTracker, processTracker>chunk(chunkSize, transactionManager)
                .reader(processTrackerReader)
                .processor(processProcessor)
                .writer(processWriter)
                .taskExecutor(batchTaskExecutor)
                .throttleLimit(threadPoolSize)  // Max number of concurrent threads
                .build();
    }
    
    /**
     * Task executor for parallel processing
     * Manages thread pool for concurrent chunk processing
     */
    @Bean
    public TaskExecutor batchTaskExecutor() {
        logger.info("Creating batch TaskExecutor with pool size: {}", threadPoolSize);
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threadPoolSize);
        executor.setMaxPoolSize(threadPoolSize * 2);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("batch-thread-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        
        return executor;
    }
}
