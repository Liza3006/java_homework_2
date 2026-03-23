package org.example.spring_hw.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.example.spring_hw.dto.validation.OnCreate;
import org.example.spring_hw.model.Priority;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.Set;

@Data
public class TaskCreateDto {

  @NotBlank(groups = OnCreate.class)
  @Size(min = 3, max = 100, groups = OnCreate.class)
  private String title;

  @Size(max = 500, groups = OnCreate.class)
  private String description;

  @NotNull(groups = OnCreate.class)
  @FutureOrPresent(groups = OnCreate.class)
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate dueDate;

  @NotNull(groups = OnCreate.class)
  private Priority priority;

  @Size(max = 5, groups = OnCreate.class)
  private Set<String> tags;
}