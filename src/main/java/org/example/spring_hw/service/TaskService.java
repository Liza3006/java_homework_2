package org.example.spring_hw.service;

import lombok.extern.slf4j.Slf4j;
import org.example.spring_hw.model.Task;
import org.example.spring_hw.repository.TaskRepository;
import org.example.spring_hw.service.scope.PrototypeScopedBean;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Сервис для работы с задачами
 */
@Slf4j
@Service
public class TaskService {

  private final TaskRepository taskRepository;
  private final ObjectFactory<PrototypeScopedBean> prototypeBeanFactory;

  private Map<Long, Task> taskCache = new ConcurrentHashMap<>();

  @Autowired
  public TaskService(TaskRepository taskRepository,
                     ObjectFactory<PrototypeScopedBean> prototypeBeanFactory) {
    this.taskRepository = taskRepository;
    this.prototypeBeanFactory = prototypeBeanFactory;
  }

  @PostConstruct
  public void initCache() {
    log.info("инициализация кэша задач");
    taskCache = taskRepository.findAll().stream()
      .collect(Collectors.toConcurrentMap(Task::getId, task -> task));
    log.info("Кэш загружен, размер: {}", taskCache.size());
  }

  @PreDestroy
  public void destroy() {
    log.info("очистка ресурсов ");
    log.info("Размер кэша перед уничтожением: {}", taskCache.size());
  }

  public List<Task> findAll() {
    return taskRepository.findAll();
  }

  public Task findById(Long id) {
    return taskRepository.findById(id);
  }

  public Task createTask(Task task) {
    String generatedId = prototypeBeanFactory.getObject().generateId();
    log.info("Сгенерирован ID: {}", generatedId);
    return taskRepository.save(task);
  }

  public Task updateTask(Long id, Task task) {
    if (!taskRepository.existsById(id)) {
      return null;
    }
    task.setId(id);
    return taskRepository.save(task);
  }

  public boolean deleteTask(Long id) {
    if (taskRepository.existsById(id)) {
      taskRepository.deleteById(id);
      return true;
    }
    return false;
  }
}