package org.example.spring_hw.controller;

import org.example.spring_hw.dto.TaskResponseDto;
import org.example.spring_hw.mapper.TaskMapper;
import org.example.spring_hw.model.Priority;
import org.example.spring_hw.model.Task;
import org.example.spring_hw.service.FavoritesService;
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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FavoritesController.class)
class FavoritesControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private FavoritesService favoritesService;

  @MockBean
  private TaskMapper taskMapper;

  @MockBean
  private JpaMetamodelMappingContext jpaMetamodelMappingContext;

  private Task testTask;
  private TaskResponseDto responseDto;

  @BeforeEach
  void setUp() {
    testTask = new Task(
      1L, "Test Task", "Description", false,
      LocalDateTime.now(), LocalDate.of(2026, 12, 31),
      Priority.HIGH, Set.of("work")
    );

    responseDto = TaskResponseDto.builder()
      .id(1L)
      .title("Test Task")
      .description("Description")
      .completed(false)
      .createdAt(LocalDateTime.now())
      .dueDate(LocalDate.of(2026, 12, 31))
      .priority(Priority.HIGH)
      .tags(Set.of("work"))
      .build();
  }


  @Test
  void addToFavorites_ValidId_ShouldReturnCreated() throws Exception {
    
    doNothing().when(favoritesService).addToFavorites(eq(1L), any());

    
    mockMvc.perform(post("/api/favorites/1")
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated())
      .andExpect(header().string("X-API-Version", "2.0.0"));
  }

  @Test
  void removeFromFavorites_ValidId_ShouldReturnNoContent() throws Exception {
    
    doNothing().when(favoritesService).removeFromFavorites(eq(1L), any());

    
    mockMvc.perform(delete("/api/favorites/1"))
      .andExpect(status().isNoContent())
      .andExpect(header().string("X-API-Version", "2.0.0"));
  }

  @Test
  void getFavorites_ShouldReturnListOfTasks() throws Exception {
    
    when(favoritesService.getFavoriteTasks(any())).thenReturn(List.of(testTask));
    when(taskMapper.toResponseDto(any())).thenReturn(responseDto);

    
    mockMvc.perform(get("/api/favorites"))
      .andExpect(status().isOk())
      .andExpect(header().string("X-API-Version", "2.0.0"))
      .andExpect(jsonPath("$", hasSize(1)))
      .andExpect(jsonPath("$[0].id").value(1L))
      .andExpect(jsonPath("$[0].title").value("Test Task"));
  }

  @Test
  void getFavoriteIds_ShouldReturnSetOfIds() throws Exception {
    when(favoritesService.getFavoriteIds(any())).thenReturn(Set.of(1L, 2L, 3L));

    mockMvc.perform(get("/api/favorites/ids"))
      .andExpect(status().isOk())
      .andExpect(header().string("X-API-Version", "2.0.0"))
      .andExpect(jsonPath("$", hasSize(3)))
      .andExpect(jsonPath("$", containsInAnyOrder(1, 2, 3)));
  }

  

  @Test
  void addToFavorites_TaskNotFound_ShouldReturnNotFound() throws Exception {
    
    doThrow(new org.example.spring_hw.exception.TaskNotFoundException("Task not found: 999"))
      .when(favoritesService).addToFavorites(eq(999L), any());

    
    mockMvc.perform(post("/api/favorites/999"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  void removeFromFavorites_TaskNotFound_ShouldReturnNotFound() throws Exception {
    
    doThrow(new org.example.spring_hw.exception.TaskNotFoundException("Task not found: 999"))
      .when(favoritesService).removeFromFavorites(eq(999L), any());

    
    mockMvc.perform(delete("/api/favorites/999"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  void getFavorites_EmptyList_ShouldReturnEmptyArray() throws Exception {
    
    when(favoritesService.getFavoriteTasks(any())).thenReturn(List.of());

    mockMvc.perform(get("/api/favorites"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$", hasSize(0)));
  }
}