package org.example.spring_hw.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Модель задачи
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {
  private Long id;
  private String title;
  private String description;
  private boolean completed;
}