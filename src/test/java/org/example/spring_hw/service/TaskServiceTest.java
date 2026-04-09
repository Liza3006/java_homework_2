package org.example.spring_hw.service;

import org.example.spring_hw.exception.BulkTaskCompletionException;
import org.example.spring_hw.model.Priority;
import org.example.spring_hw.model.Task;
import org.example.spring_hw.repository.TaskRepository;
import org.example.spring_hw.service.scope.PrototypeScopedBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

  @Mock
  private TaskRepository taskRepository;

  @Mock
  private ObjectFactory<PrototypeScopedBean> prototypeBeanFactory;

  @Mock
  private PrototypeScopedBean prototypeScopedBean;

  @InjectMocks
  private TaskService taskService;

  private Task testTask;
  private Task existingTask;
  private LocalDateTime creationTime;

  @BeforeEach
  void setUp() {
    creationTime = LocalDateTime.of(2026, 3, 23, 12, 0);

    testTask = new Task(
      1L, "Test Task", "Description", false,
      LocalDateTime.now(), LocalDate.of(2026, 12, 31),
      Priority.HIGH, Set.of("work")
    );

    existingTask = new Task(
      1L, "Original Task", "Description", false,
      creationTime, LocalDate.of(2026, 12, 31),
      Priority.HIGH, Set.of("work")
    );
  }

  @Test
  void findAll_ShouldReturnAllTasks() {
    when(taskRepository.findAll()).thenReturn(List.of(testTask));

    List<Task> tasks = taskService.findAll();

    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getTitle()).isEqualTo("Test Task");
    verify(taskRepository).findAll();
  }

  @Test
  void findAllWithAttachments_ShouldDelegateToRepository() {
    when(taskRepository.findAllWithAttachments()).thenReturn(List.of(testTask));

    List<Task> tasks = taskService.findAllWithAttachments();

    assertThat(tasks).hasSize(1);
    verify(taskRepository).findAllWithAttachments();
  }

  @Test
  void findById_ExistingId_ShouldReturnTask() {
    when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

    Task task = taskService.findById(1L);

    assertThat(task).isNotNull();
    assertThat(task.getId()).isEqualTo(1L);
    verify(taskRepository).findById(1L);
  }

  @Test
  void findById_NonExistingId_ShouldReturnNull() {
    when(taskRepository.findById(999L)).thenReturn(Optional.empty());

    Task task = taskService.findById(999L);

    assertThat(task).isNull();
    verify(taskRepository).findById(999L);
  }

  @Test
  void createTask_ShouldSaveAndReturnTask() {
    when(prototypeBeanFactory.getObject()).thenReturn(prototypeScopedBean);
    when(prototypeScopedBean.generateId()).thenReturn("generated-id-123");
    when(taskRepository.save(any(Task.class))).thenReturn(testTask);

    Task created = taskService.createTask(testTask);
    assertThat(created).isNotNull();
    assertThat(created.getId()).isEqualTo(1L);
    verify(taskRepository).save(testTask);
    verify(prototypeBeanFactory).getObject();
  }

  @Test
  void updateTask_ExistingId_ShouldUpdateAndReturnTask() {
    when(taskRepository.existsById(1L)).thenReturn(true);
    when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
    when(taskRepository.save(any(Task.class))).thenReturn(testTask);

    Task updated = taskService.updateTask(1L, testTask);
    assertThat(updated).isNotNull();
    assertThat(updated.getId()).isEqualTo(1L);
    verify(taskRepository).save(testTask);
  }

  @Test
  void updateTask_NonExistingId_ShouldReturnNull() {
    when(taskRepository.existsById(999L)).thenReturn(false);
    Task updated = taskService.updateTask(999L, testTask);

    assertThat(updated).isNull();
    verify(taskRepository, never()).save(any());
  }

  @Test
  void deleteTask_ExistingId_ShouldReturnTrue() {
    when(taskRepository.existsById(1L)).thenReturn(true);
    doNothing().when(taskRepository).deleteById(1L);

    boolean deleted = taskService.deleteTask(1L);

    assertThat(deleted).isTrue();
    verify(taskRepository).deleteById(1L);
  }

  @Test
  void deleteTask_NonExistingId_ShouldReturnFalse() {
    when(taskRepository.existsById(999L)).thenReturn(false);

    boolean deleted = taskService.deleteTask(999L);

    assertThat(deleted).isFalse();
    verify(taskRepository, never()).deleteById(any());
  }

  @Test
  void bulkCompleteTasks_ShouldMarkAllFoundTasksCompleted() {
    Task first = new Task(1L, "First", null, false, creationTime, LocalDate.now().plusDays(1), Priority.HIGH, Set.of("a"));
    Task second = new Task(2L, "Second", null, false, creationTime, LocalDate.now().plusDays(2), Priority.LOW, Set.of("b"));

    when(taskRepository.findById(1L)).thenReturn(Optional.of(first));
    when(taskRepository.findById(2L)).thenReturn(Optional.of(second));
    when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

    taskService.bulkCompleteTasks(List.of(1L, 2L));

    assertThat(first.isCompleted()).isTrue();
    assertThat(second.isCompleted()).isTrue();
    verify(taskRepository, times(2)).save(any(Task.class));
  }

  @Test
  void bulkCompleteTasks_WhenTaskMissing_ShouldThrowCustomException() {
    Task first = new Task(1L, "First", null, false, creationTime, LocalDate.now().plusDays(1), Priority.HIGH, Set.of("a"));
    when(taskRepository.findById(1L)).thenReturn(Optional.of(first));
    when(taskRepository.findById(999L)).thenReturn(Optional.empty());
    when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

    assertThatThrownBy(() -> taskService.bulkCompleteTasks(List.of(1L, 999L)))
      .isInstanceOf(BulkTaskCompletionException.class)
      .hasMessageContaining("Task not found: 999");

    assertThat(first.isCompleted()).isTrue();
    verify(taskRepository).save(first);
  }

  @Test
  void updateTask_WithDueDateAfterCreation_ShouldUpdateSuccessfully() {
    Task updateTask = new Task(
      1L, "Updated Task", "Description", false,
      creationTime, LocalDate.of(2026, 12, 31),
      Priority.HIGH, Set.of("work")
    );

    when(taskRepository.existsById(1L)).thenReturn(true);
    when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
    when(taskRepository.save(any(Task.class))).thenReturn(updateTask);

    Task result = taskService.updateTask(1L, updateTask);

    assertThat(result).isNotNull();
    assertThat(result.getDueDate()).isEqualTo(LocalDate.of(2026, 12, 31));
    verify(taskRepository).save(updateTask);
  }

  @Test
  void updateTask_WithDueDateEqualToCreation_ShouldUpdateSuccessfully() {
    LocalDate dueDateEqualsCreation = existingTask.getCreatedAt().toLocalDate();
    Task updateTask = new Task(
      1L, "Updated Task", "Description", false,
      creationTime, dueDateEqualsCreation,
      Priority.HIGH, Set.of("work")
    );

    when(taskRepository.existsById(1L)).thenReturn(true);
    when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
    when(taskRepository.save(any(Task.class))).thenReturn(updateTask);

    Task result = taskService.updateTask(1L, updateTask);

    assertThat(result).isNotNull();
    assertThat(result.getDueDate()).isEqualTo(dueDateEqualsCreation);
    verify(taskRepository).save(updateTask);
  }

  @Test
  void updateTask_WithDueDateBeforeCreation_ShouldThrowIllegalArgumentException() {
    LocalDate invalidDueDate = existingTask.getCreatedAt().toLocalDate().minusDays(1);
    Task updateTask = new Task(
      1L, "Updated Task", "Description", false,
      creationTime, invalidDueDate,
      Priority.HIGH, Set.of("work")
    );

    when(taskRepository.existsById(1L)).thenReturn(true);
    when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));

    assertThatThrownBy(() -> taskService.updateTask(1L, updateTask))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Due date")
      .hasMessageContaining("cannot be before creation date");

    verify(taskRepository, never()).save(any());
  }

  @Test
  void updateTask_WithNullDueDate_ShouldSkipValidation() {
    Task updateTask = new Task(
      1L, "Updated Task", "Description", false,
      creationTime, null,
      Priority.HIGH, Set.of("work")
    );

    when(taskRepository.existsById(1L)).thenReturn(true);
    when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
    when(taskRepository.save(any(Task.class))).thenReturn(updateTask);

    Task result = taskService.updateTask(1L, updateTask);

    assertThat(result).isNotNull();
    assertThat(result.getDueDate()).isNull();
    verify(taskRepository).save(updateTask);
  }

  @Test
  void updateTask_WithDueDateButTaskNotFound_ShouldReturnNull() {
    when(taskRepository.existsById(999L)).thenReturn(false);

    Task result = taskService.updateTask(999L, testTask);

    assertThat(result).isNull();
    verify(taskRepository, never()).findById(any());
    verify(taskRepository, never()).save(any());
  }
}
