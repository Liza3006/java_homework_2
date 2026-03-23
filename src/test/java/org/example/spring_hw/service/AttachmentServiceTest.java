package org.example.spring_hw.service;

import org.example.spring_hw.dto.AttachmentResponseDto;
import org.example.spring_hw.exception.AttachmentNotFoundException;
import org.example.spring_hw.exception.TaskNotFoundException;
import org.example.spring_hw.model.TaskAttachment;
import org.example.spring_hw.repository.TaskAttachmentRepository;
import org.example.spring_hw.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

  @Mock
  private TaskAttachmentRepository attachmentRepository;

  @Mock
  private TaskRepository taskRepository;

  @InjectMocks
  private AttachmentService attachmentService;

  @TempDir
  Path tempDir;

  private TaskAttachment testAttachment;
  private MockMultipartFile testFile;
  private String storedFileName;

  @BeforeEach
  void setUp() {
    storedFileName = "test-uuid.txt";

    ReflectionTestUtils.setField(attachmentService, "uploadDir", tempDir.toString());
    ReflectionTestUtils.setField(attachmentService, "uploadPath", tempDir);

    testAttachment = TaskAttachment.builder()
      .id(1L)
      .taskId(1L)
      .fileName("test.txt")
      .storedFileName(storedFileName)
      .contentType("text/plain")
      .size(12L)
      .uploadedAt(LocalDateTime.now())
      .build();

    testFile = new MockMultipartFile(
      "file",
      "test.txt",
      "text/plain",
      "test content".getBytes()  
    );
  }

  @Test
  void storeAttachment_ValidTaskAndFile_ShouldSaveAndReturnDto() throws IOException {
    
    when(taskRepository.existsById(1L)).thenReturn(true);
    when(attachmentRepository.save(any(TaskAttachment.class))).thenAnswer(invocation -> {
      TaskAttachment saved = invocation.getArgument(0);
      saved.setId(1L);
      return saved;
    });

    
    AttachmentResponseDto result = attachmentService.storeAttachment(1L, testFile);

    
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getFileName()).isEqualTo("test.txt");
    assertThat(result.getSize()).isEqualTo(12L);

    long fileCount = Files.list(tempDir).count();
    assertThat(fileCount).isGreaterThan(0);

    verify(attachmentRepository).save(any(TaskAttachment.class));
  }

  @Test
  void getAttachment_ExistingId_ShouldReturnAttachment() {
    
    when(attachmentRepository.findById(1L)).thenReturn(Optional.of(testAttachment));

    
    TaskAttachment result = attachmentService.getAttachment(1L);

    
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getFileName()).isEqualTo("test.txt");
  }

  @Test
  void loadAsResource_ExistingAttachment_ShouldReturnResource() throws IOException {
    
    Path filePath = tempDir.resolve(storedFileName);
    Files.write(filePath, "test content".getBytes());

    when(attachmentRepository.findById(1L)).thenReturn(Optional.of(testAttachment));

    
    var resource = attachmentService.loadAsResource(1L);

    
    assertThat(resource).isNotNull();
    assertThat(resource.exists()).isTrue();
  }

  @Test
  void deleteAttachment_ExistingId_ShouldDeleteFileAndRecord() throws IOException {
    
    Path filePath = tempDir.resolve(storedFileName);
    Files.write(filePath, "test content".getBytes());

    when(attachmentRepository.findById(1L)).thenReturn(Optional.of(testAttachment));
    doNothing().when(attachmentRepository).deleteById(1L);

    
    attachmentService.deleteAttachment(1L);

    
    assertThat(Files.exists(filePath)).isFalse();
    verify(attachmentRepository).deleteById(1L);
  }

  @Test
  void getAttachmentsByTaskId_ExistingTask_ShouldReturnList() {
    
    when(taskRepository.existsById(1L)).thenReturn(true);
    when(attachmentRepository.findByTaskId(1L)).thenReturn(List.of(testAttachment));

    
    List<AttachmentResponseDto> result = attachmentService.getAttachmentsByTaskId(1L);

    
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(1L);
  }

  @Test
  void storeAttachment_TaskNotFound_ShouldThrowTaskNotFoundException() {
    
    when(taskRepository.existsById(999L)).thenReturn(false);

    
    assertThatThrownBy(() -> attachmentService.storeAttachment(999L, testFile))
      .isInstanceOf(TaskNotFoundException.class)
      .hasMessageContaining("Task not found: 999");

    verify(attachmentRepository, never()).save(any());
  }

  @Test
  void getAttachment_NonExistingId_ShouldThrowAttachmentNotFoundException() {
    
    when(attachmentRepository.findById(999L)).thenReturn(Optional.empty());

    
    assertThatThrownBy(() -> attachmentService.getAttachment(999L))
      .isInstanceOf(AttachmentNotFoundException.class)
      .hasMessageContaining("Attachment not found: 999");
  }

  @Test
  void loadAsResource_NonExistingAttachment_ShouldThrowAttachmentNotFoundException() {
    
    when(attachmentRepository.findById(999L)).thenReturn(Optional.empty());

    
    assertThatThrownBy(() -> attachmentService.loadAsResource(999L))
      .isInstanceOf(AttachmentNotFoundException.class)
      .hasMessageContaining("Attachment not found: 999");
  }

  @Test
  void loadAsResource_FileDoesNotExist_ShouldThrowAttachmentNotFoundException() throws IOException {
    
    when(attachmentRepository.findById(1L)).thenReturn(Optional.of(testAttachment));

    
    assertThatThrownBy(() -> attachmentService.loadAsResource(1L))
      .isInstanceOf(AttachmentNotFoundException.class)
      .hasMessageContaining("File not found for attachment: 1");
  }

  @Test
  void deleteAttachment_NonExistingAttachment_ShouldThrowAttachmentNotFoundException() {
    
    when(attachmentRepository.findById(999L)).thenReturn(Optional.empty());

    
    assertThatThrownBy(() -> attachmentService.deleteAttachment(999L))
      .isInstanceOf(AttachmentNotFoundException.class)
      .hasMessageContaining("Attachment not found: 999");

    verify(attachmentRepository, never()).deleteById(any());
  }

  @Test
  void getAttachmentsByTaskId_TaskNotFound_ShouldThrowTaskNotFoundException() {
    
    when(taskRepository.existsById(999L)).thenReturn(false);

    
    assertThatThrownBy(() -> attachmentService.getAttachmentsByTaskId(999L))
      .isInstanceOf(TaskNotFoundException.class)
      .hasMessageContaining("Task not found: 999");

    verify(attachmentRepository, never()).findByTaskId(any());
  }

  @Test
  void storeAttachment_EmptyFile_ShouldThrowIllegalArgumentException() {
    
    MockMultipartFile emptyFile = new MockMultipartFile(
      "file",
      "empty.txt",
      "text/plain",
      new byte[0]
    );
    

    
    assertThatThrownBy(() -> attachmentService.storeAttachment(1L, emptyFile))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("File is empty");

    verify(attachmentRepository, never()).save(any());
    
  }
}