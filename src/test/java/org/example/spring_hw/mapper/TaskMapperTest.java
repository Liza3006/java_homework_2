package org.example.spring_hw.mapper;

import org.example.spring_hw.dto.AttachmentResponseDto;
import org.example.spring_hw.dto.TaskCreateDto;
import org.example.spring_hw.dto.TaskResponseDto;
import org.example.spring_hw.dto.TaskUpdateDto;
import org.example.spring_hw.model.Priority;
import org.example.spring_hw.model.Task;
import org.example.spring_hw.model.TaskAttachment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TaskMapperTest {

  private TaskMapper taskMapper;

  @BeforeEach
  void setUp() {
    taskMapper = Mappers.getMapper(TaskMapper.class);
  }

  @Test
  void toEntity_ShouldMapCreateDtoToTask() {
    TaskCreateDto dto = new TaskCreateDto();
    dto.setTitle("Test Task");
    dto.setDescription("Test Description");
    dto.setDueDate(LocalDate.of(2026, 12, 31));
    dto.setPriority(Priority.HIGH);
    dto.setTags(Set.of("work", "urgent"));

    Task task = taskMapper.toEntity(dto);

    assertThat(task).isNotNull();
    assertThat(task.getId()).isNull();
    assertThat(task.getTitle()).isEqualTo("Test Task");
    assertThat(task.getDescription()).isEqualTo("Test Description");
    assertThat(task.getDueDate()).isEqualTo(LocalDate.of(2026, 12, 31));
    assertThat(task.getPriority()).isEqualTo(Priority.HIGH);
    assertThat(task.getTags()).containsExactlyInAnyOrder("work", "urgent");
    assertThat(task.isCompleted()).isFalse();
    assertThat(task.getCreatedAt()).isNull();
  }

  @Test
  void toEntity_ShouldHandleNullTags() {
    TaskCreateDto dto = new TaskCreateDto();
    dto.setTitle("Test Task");
    dto.setDueDate(LocalDate.of(2026, 12, 31));
    dto.setPriority(Priority.MEDIUM);
    dto.setTags(null);

    Task task = taskMapper.toEntity(dto);

    assertThat(task).isNotNull();
    assertThat(task.getTags()).isEmpty();
  }

  @Test
  void updateEntity_ShouldUpdateOnlyNonNullFields() {
    Task task = new Task();
    task.setId(1L);
    task.setTitle("Original Title");
    task.setDescription("Original Description");
    task.setCompleted(false);
    task.setDueDate(LocalDate.of(2026, 12, 31));
    task.setPriority(Priority.LOW);
    task.setTags(Set.of("old"));

    TaskUpdateDto dto = new TaskUpdateDto();
    dto.setTitle("Updated Title");
    dto.setCompleted(true);
    dto.setPriority(Priority.HIGH);

    taskMapper.updateEntity(dto, task);

    assertThat(task.getId()).isEqualTo(1L);
    assertThat(task.getTitle()).isEqualTo("Updated Title");
    assertThat(task.getDescription()).isEqualTo("Original Description");
    assertThat(task.isCompleted()).isTrue();
    assertThat(task.getDueDate()).isEqualTo(LocalDate.of(2026, 12, 31));
    assertThat(task.getPriority()).isEqualTo(Priority.HIGH);
    assertThat(task.getTags()).containsExactly("old");
  }

  @Test
  void toResponseDto_ShouldMapTaskToResponseDto() {
    LocalDateTime createdAt = LocalDateTime.of(2026, 3, 23, 12, 0);
    Task task = new Task(
      1L, "Test Task", "Description", true,
      createdAt, LocalDate.of(2026, 12, 31),
      Priority.HIGH, Set.of("work", "urgent")
    );

    TaskResponseDto dto = taskMapper.toResponseDto(task);
    assertThat(dto).isNotNull();
    assertThat(dto.getId()).isEqualTo(1L);
    assertThat(dto.getTitle()).isEqualTo("Test Task");
    assertThat(dto.getDescription()).isEqualTo("Description");
    assertThat(dto.isCompleted()).isTrue();
    assertThat(dto.getCreatedAt()).isEqualTo(createdAt);
    assertThat(dto.getDueDate()).isEqualTo(LocalDate.of(2026, 12, 31));
    assertThat(dto.getPriority()).isEqualTo(Priority.HIGH);
    assertThat(dto.getTags()).containsExactlyInAnyOrder("work", "urgent");
    assertThat(dto.getAttachments()).isEmpty();
  }

  @Test
  void toResponseDto_ShouldMapAttachments() {
    Task task = new Task();
    task.setId(42L);
    task.setTitle("Task with attachment");
    task.setCompleted(false);
    task.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
    task.setPriority(Priority.MEDIUM);

    TaskAttachment attachment = TaskAttachment.builder()
      .id(7L)
      .task(task)
      .fileName("report.pdf")
      .storedFileName("stored-report.pdf")
      .contentType("application/pdf")
      .size(1024L)
      .uploadedAt(LocalDateTime.of(2026, 1, 1, 11, 0))
      .build();
    task.setAttachments(Set.of(attachment));

    TaskResponseDto dto = taskMapper.toResponseDto(task);

    assertThat(dto.getAttachments()).hasSize(1);
    AttachmentResponseDto mapped = dto.getAttachments().iterator().next();
    assertThat(mapped.getId()).isEqualTo(7L);
    assertThat(mapped.getTaskId()).isEqualTo(42L);
    assertThat(mapped.getFileName()).isEqualTo("report.pdf");
    assertThat(mapped.getSize()).isEqualTo(1024L);
    assertThat(mapped.getUploadedAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 11, 0));
  }
}
