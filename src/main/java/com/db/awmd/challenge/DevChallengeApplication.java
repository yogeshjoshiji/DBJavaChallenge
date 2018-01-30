package com.db.awmd.challenge;

import java.util.concurrent.Executor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.db.awmd.challenge.config.SwaggerConfig;

@EnableAsync
@SpringBootApplication
@Import(SwaggerConfig.class)
public class DevChallengeApplication {

	public static void main(String[] args) {		
			SpringApplication.run(DevChallengeApplication.class, args);		
	}

	@Bean
	public Executor asyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(4);
		executor.setMaxPoolSize(5);
		executor.setQueueCapacity(500);
		executor.setThreadNamePrefix("EmailNotification-");
		executor.initialize();
		return executor;
	}
}
