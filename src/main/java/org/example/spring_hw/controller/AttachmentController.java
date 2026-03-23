package org.example.spring_hw.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.spring_hw.dto.AttachmentResponseDto;
import org.example.spring_hw.service.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@Tag(name = "Attachments", description = "Управление вложениями задач")
public class AttachmentController {

  private final AttachmentService attachmentService;

  @Autowired
  public AttachmentController(AttachmentService attachmentService) {
    this.attachmentService = attachmentService;
  }

  @PostMapping(value = "/api/tasks/{taskId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Загрузить файл", description = "Загружает файл и прикрепляет его к задаче")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Файл успешно загружен"),
    @ApiResponse(responseCode = "404", description = "Задача не найдена"),
    @ApiResponse(responseCode = "400", description = "Ошибка валидации файла (размер > 10MB)")
  })
  public ResponseEntity<AttachmentResponseDto> uploadAttachment(
    @PathVariable @Parameter(description = "ID задачи", example = "1") Long taskId,
    @RequestParam("file") @Parameter(description = "Файл для загрузки") MultipartFile file) throws IOException {

    AttachmentResponseDto response = attachmentService.storeAttachment(taskId, file);
    return ResponseEntity.status(HttpStatus.CREATED)
      .header("X-API-Version", "2.0.0")
      .body(response);
  }

  @GetMapping("/api/attachments/{attachmentId}")
  @Operation(summary = "Скачать файл", description = "Скачивает файл по ID вложения")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Файл успешно скачан"),
    @ApiResponse(responseCode = "404", description = "Вложение не найдено")
  })
  public ResponseEntity<Resource> downloadAttachment(
    @PathVariable @Parameter(description = "ID вложения", example = "1") Long attachmentId) throws IOException {

    var attachment = attachmentService.getAttachment(attachmentId);
    Resource resource = attachmentService.loadAsResource(attachmentId);

    return ResponseEntity.ok()
      .contentType(MediaType.parseMediaType(attachment.getContentType()))
      .header(HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=\"" + attachment.getFileName() + "\"")
      .header("X-API-Version", "2.0.0")
      .body(resource);
  }

  @DeleteMapping("/api/attachments/{attachmentId}")
  @Operation(summary = "Удалить вложение", description = "Удаляет файл и его метаданные")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Вложение удалено"),
    @ApiResponse(responseCode = "404", description = "Вложение не найдено")
  })
  public ResponseEntity<Void> deleteAttachment(
    @PathVariable @Parameter(description = "ID вложения", example = "1") Long attachmentId) throws IOException {

    attachmentService.deleteAttachment(attachmentId);
    return ResponseEntity.noContent()
      .header("X-API-Version", "2.0.0")
      .build();
  }

  @GetMapping("/api/tasks/{taskId}/attachments")
  @Operation(summary = "Получить список вложений", description = "Возвращает метаданные всех вложений задачи")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Успешно"),
    @ApiResponse(responseCode = "404", description = "Задача не найдена")
  })
  public ResponseEntity<List<AttachmentResponseDto>> getTaskAttachments(
    @PathVariable @Parameter(description = "ID задачи", example = "1") Long taskId) {

    List<AttachmentResponseDto> attachments = attachmentService.getAttachmentsByTaskId(taskId);
    return ResponseEntity.ok()
      .header("X-API-Version", "2.0.0")
      .body(attachments);
  }
}
