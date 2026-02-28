package org.example.spring_hw.config;

import org.example.spring_hw.repository.StubTaskRepository;
import org.example.spring_hw.repository.TaskRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурационный класс
 */
@Configuration
public class AppConfig {

  @Bean("stubTaskRepository")
  public TaskRepository stubTaskRepository() {
    return new StubTaskRepository();
  }
}