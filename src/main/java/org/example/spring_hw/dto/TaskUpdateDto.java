package org.example.spring_hw.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.example.spring_hw.dto.validation.OnUpdate;
import org.example.spring_hw.model.Priority;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

@Data
public class TaskUpdateDto {

  @Size(min = 3, max = 100, groups = OnUpdate.class)
  private String title;

  @Size(max = 500)
  private String description;

  private Boolean completed;

  @FutureOrPresent(groups = OnUpdate.class)
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate dueDate;

  private Priority priority;

  @Size(max = 5)
  private Set<String> tags;
}
