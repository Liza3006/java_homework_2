package org.example.spring_hw.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.spring_hw.dto.TaskResponseDto;
import org.example.spring_hw.mapper.TaskMapper;
import org.example.spring_hw.model.Task;
import org.example.spring_hw.service.FavoritesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/favorites")
@Tag(name = "Favorites", description = "Управление избранными задачами (на основе сессии)")
public class FavoritesController {

  private final FavoritesService favoritesService;
  private final TaskMapper taskMapper;

  @Autowired
  public FavoritesController(FavoritesService favoritesService, TaskMapper taskMapper) {
    this.favoritesService = favoritesService;
    this.taskMapper = taskMapper;
  }

  @PostMapping("/{taskId}")
  @Operation(summary = "Добавить в избранное", description = "Добавляет задачу в список избранных")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Задача добавлена в избранное"),
    @ApiResponse(responseCode = "404", description = "Задача не найдена")
  })
  public ResponseEntity<Void> addToFavorites(
    @PathVariable @Parameter(description = "ID задачи", example = "1") Long taskId,
    HttpSession session) {
    favoritesService.addToFavorites(taskId, session);
    return ResponseEntity.status(HttpStatus.CREATED)
      .header("X-API-Version", "2.0.0")
      .build();
  }

  @DeleteMapping("/{taskId}")
  @Operation(summary = "Удалить из избранного", description = "Удаляет задачу из списка избранных")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Задача удалена из избранного"),
    @ApiResponse(responseCode = "404", description = "Задача не найдена")
  })
  public ResponseEntity<Void> removeFromFavorites(
    @PathVariable @Parameter(description = "ID задачи", example = "1") Long taskId,
    HttpSession session) {
    favoritesService.removeFromFavorites(taskId, session);
    return ResponseEntity.noContent()
      .header("X-API-Version", "2.0.0")
      .build();
  }

  @GetMapping
  @Operation(summary = "Получить избранные задачи", description = "Возвращает список всех избранных задач")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Успешно")
  })
  public ResponseEntity<List<TaskResponseDto>> getFavorites(HttpSession session) {
    List<Task> favoriteTasks = favoritesService.getFavoriteTasks(session);
    List<TaskResponseDto> response = favoriteTasks.stream()
      .map(taskMapper::toResponseDto)
      .collect(Collectors.toList());
    return ResponseEntity.ok()
      .header("X-API-Version", "2.0.0")
      .body(response);
  }

  @GetMapping("/ids")
  @Operation(summary = "Получить ID избранных задач", description = "Возвращает список ID избранных задач")
  public ResponseEntity<Set<Long>> getFavoriteIds(HttpSession session) {
    return ResponseEntity.ok()
      .header("X-API-Version", "2.0.0")
      .body(favoritesService.getFavoriteIds(session));
  }
}
