package org.example.spring_hw.service;

import org.example.spring_hw.exception.TaskNotFoundException;
import org.example.spring_hw.model.Priority;
import org.example.spring_hw.model.Task;
import org.example.spring_hw.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoritesServiceTest {

  @Mock
  private TaskRepository taskRepository;

  @Mock
  private HttpSession session;

  @InjectMocks
  private FavoritesService favoritesService;

  private Task testTask;
  private static final String FAVORITES_SESSION_KEY = "favoriteTaskIds";

  @BeforeEach
  void setUp() {
    testTask = new Task(
      1L, "Test Task", "Description", false,
      LocalDateTime.now(), LocalDate.of(2026, 12, 31),
      Priority.HIGH, Set.of("work")
    );
  }

  @Test
  void addToFavorites_ValidTask_ShouldAddToSession() {
    
    when(taskRepository.existsById(1L)).thenReturn(true);
    when(session.getAttribute(FAVORITES_SESSION_KEY)).thenReturn(null);

    
    favoritesService.addToFavorites(1L, session);

    
    
    
    verify(session, times(2)).setAttribute(eq(FAVORITES_SESSION_KEY), any(Set.class));
  }

  @Test
  void addToFavorites_TaskAlreadyInFavorites_ShouldNotAddDuplicate() {
    
    when(taskRepository.existsById(1L)).thenReturn(true);
    Set<Long> existingFavorites = new HashSet<>(Set.of(1L, 2L));
    when(session.getAttribute(FAVORITES_SESSION_KEY)).thenReturn(existingFavorites);

    
    favoritesService.addToFavorites(1L, session);

    
    verify(session, never()).setAttribute(eq(FAVORITES_SESSION_KEY), any());
  }

  @Test
  void removeFromFavorites_ExistingFavorite_ShouldRemove() {
    
    when(taskRepository.existsById(1L)).thenReturn(true);
    Set<Long> existingFavorites = new HashSet<>(Set.of(1L, 2L, 3L));
    when(session.getAttribute(FAVORITES_SESSION_KEY)).thenReturn(existingFavorites);

    
    favoritesService.removeFromFavorites(1L, session);

    
    verify(session).setAttribute(eq(FAVORITES_SESSION_KEY), any(Set.class));
  }

  @Test
  void removeFromFavorites_NotInFavorites_ShouldDoNothing() {
    
    when(taskRepository.existsById(1L)).thenReturn(true);
    Set<Long> existingFavorites = new HashSet<>(Set.of(2L, 3L));
    when(session.getAttribute(FAVORITES_SESSION_KEY)).thenReturn(existingFavorites);

    
    favoritesService.removeFromFavorites(1L, session);

    
    verify(session, never()).setAttribute(eq(FAVORITES_SESSION_KEY), any());
  }

  @Test
  void getFavoriteIds_ShouldReturnSetOfIds() {
    
    Set<Long> expectedFavorites = Set.of(1L, 2L, 3L);
    when(session.getAttribute(FAVORITES_SESSION_KEY)).thenReturn(expectedFavorites);

    
    Set<Long> result = favoritesService.getFavoriteIds(session);

    
    assertThat(result).containsExactlyInAnyOrder(1L, 2L, 3L);
  }

  @Test
  void getFavoriteIds_EmptySession_ShouldReturnEmptySet() {
    
    when(session.getAttribute(FAVORITES_SESSION_KEY)).thenReturn(null);

    
    Set<Long> result = favoritesService.getFavoriteIds(session);

    
    assertThat(result).isEmpty();
  }

  @Test
  void getFavoriteTasks_ShouldReturnListOfTasks() {
    
    Set<Long> favoriteIds = new HashSet<>(Set.of(1L, 2L));
    when(session.getAttribute(FAVORITES_SESSION_KEY)).thenReturn(favoriteIds);
    when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

    Task task2 = new Task(2L, "Task 2", "Desc", false,
      LocalDateTime.now(), LocalDate.of(2026, 12, 31),
      Priority.MEDIUM, Set.of());
    when(taskRepository.findById(2L)).thenReturn(Optional.of(task2));

    
    List<Task> result = favoritesService.getFavoriteTasks(session);

    
    assertThat(result).hasSize(2);
    assertThat(result).extracting(Task::getId).containsExactlyInAnyOrder(1L, 2L);
  }

  @Test
  void getFavoriteTasks_EmptySession_ShouldReturnEmptyList() {
    
    when(session.getAttribute(FAVORITES_SESSION_KEY)).thenReturn(null);

    
    List<Task> result = favoritesService.getFavoriteTasks(session);

    
    assertThat(result).isEmpty();
    verify(taskRepository, never()).findById(any());
  }

  @Test
  void addToFavorites_TaskNotFound_ShouldThrowTaskNotFoundException() {
    
    when(taskRepository.existsById(999L)).thenReturn(false);

    
    assertThatThrownBy(() -> favoritesService.addToFavorites(999L, session))
      .isInstanceOf(TaskNotFoundException.class)
      .hasMessageContaining("Task not found: 999");

    verify(session, never()).setAttribute(any(), any());
  }

  @Test
  void removeFromFavorites_TaskNotFound_ShouldThrowTaskNotFoundException() {
    
    when(taskRepository.existsById(999L)).thenReturn(false);

    
    assertThatThrownBy(() -> favoritesService.removeFromFavorites(999L, session))
      .isInstanceOf(TaskNotFoundException.class)
      .hasMessageContaining("Task not found: 999");

    verify(session, never()).setAttribute(any(), any());
  }
}