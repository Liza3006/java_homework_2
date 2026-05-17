package org.example.spring_hw.service;

import org.example.spring_hw.model.Priority;
import org.example.spring_hw.model.Task;
import org.example.spring_hw.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class TaskServiceTest {

  @MockitoBean
  private TaskRepository taskRepository;

  @Autowired
  private TaskService taskService;

  @Test
  void updateTaskStatus_existingTask_shouldPersistUpdatedStatus() {
    long taskId = 1L;
    LocalDateTime createdAt = LocalDateTime.of(2026, 3, 23, 12, 0);
    LocalDate dueDate = LocalDate.of(2026, 12, 31);

    Task existingTask = new Task(taskId, "Original Task", "Description", false, createdAt, dueDate, Priority.HIGH, Set.of("work"));
    Task updateRequest = new Task(null, "Original Task", "Description", true, createdAt, dueDate, Priority.HIGH, Set.of("work"));

    when(taskRepository.existsById(taskId)).thenReturn(true);
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
    when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Task result = taskService.updateTask(taskId, updateRequest);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(taskId);
    assertThat(result.isCompleted()).isTrue();

    var taskCaptor = org.mockito.ArgumentCaptor.forClass(Task.class);
    verify(taskRepository).existsById(taskId);
    verify(taskRepository).findById(taskId);
    verify(taskRepository).save(taskCaptor.capture());
    assertThat(taskCaptor.getValue().getId()).isEqualTo(taskId);
    assertThat(taskCaptor.getValue().isCompleted()).isTrue();
  }
}
