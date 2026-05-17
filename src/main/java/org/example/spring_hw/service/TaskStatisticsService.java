package org.example.spring_hw.service;

import org.example.spring_hw.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TaskStatisticsService {

  private final TaskRepository primaryRepo;
  private final TaskRepository stubRepo;

  @Value("${app.name}")
  private String appName;

  @Value("${app.version}")
  private String appVersion;

  @Autowired
  public TaskStatisticsService(@Qualifier("taskRepository") TaskRepository primaryRepo,
                               @Qualifier("stubTaskRepository") TaskRepository stubRepo) {
    this.primaryRepo = primaryRepo;
    this.stubRepo = stubRepo;
  }

  public String compareRepositories() {
    return String.format("App: %s v%s\nPrimary repo size: %d\nStub repo size: %d",
      appName, appVersion,
      primaryRepo.findAll().size(),
      stubRepo.findAll().size());
  }
}