package org.example.spring_hw.service;

import org.example.spring_hw.exception.TaskNotFoundException;
import org.example.spring_hw.model.Task;
import org.example.spring_hw.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FavoritesService {

  private static final String FAVORITES_SESSION_KEY = "favoriteTaskIds";

  private final TaskRepository taskRepository;

  @Autowired
  public FavoritesService(TaskRepository taskRepository) {
    this.taskRepository = taskRepository;
  }

  @SuppressWarnings("unchecked")
  private Set<Long> getFavoritesSet(HttpSession session) {
    Set<Long> favorites = (Set<Long>) session.getAttribute(FAVORITES_SESSION_KEY);
    if (favorites == null) {
      favorites = new HashSet<>();
      session.setAttribute(FAVORITES_SESSION_KEY, favorites);
    }
    return favorites;
  }

  public void addToFavorites(Long taskId, HttpSession session) {
    if (!taskRepository.existsById(taskId)) {
      throw new TaskNotFoundException("Task not found: " + taskId);
    }
    Set<Long> favorites = getFavoritesSet(session);
    if (favorites.add(taskId)) {
      session.setAttribute(FAVORITES_SESSION_KEY, favorites);
    }
  }

  public void removeFromFavorites(Long taskId, HttpSession session) {
    if (!taskRepository.existsById(taskId)) {
      throw new TaskNotFoundException("Task not found: " + taskId);
    }
    Set<Long> favorites = getFavoritesSet(session);
    if (favorites.remove(taskId)) {
      session.setAttribute(FAVORITES_SESSION_KEY, favorites);
    }
  }

  public Set<Long> getFavoriteIds(HttpSession session) {
    return new HashSet<>(getFavoritesSet(session));
  }

  public List<Task> getFavoriteTasks(HttpSession session) {
    Set<Long> favoriteIds = getFavoritesSet(session);
    return favoriteIds.stream()
      .map(taskRepository::findById)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }
}