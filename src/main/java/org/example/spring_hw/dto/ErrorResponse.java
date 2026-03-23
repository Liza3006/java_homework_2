package org.example.spring_hw.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Структура ответа при ошибке")
public class ErrorResponse {

  @Schema(description = "Временная метка ошибки", example = "2024-01-15T10:30:00Z")
  private Instant timestamp;

  @Schema(description = "HTTP статус код", example = "400")
  private int status;

  @Schema(description = "Название ошибки", example = "Bad Request")
  private String error;

  @Schema(description = "Детальное сообщение", example = "Validation failed")
  private String message;

  @Schema(description = "Путь запроса", example = "/api/tasks")
  private String path;

  @Schema(description = "Дополнительные детали ошибки")
  private Map<String, Object> details;
}