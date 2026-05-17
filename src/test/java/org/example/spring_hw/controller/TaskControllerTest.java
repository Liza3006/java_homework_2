package org.example.spring_hw.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.spring_hw.dto.TaskCreateDto;
import org.example.spring_hw.dto.TaskResponseDto;
import org.example.spring_hw.model.Priority;
import org.example.spring_hw.model.Task;
import org.example.spring_hw.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private TaskService taskService;

  @MockitoBean
  private org.example.spring_hw.mapper.TaskMapper taskMapper;

  @MockitoBean
  private org.example.spring_hw.service.scope.RequestScopedBean requestScopedBean;

  @MockitoBean
  private JpaMetamodelMappingContext jpaMetamodelMappingContext;

  private TaskCreateDto createDto;
  private TaskResponseDto responseDto;
  private final LocalDateTime createdAt = LocalDateTime.of(2026, 3, 23, 12, 0);
  private final LocalDate dueDate = LocalDate.of(2026, 12, 31);

  @BeforeEach
  void setUp() {
    createDto = new TaskCreateDto();
    createDto.setTitle("Test Task");
    createDto.setDescription("Test Description");
    createDto.setDueDate(dueDate);
    createDto.setPriority(Priority.HIGH);
    createDto.setTags(Set.of("work"));

    responseDto = TaskResponseDto.builder()
      .id(1L)
      .title("Test Task")
      .description("Test Description")
      .completed(false)
      .createdAt(createdAt)
      .dueDate(dueDate)
      .priority(Priority.HIGH)
      .tags(Set.of("work"))
      .attachments(Set.of())
      .build();

    when(requestScopedBean.getRequestId()).thenReturn("test-id");
    when(requestScopedBean.getStartTime()).thenReturn(createdAt);
  }

  @Test
  void createTask_shouldReturn201AndSerializedJson() throws Exception {
    Task mappedTask = new Task(null, "Test Task", "Test Description", false, createdAt, dueDate, Priority.HIGH, Set.of("work"));
    Task savedTask = new Task(1L, "Test Task", "Test Description", false, createdAt, dueDate, Priority.HIGH, Set.of("work"));

    when(taskMapper.toEntity(any(TaskCreateDto.class))).thenReturn(mappedTask);
    when(taskService.createTask(mappedTask)).thenReturn(savedTask);
    when(taskMapper.toResponseDto(savedTask)).thenReturn(responseDto);

    mockMvc.perform(post("/api/tasks")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createDto)))
      .andExpect(status().isCreated())
      .andExpect(header().string("X-API-Version", "2.0.0"))
      .andExpect(jsonPath("$.id").value(1L))
      .andExpect(jsonPath("$.title").value("Test Task"))
      .andExpect(jsonPath("$.completed").value(false))
      .andExpect(jsonPath("$.dueDate").value("2026-12-31"))
      .andExpect(jsonPath("$.priority").value("HIGH"))
      .andExpect(jsonPath("$.attachments", hasSize(0)));
  }

  @Test
  void getTaskById_shouldReturnSavedTaskAsJson() throws Exception {
    Task task = new Task(1L, "Saved Task", "Saved Description", false, createdAt, dueDate, Priority.MEDIUM, Set.of("saved"));
    TaskResponseDto savedResponse = TaskResponseDto.builder()
      .id(task.getId())
      .title(task.getTitle())
      .description(task.getDescription())
      .completed(task.isCompleted())
      .createdAt(task.getCreatedAt())
      .dueDate(task.getDueDate())
      .priority(task.getPriority())
      .tags(task.getTags())
      .attachments(Set.of())
      .build();

    when(taskService.findById(1L)).thenReturn(task);
    when(taskMapper.toResponseDto(task)).thenReturn(savedResponse);

    mockMvc.perform(get("/api/tasks/{id}", 1L))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(1L))
      .andExpect(jsonPath("$.title").value("Saved Task"))
      .andExpect(jsonPath("$.description").value("Saved Description"))
      .andExpect(jsonPath("$.completed").value(false))
      .andExpect(jsonPath("$.createdAt").value("2026-03-23T12:00:00"))
      .andExpect(jsonPath("$.dueDate").value("2026-12-31"))
      .andExpect(jsonPath("$.priority").value("MEDIUM"))
      .andExpect(jsonPath("$.tags", hasSize(1)));
  }

  @Test
  void createTask_invalidTitle_shouldReturnBadRequest() throws Exception {
    createDto.setTitle("12");

    mockMvc.perform(post("/api/tasks")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createDto)))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.error").value("Validation Failed"));
  }
}
