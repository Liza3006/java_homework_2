package org.example.spring_hw.repository;

import org.example.spring_hw.model.Task;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Реализация репозитория задач
 */
@Repository
@Primary
public class InMemoryTaskRepository implements TaskRepository {

  private final Map<Long, Task> tasks = new ConcurrentHashMap<>();
  private final AtomicLong counter = new AtomicLong(1);

  @Override
  public List<Task> findAll() {
    return new ArrayList<>(tasks.values());
  }

  @Override
  public Task findById(Long id) {
    return tasks.get(id);
  }

  @Override
  public Task save(Task task) {
    if (task.getId() == null) {
      task.setId(counter.getAndIncrement());
    }
    tasks.put(task.getId(), task);
    return task;
  }

  @Override
  public void deleteById(Long id) {
    tasks.remove(id);
  }

  @Override
  public boolean existsById(Long id) {
    return tasks.containsKey(id);
  }
}