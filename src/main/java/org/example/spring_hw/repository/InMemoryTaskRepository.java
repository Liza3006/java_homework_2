package org.example.spring_hw.repository;

import org.example.spring_hw.model.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Реализация репозитория задач
 */
public class InMemoryTaskRepository {

  private final Map<Long, Task> tasks = new ConcurrentHashMap<>();
  private final AtomicLong counter = new AtomicLong(1);

  public List<Task> findAll() {
    return new ArrayList<>(tasks.values());
  }

  public Task findById(Long id) {
    return tasks.get(id);
  }

  public Task save(Task task) {
    if (task.getId() == null) {
      task.setId(counter.getAndIncrement());
    }
    tasks.put(task.getId(), task);
    return task;
  }

  public void deleteById(Long id) {
    tasks.remove(id);
  }

  public boolean existsById(Long id) {
    return tasks.containsKey(id);
  }
}