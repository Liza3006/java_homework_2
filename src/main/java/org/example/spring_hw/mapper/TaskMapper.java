package org.example.spring_hw.mapper;

import org.example.spring_hw.dto.AttachmentResponseDto;
import org.example.spring_hw.dto.TaskCreateDto;
import org.example.spring_hw.dto.TaskResponseDto;
import org.example.spring_hw.dto.TaskUpdateDto;
import org.example.spring_hw.model.Task;
import org.example.spring_hw.model.TaskAttachment;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.HashSet;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface TaskMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "attachments", ignore = true)
  @Mapping(target = "completed", constant = "false")
  @Mapping(target = "tags", source = "tags", qualifiedByName = "copyTagsOrEmpty")
  Task toEntity(TaskCreateDto dto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "attachments", ignore = true)
  @Mapping(target = "tags", ignore = true)
  void updateEntity(TaskUpdateDto dto, @MappingTarget Task task);

  TaskResponseDto toResponseDto(Task task);

  @Mapping(target = "taskId", source = "task.id")
  AttachmentResponseDto toAttachmentResponseDto(TaskAttachment attachment);

  @Named("copyTagsOrEmpty")
  default Set<String> copyTagsOrEmpty(Set<String> tags) {
    return tags == null ? new HashSet<>() : new HashSet<>(tags);
  }

  @AfterMapping
  default void updateTags(TaskUpdateDto dto, @MappingTarget Task task) {
    if (dto != null && dto.getTags() != null) {
      task.setTags(copyTagsOrEmpty(dto.getTags()));
    }
  }
}
