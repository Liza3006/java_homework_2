package org.example.spring_hw.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.spring_hw.dto.TaskCreateDto;
import org.example.spring_hw.dto.TaskResponseDto;
import org.example.spring_hw.dto.TaskUpdateDto;
import org.example.spring_hw.dto.validation.OnCreate;
import org.example.spring_hw.dto.validation.OnUpdate;
import org.example.spring_hw.mapper.TaskMapper;
import org.example.spring_hw.model.Task;
import org.example.spring_hw.service.TaskService;
import org.example.spring_hw.service.scope.RequestScopedBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
@Validated
@Tag(name = "Tasks", description = "Управление задачами")
public class TaskController {

  private final TaskService taskService;
  private final TaskMapper taskMapper;
  private final RequestScopedBean requestScopedBean;

  @Autowired
  public TaskController(TaskService taskService, TaskMapper taskMapper, RequestScopedBean requestScopedBean) {
    this.taskService = taskService;
    this.taskMapper = taskMapper;
    this.requestScopedBean = requestScopedBean;
  }

  @GetMapping
  @Operation(summary = "Получить все задачи", description = "Возвращает список всех задач")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Успешно",
      content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = TaskResponseDto.class))),
    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
  })
  public ResponseEntity<List<TaskResponseDto>> getAllTasks() {
    System.out.println("RequestScopedBean ID: " + requestScopedBean.getRequestId() +
      ", start: " + requestScopedBean.getStartTime());

    List<Task> tasks = taskService.findAllWithAttachments();
    List<TaskResponseDto> response = tasks.stream()
      .map(taskMapper::toResponseDto)
      .collect(Collectors.toList());

    return ResponseEntity.ok()
      .header("X-Total-Count", String.valueOf(tasks.size()))
      .header("X-API-Version", "2.0.0")
      .body(response);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Получить задачу по ID", description = "Возвращает задачу с указанным ID")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Успешно"),
    @ApiResponse(responseCode = "404", description = "Задача не найдена")
  })
  public ResponseEntity<TaskResponseDto> getTaskById(
    @PathVariable @Parameter(description = "ID задачи", example = "1") Long id) {
    Task task = taskService.findById(id);
    if (task == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(taskMapper.toResponseDto(task));
  }

  @PostMapping
  @Operation(summary = "Создать новую задачу", description = "Создает новую задачу")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Задача создана"),
    @ApiResponse(responseCode = "400", description = "Неверные данные")
  })
  public ResponseEntity<TaskResponseDto> createTask(@Validated(OnCreate.class) @RequestBody TaskCreateDto dto) {
    System.out.println("=== CREATE TASK CALLED ===");
    System.out.println("DTO: " + dto);
    Task task = taskMapper.toEntity(dto);
    System.out.println("Task after mapping: " + task);
    Task created = taskService.createTask(task);
    System.out.println("Created task: " + created);
    return ResponseEntity.status(HttpStatus.CREATED)
      .header("X-API-Version", "2.0.0")
      .body(taskMapper.toResponseDto(created));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Обновить задачу", description = "Обновляет существующую задачу")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Задача обновлена"),
    @ApiResponse(responseCode = "404", description = "Задача не найдена"),
    @ApiResponse(responseCode = "400", description = "Неверные данные")
  })
  public ResponseEntity<TaskResponseDto> updateTask(
    @PathVariable @Parameter(description = "ID задачи", example = "1") Long id,
    @Validated(OnUpdate.class) @Valid @RequestBody
    @Parameter(description = "Данные для обновления") TaskUpdateDto dto) {
    Task task = taskService.findById(id);
    if (task == null) {
      return ResponseEntity.notFound().build();
    }
    taskMapper.updateEntity(dto, task);
    Task updated = taskService.updateTask(id, task);
    return ResponseEntity.ok()
      .header("X-API-Version", "2.0.0")
      .body(taskMapper.toResponseDto(updated));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Удалить задачу", description = "Удаляет задачу по ID")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Задача удалена"),
    @ApiResponse(responseCode = "404", description = "Задача не найдена")
  })
  public ResponseEntity<Void> deleteTask(
    @PathVariable @Parameter(description = "ID задачи", example = "1") Long id) {
    boolean deleted = taskService.deleteTask(id);
    if (!deleted) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.noContent()
      .header("X-API-Version", "2.0.0")
      .build();
  }
}