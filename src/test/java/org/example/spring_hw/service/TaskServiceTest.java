package org.example.spring_hw.service;

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
  void findById_ExistingId_ShouldReturnTask() {
    when(taskRepository.findById(1L)).thenReturn(testTask);

    Task task = taskService.findById(1L);

    assertThat(task).isNotNull();
    assertThat(task.getId()).isEqualTo(1L);
    verify(taskRepository).findById(1L);
  }

  @Test
  void findById_NonExistingId_ShouldReturnNull() {
    when(taskRepository.findById(999L)).thenReturn(null);

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
    when(taskRepository.findById(1L)).thenReturn(existingTask);
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
  void updateTask_WithDueDateAfterCreation_ShouldUpdateSuccessfully() {
    
    Task updateTask = new Task(
      1L, "Updated Task", "Description", false,
      creationTime, LocalDate.of(2026, 12, 31),
      Priority.HIGH, Set.of("work")
    );

    when(taskRepository.existsById(1L)).thenReturn(true);
    when(taskRepository.findById(1L)).thenReturn(existingTask);
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
    when(taskRepository.findById(1L)).thenReturn(existingTask);
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
    when(taskRepository.findById(1L)).thenReturn(existingTask);
    
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
    when(taskRepository.findById(1L)).thenReturn(existingTask);
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
