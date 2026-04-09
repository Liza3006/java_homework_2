package org.example.spring_hw.repository;

import org.example.spring_hw.model.TaskAttachment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Long> {

  @EntityGraph(attributePaths = "task")
  List<TaskAttachment> findByTaskId(Long taskId);
}