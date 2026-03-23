package org.example.spring_hw.mapper;

import org.example.spring_hw.dto.TaskCreateDto;
import org.example.spring_hw.dto.TaskResponseDto;
import org.example.spring_hw.dto.TaskUpdateDto;
import org.example.spring_hw.model.Task;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;

@Component
public class TaskMapper {

  public Task toEntity(TaskCreateDto dto) {
    if (dto == null) {
      return null;
    }
    Task task = new Task();
    task.setTitle(dto.getTitle());
    task.setDescription(dto.getDescription());
    task.setDueDate(dto.getDueDate());
    task.setPriority(dto.getPriority());
    task.setTags(dto.getTags() != null ? dto.getTags() : new HashSet<>());
    task.setCompleted(false);
    task.setCreatedAt(LocalDateTime.now());
    return task;
  }

  public void updateEntity(TaskUpdateDto dto, Task task) {
    if (dto == null || task == null) {
      return;
    }
    if (dto.getTitle() != null) {
      task.setTitle(dto.getTitle());
    }
    if (dto.getDescription() != null) {
      task.setDescription(dto.getDescription());
    }
    if (dto.getCompleted() != null) {
      task.setCompleted(dto.getCompleted());
    }
    if (dto.getDueDate() != null) {
      task.setDueDate(dto.getDueDate());
    }
    if (dto.getPriority() != null) {
      task.setPriority(dto.getPriority());
    }
    if (dto.getTags() != null) {
      task.setTags(dto.getTags());
    }
  }

  public TaskResponseDto toResponseDto(Task task) {
    if (task == null) {
      return null;
    }
    return TaskResponseDto.builder()
      .id(task.getId())
      .title(task.getTitle())
      .description(task.getDescription())
      .completed(task.isCompleted())
      .createdAt(task.getCreatedAt())
      .dueDate(task.getDueDate())
      .priority(task.getPriority())
      .tags(task.getTags())
      .build();
  }
}
