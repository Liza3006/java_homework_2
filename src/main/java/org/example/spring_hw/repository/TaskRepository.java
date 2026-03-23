package org.example.spring_hw.repository;

import org.example.spring_hw.model.Task;
import java.util.List;

public interface TaskRepository {
  List<Task> findAll();
  Task findById(Long id);
  Task save(Task task);
  void deleteById(Long id);
  boolean existsById(Long id);
}