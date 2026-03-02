package org.example.spring_hw.repository;

import lombok.extern.slf4j.Slf4j;
import org.example.spring_hw.model.Task;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class StubTaskRepository implements TaskRepository {

  private final List<Task> tasks = new ArrayList<>();

  public StubTaskRepository() {
    tasks.add(new Task(1L, "Stub Task 1", "Description 1", false));
    tasks.add(new Task(2L, "Stub Task 2", "Description 2", true));
  }

  @Override
  public List<Task> findAll() {
    return new ArrayList<>(tasks);
  }

  @Override
  public Task findById(Long id) {
    return tasks.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null);
  }

  @Override
  public Task save(Task task) {
    log.warn("Сохранение задачи {}", task.getTitle());
    return task;
  }

  @Override
  public void deleteById(Long id) {
    log.warn("Удаление задачи {}", id);
  }

  @Override
  public boolean existsById(Long id) {
    return tasks.stream().anyMatch(t -> t.getId().equals(id));
  }
}