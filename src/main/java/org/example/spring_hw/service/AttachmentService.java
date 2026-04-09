package org.example.spring_hw.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.spring_hw.dto.AttachmentResponseDto;
import org.example.spring_hw.exception.AttachmentNotFoundException;
import org.example.spring_hw.exception.TaskNotFoundException;
import org.example.spring_hw.model.Task;
import org.example.spring_hw.model.TaskAttachment;
import org.example.spring_hw.repository.TaskAttachmentRepository;
import org.example.spring_hw.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AttachmentService {

  private final TaskAttachmentRepository attachmentRepository;
  private final TaskRepository taskRepository;

  @Value("${app.upload-dir:uploads}")
  private String uploadDir;

  private Path uploadPath;

  @Autowired
  public AttachmentService(TaskAttachmentRepository attachmentRepository,
                           TaskRepository taskRepository) {
    this.attachmentRepository = attachmentRepository;
    this.taskRepository = taskRepository;
  }

  @PostConstruct
  public void init() throws IOException {
    this.uploadPath = Paths.get(uploadDir);
    if (!Files.exists(uploadPath)) {
      Files.createDirectories(uploadPath);
      log.info("Upload directory created at: {}", uploadPath.toAbsolutePath());
    } else {
      log.info("Upload directory already exists at: {}", uploadPath.toAbsolutePath());
    }
  }

  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = IOException.class, readOnly = false)
  public AttachmentResponseDto storeAttachment(Long taskId, MultipartFile file) throws IOException {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File is empty");
    }
    Task task = taskRepository.findById(taskId)
      .orElseThrow(() -> new TaskNotFoundException("Task not found: " + taskId));

    String originalFilename = file.getOriginalFilename();
    String extension = "";
    if (originalFilename != null && originalFilename.contains(".")) {
      extension = originalFilename.substring(originalFilename.lastIndexOf("."));
    }
    String storedFileName = UUID.randomUUID().toString() + extension;

    Path targetPath = uploadPath.resolve(storedFileName);

    try (InputStream inputStream = file.getInputStream()) {
      Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException ex) {
      Files.deleteIfExists(targetPath);
      throw ex;
    }

    TaskAttachment attachment = TaskAttachment.builder()
      .task(task)
      .fileName(originalFilename)
      .storedFileName(storedFileName)
      .contentType(file.getContentType())
      .size(file.getSize())
      .uploadedAt(LocalDateTime.now())
      .build();

    TaskAttachment saved;
    try {
      saved = attachmentRepository.save(attachment);
    } catch (RuntimeException ex) {
      Files.deleteIfExists(targetPath);
      throw ex;
    }

    return AttachmentResponseDto.builder()
      .id(saved.getId())
      .taskId(saved.getTask().getId())
      .fileName(saved.getFileName())
      .size(saved.getSize())
      .uploadedAt(saved.getUploadedAt())
      .build();
  }

  public TaskAttachment getAttachment(Long attachmentId) {
    return attachmentRepository.findById(attachmentId)
      .orElseThrow(() -> new AttachmentNotFoundException("Attachment not found: " + attachmentId));
  }

  public Resource loadAsResource(Long attachmentId) throws IOException {
    TaskAttachment attachment = getAttachment(attachmentId);
    Path filePath = uploadPath.resolve(attachment.getStoredFileName());

    if (!Files.exists(filePath)) {
      throw new AttachmentNotFoundException("File not found for attachment: " + attachmentId);
    }

    return new UrlResource(filePath.toUri());
  }

  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = IOException.class, readOnly = false)
  public void deleteAttachment(Long attachmentId) throws IOException {
    TaskAttachment attachment = getAttachment(attachmentId);
    Path filePath = uploadPath.resolve(attachment.getStoredFileName());

    Files.deleteIfExists(filePath);
    attachmentRepository.deleteById(attachmentId);
    log.info("Deleted attachment: {}", attachmentId);
  }

  @Transactional(readOnly = true)
  public List<AttachmentResponseDto> getAttachmentsByTaskId(Long taskId) {
    if (!taskRepository.existsById(taskId)) {
      throw new TaskNotFoundException("Task not found: " + taskId);
    }

    return attachmentRepository.findByTaskId(taskId).stream()
      .map(a -> AttachmentResponseDto.builder()
        .id(a.getId())
        .taskId(a.getTask().getId())
        .fileName(a.getFileName())
        .size(a.getSize())
        .uploadedAt(a.getUploadedAt())
        .build())
      .collect(Collectors.toList());
  }
}
