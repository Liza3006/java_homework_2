package org.example.spring_hw.repository;

import lombok.extern.slf4j.Slf4j;
import org.example.spring_hw.model.Priority;
import org.example.spring_hw.model.Task;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
public class StubTaskRepository {

  private final List<Task> tasks = new ArrayList<>();

  public StubTaskRepository() {
    tasks.add(new Task(
      1L,
      "Stub Task 1",
      "Description 1",
      false,
      LocalDateTime.now().minusDays(1),
      LocalDate.now().plusDays(7),
      Priority.MEDIUM,
      Set.of("stub", "test")
    ));
    tasks.add(new Task(
      2L,
      "Stub Task 2",
      "Description 2",
      true,
      LocalDateTime.now().minusDays(2),
      LocalDate.now().plusDays(3),
      Priority.HIGH,
      Set.of("stub", "important")
    ));
  }

  public List<Task> findAll() {
    return new ArrayList<>(tasks);
  }

  public Task findById(Long id) {
    return tasks.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null);
  }

  public Task save(Task task) {
    log.warn("Сохранение задачи {}", task.getTitle());
    return task;
  }

  public void deleteById(Long id) {
    log.warn("Удаление задачи {}", id);
  }

  public boolean existsById(Long id) {
    return tasks.stream().anyMatch(t -> t.getId().equals(id));
  }
}
