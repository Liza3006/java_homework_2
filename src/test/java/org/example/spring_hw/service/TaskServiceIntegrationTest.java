package org.example.spring_hw.service;

import org.example.spring_hw.config.JpaAuditingConfig;
import org.example.spring_hw.exception.BulkTaskCompletionException;
import org.example.spring_hw.model.Priority;
import org.example.spring_hw.model.Task;
import org.example.spring_hw.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Import(JpaAuditingConfig.class)
class TaskServiceIntegrationTest {

  @Autowired
  private TaskService taskService;

  @Autowired
  private TaskRepository taskRepository;

  @BeforeEach
  void setUp() {
    taskRepository.deleteAllInBatch();
  }

  @Test
  void bulkCompleteTasks_ShouldMarkAllTasksCompleted() {
    Task first = taskRepository.save(new Task(null, "First", null, false,
      LocalDateTime.now(), LocalDate.now().plusDays(1), Priority.HIGH, Set.of("a")));
    Task second = taskRepository.save(new Task(null, "Second", null, false,
      LocalDateTime.now(), LocalDate.now().plusDays(2), Priority.LOW, Set.of("b")));

    taskService.bulkCompleteTasks(List.of(first.getId(), second.getId()));

    List<Task> updated = taskRepository.findAll();
    assertThat(updated).allMatch(Task::isCompleted);
  }

  @Test
  void bulkCompleteTasks_WhenAnyTaskMissing_ShouldRollbackAllChanges() {
    Task first = taskRepository.save(new Task(null, "First", null, false,
      LocalDateTime.now(), LocalDate.now().plusDays(1), Priority.HIGH, Set.of("a")));
    Task second = taskRepository.save(new Task(null, "Second", null, false,
      LocalDateTime.now(), LocalDate.now().plusDays(2), Priority.LOW, Set.of("b")));

    assertThatThrownBy(() -> taskService.bulkCompleteTasks(List.of(first.getId(), 99999L, second.getId())))
      .isInstanceOf(BulkTaskCompletionException.class)
      .hasMessageContaining("Task not found: 99999");

    List<Task> tasks = taskRepository.findAll();
    assertThat(tasks).allMatch(task -> !task.isCompleted());
  }
}
