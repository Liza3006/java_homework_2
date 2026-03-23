package org.example.spring_hw.controller;

import org.example.spring_hw.dto.AttachmentResponseDto;
import org.example.spring_hw.exception.TaskNotFoundException;
import org.example.spring_hw.service.AttachmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AttachmentController.class)
class AttachmentControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private AttachmentService attachmentService;

  private AttachmentResponseDto responseDto;

  @BeforeEach
  void setUp() {
    responseDto = AttachmentResponseDto.builder()
      .id(1L)
      .taskId(1L)
      .fileName("test.txt")
      .size(1024L)
      .uploadedAt(LocalDateTime.now())
      .build();
  }

  

  @Test
  void uploadAttachment_ValidFile_ShouldReturnCreated() throws Exception {
    
    MockMultipartFile file = new MockMultipartFile(
      "file",
      "test.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "test content".getBytes()
    );
    when(attachmentService.storeAttachment(eq(1L), any())).thenReturn(responseDto);

    
    mockMvc.perform(multipart("/api/tasks/1/attachments")
        .file(file))
      .andExpect(status().isCreated())
      .andExpect(header().string("X-API-Version", "2.0.0"))
      .andExpect(jsonPath("$.id").value(1L))
      .andExpect(jsonPath("$.fileName").value("test.txt"));
  }

  @Test
  void downloadAttachment_ValidId_ShouldReturnFile() throws Exception {
    
    org.example.spring_hw.model.TaskAttachment attachment = org.example.spring_hw.model.TaskAttachment.builder()
      .id(1L)
      .fileName("test.txt")
      .contentType("text/plain")
      .build();
    when(attachmentService.getAttachment(1L)).thenReturn(attachment);
    when(attachmentService.loadAsResource(1L)).thenReturn(
      new org.springframework.core.io.ByteArrayResource("content".getBytes()));

    
    mockMvc.perform(get("/api/attachments/1"))
      .andExpect(status().isOk())
      .andExpect(header().string("X-API-Version", "2.0.0"))
      .andExpect(header().string("Content-Disposition", "attachment; filename=\"test.txt\""));
  }

  @Test
  void deleteAttachment_ValidId_ShouldReturnNoContent() throws Exception {
    
    doNothing().when(attachmentService).deleteAttachment(1L);

    
    mockMvc.perform(delete("/api/attachments/1"))
      .andExpect(status().isNoContent())
      .andExpect(header().string("X-API-Version", "2.0.0"));
  }

  @Test
  void getTaskAttachments_ValidTaskId_ShouldReturnList() throws Exception {
    
    when(attachmentService.getAttachmentsByTaskId(1L)).thenReturn(List.of(responseDto));

    
    mockMvc.perform(get("/api/tasks/1/attachments"))
      .andExpect(status().isOk())
      .andExpect(header().string("X-API-Version", "2.0.0"))
      .andExpect(jsonPath("$", hasSize(1)))
      .andExpect(jsonPath("$[0].id").value(1L));
  }

  

  @Test
  void uploadAttachment_TaskNotFound_ShouldReturnNotFound() throws Exception {
    
    MockMultipartFile file = new MockMultipartFile(
      "file",
      "test.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "test content".getBytes()
    );
    when(attachmentService.storeAttachment(eq(999L), any()))
      .thenThrow(new TaskNotFoundException("Task not found: 999"));

    
    mockMvc.perform(multipart("/api/tasks/999/attachments")
        .file(file))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  void downloadAttachment_NotFound_ShouldReturnNotFound() throws Exception {
    
    when(attachmentService.getAttachment(999L))
      .thenThrow(new org.example.spring_hw.exception.AttachmentNotFoundException("Attachment not found: 999"));

    
    mockMvc.perform(get("/api/attachments/999"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  void deleteAttachment_NotFound_ShouldReturnNotFound() throws Exception {
    
    doThrow(new org.example.spring_hw.exception.AttachmentNotFoundException("Attachment not found: 999"))
      .when(attachmentService).deleteAttachment(999L);

    
    mockMvc.perform(delete("/api/attachments/999"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  void uploadAttachment_EmptyFile_ShouldReturnBadRequest() throws Exception {
    MockMultipartFile emptyFile = new MockMultipartFile(
      "file",
      "empty.txt",
      MediaType.TEXT_PLAIN_VALUE,
      new byte[0]  
    );

    when(attachmentService.storeAttachment(eq(1L), any()))
      .thenThrow(new IllegalArgumentException("File is empty"));
    
    mockMvc.perform(multipart("/api/tasks/1/attachments")
        .file(emptyFile))
      .andExpect(status().isBadRequest());
  }

}