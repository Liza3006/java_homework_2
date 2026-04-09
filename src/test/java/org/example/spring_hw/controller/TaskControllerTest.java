package org.example.spring_hw.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.spring_hw.dto.TaskCreateDto;
import org.example.spring_hw.dto.TaskResponseDto;
import org.example.spring_hw.dto.TaskUpdateDto;
import org.example.spring_hw.model.Priority;
import org.example.spring_hw.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private TaskService taskService;

  @MockBean
  private org.example.spring_hw.mapper.TaskMapper taskMapper;

  @MockBean
  private org.example.spring_hw.service.scope.RequestScopedBean requestScopedBean;

  @MockBean
  private JpaMetamodelMappingContext jpaMetamodelMappingContext;

  private TaskResponseDto responseDto;
  private TaskCreateDto createDto;

  @BeforeEach
  void setUp() {
    createDto = new TaskCreateDto();
    createDto.setTitle("Test Task");
    createDto.setDescription("Test Description");
    createDto.setDueDate(LocalDate.of(2026, 12, 31));
    createDto.setPriority(Priority.HIGH);
    createDto.setTags(Set.of("work"));

    responseDto = TaskResponseDto.builder()
      .id(1L)
      .title("Test Task")
      .description("Test Description")
      .completed(false)
      .createdAt(LocalDateTime.now())
      .dueDate(LocalDate.of(2026, 12, 31))
      .priority(Priority.HIGH)
      .tags(Set.of("work"))
      .build();

    when(requestScopedBean.getRequestId()).thenReturn("test-id");
    when(requestScopedBean.getStartTime()).thenReturn(LocalDateTime.now());
  }

  @Test
  void getAllTasks_ShouldReturnListOfTasks() throws Exception {
    org.example.spring_hw.model.Task task = new org.example.spring_hw.model.Task();
    when(taskService.findAllWithAttachments()).thenReturn(List.of(task));
    when(taskMapper.toResponseDto(any())).thenReturn(responseDto);

    mockMvc.perform(get("/api/tasks"))
      .andExpect(status().isOk())
      .andExpect(header().string("X-Total-Count", "1"))
      .andExpect(header().string("X-API-Version", "2.0.0"))
      .andExpect(jsonPath("$", hasSize(1)))
      .andExpect(jsonPath("$[0].title").value("Test Task"));
  }

  @Test
  void getTaskById_ExistingId_ShouldReturnTask() throws Exception {
    org.example.spring_hw.model.Task task = new org.example.spring_hw.model.Task();
    when(taskService.findById(1L)).thenReturn(task);
    when(taskMapper.toResponseDto(any())).thenReturn(responseDto);

    mockMvc.perform(get("/api/tasks/1"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(1L))
      .andExpect(jsonPath("$.title").value("Test Task"));
  }

  @Test
  void createTask_ValidData_ShouldReturnCreatedTask() throws Exception {
    org.example.spring_hw.model.Task task = new org.example.spring_hw.model.Task();
    when(taskMapper.toEntity(any(TaskCreateDto.class))).thenReturn(task);
    when(taskService.createTask(any())).thenReturn(task);
    when(taskMapper.toResponseDto(any())).thenReturn(responseDto);

    mockMvc.perform(post("/api/tasks")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createDto)))
      .andExpect(status().isCreated())
      .andExpect(header().string("X-API-Version", "2.0.0"))
      .andExpect(jsonPath("$.id").value(1L))
      .andExpect(jsonPath("$.title").value("Test Task"));
  }

  @Test
  void updateTask_ExistingId_ShouldReturnUpdatedTask() throws Exception {
    org.example.spring_hw.model.Task existingTask = new org.example.spring_hw.model.Task();
    when(taskService.findById(1L)).thenReturn(existingTask);
    when(taskService.updateTask(eq(1L), any())).thenReturn(existingTask);
    when(taskMapper.toResponseDto(any())).thenReturn(responseDto);

    TaskUpdateDto updateDto = new TaskUpdateDto();
    updateDto.setTitle("Updated Task");
    updateDto.setCompleted(true);

    mockMvc.perform(put("/api/tasks/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updateDto)))
      .andExpect(status().isOk())
      .andExpect(header().string("X-API-Version", "2.0.0"))
      .andExpect(jsonPath("$.title").value("Test Task"));
  }

  @Test
  void deleteTask_ExistingId_ShouldReturnNoContent() throws Exception {
    when(taskService.deleteTask(1L)).thenReturn(true);

    mockMvc.perform(delete("/api/tasks/1"))
      .andExpect(status().isNoContent())
      .andExpect(header().string("X-API-Version", "2.0.0"));
  }

  @Test
  void getTaskById_NonExistingId_ShouldReturnNotFound() throws Exception {
    when(taskService.findById(999L)).thenReturn(null);

    mockMvc.perform(get("/api/tasks/999"))
      .andExpect(status().isNotFound());
  }

  @Test
  void createTask_InvalidTitle_ShouldReturnBadRequest() throws Exception {
    createDto.setTitle("12");

    mockMvc.perform(post("/api/tasks")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createDto)))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.error").value("Validation Failed"));
  }

  @Test
  void createTask_InvalidDueDate_ShouldReturnBadRequest() throws Exception {
    createDto.setDueDate(LocalDate.of(2020, 1, 1));

    mockMvc.perform(post("/api/tasks")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createDto)))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400));
  }

  @Test
  void createTask_MissingPriority_ShouldReturnBadRequest() throws Exception {
    createDto.setPriority(null);

    mockMvc.perform(post("/api/tasks")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createDto)))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400));
  }

  @Test
  void createTask_TooManyTags_ShouldReturnBadRequest() throws Exception {
    createDto.setTags(Set.of("1", "2", "3", "4", "5", "6"));

    mockMvc.perform(post("/api/tasks")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createDto)))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400));
  }

  @Test
  void updateTask_NonExistingId_ShouldReturnNotFound() throws Exception {
    when(taskService.findById(999L)).thenReturn(null);

    TaskUpdateDto updateDto = new TaskUpdateDto();
    updateDto.setTitle("Updated");

    mockMvc.perform(put("/api/tasks/999")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updateDto)))
      .andExpect(status().isNotFound());
  }

  @Test
  void deleteTask_NonExistingId_ShouldReturnNotFound() throws Exception {
    when(taskService.deleteTask(999L)).thenReturn(false);

    mockMvc.perform(delete("/api/tasks/999"))
      .andExpect(status().isNotFound());
  }
}
