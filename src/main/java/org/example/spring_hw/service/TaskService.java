package org.example.spring_hw.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.example.spring_hw.exception.BulkTaskCompletionException;
import org.example.spring_hw.model.Task;
import org.example.spring_hw.repository.TaskRepository;
import org.example.spring_hw.service.scope.PrototypeScopedBean;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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

  @Transactional(readOnly = true)
  public List<Task> findAllWithAttachments() {
    return taskRepository.findAllWithAttachments();
  }

  public Task findById(Long id) {
    return taskRepository.findById(id).orElse(null);
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

    Task existingTask = taskRepository.findById(id).orElse(null);
    if (existingTask == null) {
      return null;
    }

    if (task.getDueDate() != null && existingTask.getCreatedAt() != null) {
      LocalDate creationDate = existingTask.getCreatedAt().toLocalDate();
      if (task.getDueDate().isBefore(creationDate)) {
        log.warn("Invalid due date: {} is before creation date: {}", task.getDueDate(), creationDate);
        throw new IllegalArgumentException(
          "Due date (" + task.getDueDate() + ") cannot be before creation date (" + creationDate + ")"
        );
      }
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

  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = BulkTaskCompletionException.class, readOnly = false)
  public void bulkCompleteTasks(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return;
    }

    for (Long id : ids) {
      Task task = taskRepository.findById(id)
        .orElseThrow(() -> new BulkTaskCompletionException("Task not found: " + id));
      task.setCompleted(true);
      taskRepository.save(task);
    }
  }
}