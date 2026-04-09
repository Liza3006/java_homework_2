package org.example.spring_hw.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.spring_hw.model.TaskAttachment;

import java.util.List;

/**
 * Legacy helper, kept only as a non-Spring class while the repository method is handled by Spring Data.
 */
class TaskAttachmentRepositoryImpl {

  @PersistenceContext
  private EntityManager entityManager;

  public List<TaskAttachment> findByTaskId(Long taskId) {
    return entityManager.createQuery(
        "select a from TaskAttachment a where a.task.id = :taskId",
        TaskAttachment.class)
      .setParameter("taskId", taskId)
      .getResultList();
  }
}



