package org.example.spring_hw.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskAttachment {
  private Long id;
  private Long taskId;
  private String fileName;
  private String storedFileName;
  private String contentType;
  private long size;
  private LocalDateTime uploadedAt;
}