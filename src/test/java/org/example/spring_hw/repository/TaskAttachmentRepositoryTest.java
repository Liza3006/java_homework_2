package org.example.spring_hw.repository;

import jakarta.persistence.EntityManager;
import org.example.spring_hw.config.JpaAuditingConfig;
import org.example.spring_hw.model.Priority;
import org.example.spring_hw.model.Task;
import org.example.spring_hw.model.TaskAttachment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaAuditingConfig.class)
class TaskAttachmentRepositoryTest {

  @Autowired
  private TaskRepository taskRepository;

  @Autowired
  private TaskAttachmentRepository taskAttachmentRepository;

  @Autowired
  private EntityManager entityManager;

  @Test
  void findByTaskId_ShouldReturnAttachmentsForGivenTask() {
    Task task = taskRepository.save(new Task(null, "Task with files", null, false,
      LocalDateTime.now(), LocalDate.now().plusDays(5), Priority.HIGH, Set.of("files")));

    TaskAttachment first = taskAttachmentRepository.save(TaskAttachment.builder()
      .task(task)
      .fileName("one.txt")
      .storedFileName("one-stored.txt")
      .contentType("text/plain")
      .size(10L)
      .uploadedAt(LocalDateTime.now())
      .build());

    TaskAttachment second = taskAttachmentRepository.save(TaskAttachment.builder()
      .task(task)
      .fileName("two.txt")
      .storedFileName("two-stored.txt")
      .contentType("text/plain")
      .size(11L)
      .uploadedAt(LocalDateTime.now())
      .build());

    List<TaskAttachment> result = taskAttachmentRepository.findByTaskId(task.getId());

    assertThat(result).hasSize(2);
    assertThat(result).extracting(TaskAttachment::getId).containsExactlyInAnyOrder(first.getId(), second.getId());
  }

  @Test
  void findByTaskId_ShouldReturnOnlyAttachmentsForRequestedTask() {
    Task firstTask = taskRepository.save(new Task(null, "First task", null, false,
      LocalDateTime.now(), LocalDate.now().plusDays(2), Priority.HIGH, Set.of("first")));
    Task secondTask = taskRepository.save(new Task(null, "Second task", null, false,
      LocalDateTime.now(), LocalDate.now().plusDays(3), Priority.LOW, Set.of("second")));

    taskAttachmentRepository.save(TaskAttachment.builder()
      .task(firstTask)
      .fileName("first.txt")
      .storedFileName("first-stored.txt")
      .contentType("text/plain")
      .size(100L)
      .uploadedAt(LocalDateTime.now())
      .build());

    taskAttachmentRepository.save(TaskAttachment.builder()
      .task(secondTask)
      .fileName("second.txt")
      .storedFileName("second-stored.txt")
      .contentType("text/plain")
      .size(200L)
      .uploadedAt(LocalDateTime.now())
      .build());

    entityManager.clear();

    List<TaskAttachment> result = taskAttachmentRepository.findByTaskId(firstTask.getId());

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getFileName()).isEqualTo("first.txt");
    assertThat(result.get(0).getTask().getId()).isEqualTo(firstTask.getId());
  }

  @Test
  void saveAttachment_WithExistingTask_ShouldPersistTaskRelationship() {
    Task task = taskRepository.save(new Task(null, "Task to attach file", null, false,
      LocalDateTime.now(), LocalDate.now().plusDays(5), Priority.MEDIUM, Set.of("attachment")));

    TaskAttachment savedAttachment = taskAttachmentRepository.save(TaskAttachment.builder()
      .task(task)
      .fileName("contract.pdf")
      .storedFileName("contract-123.pdf")
      .contentType("application/pdf")
      .size(1024L)
      .uploadedAt(LocalDateTime.now())
      .build());

    entityManager.clear();

    List<TaskAttachment> attachments = taskAttachmentRepository.findByTaskId(task.getId());

    assertThat(attachments).hasSize(1);
    assertThat(attachments.get(0).getId()).isEqualTo(savedAttachment.getId());
    assertThat(attachments.get(0).getTask().getId()).isEqualTo(task.getId());
  }
}

