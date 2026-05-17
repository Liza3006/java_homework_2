package org.example.spring_hw.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.example.spring_hw.config.JpaAuditingConfig;
import org.example.spring_hw.model.Priority;
import org.example.spring_hw.model.Task;
import org.example.spring_hw.model.TaskAttachment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaAuditingConfig.class)
@Testcontainers
class TaskRepositoryIntegrationTest {

  @Container
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15-alpine")
    .withDatabaseName("spring_hw_test")
    .withUsername("spring")
    .withPassword("spring");

  @DynamicPropertySource
  static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
  }

  @Autowired
  private TaskRepository taskRepository;

  @Autowired
  private TaskAttachmentRepository taskAttachmentRepository;

  @Autowired
  private EntityManagerFactory entityManagerFactory;

  @Autowired
  private EntityManager entityManager;

  @Test
  void findByCompletedAndPriority_ShouldReturnMatchingTasks() {
    taskRepository.save(new Task(null, "Done high", null, true,
      LocalDateTime.now(), LocalDate.now().plusDays(2), Priority.HIGH, Set.of("a")));
    taskRepository.save(new Task(null, "Todo high", null, false,
      LocalDateTime.now(), LocalDate.now().plusDays(3), Priority.HIGH, Set.of("b")));
    taskRepository.save(new Task(null, "Todo low", null, false,
      LocalDateTime.now(), LocalDate.now().plusDays(4), Priority.LOW, Set.of("c")));

    List<Task> result = taskRepository.findByCompletedAndPriority(false, Priority.HIGH);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getTitle()).isEqualTo("Todo high");
  }

  @Test
  void findTasksDueWithinNext7Days_ShouldReturnOnlyTasksInsideRange() {
    taskRepository.save(new Task(null, "Due today", null, false,
      LocalDateTime.now(), LocalDate.now(), Priority.MEDIUM, Set.of("today")));
    taskRepository.save(new Task(null, "Due in 7 days", null, false,
      LocalDateTime.now(), LocalDate.now().plusDays(7), Priority.MEDIUM, Set.of("seven")));
    taskRepository.save(new Task(null, "Due in 8 days", null, false,
      LocalDateTime.now(), LocalDate.now().plusDays(8), Priority.MEDIUM, Set.of("eight")));
    taskRepository.save(new Task(null, "No due date", null, false,
      LocalDateTime.now(), null, Priority.MEDIUM, Set.of("none")));

    List<Task> result = taskRepository.findTasksDueWithinNext7Days();

    assertThat(result).extracting(Task::getTitle)
      .contains("Due today", "Due in 7 days")
      .doesNotContain("Due in 8 days", "No due date");
  }

  @Test
  void findTasksDueWithinNext7DaysUntil_ShouldRespectProvidedEndDate() {
    LocalDate today = LocalDate.now();

    taskRepository.save(new Task(null, "Inside custom range", null, false,
      LocalDateTime.now(), today.plusDays(3), Priority.MEDIUM, Set.of("inside")));
    taskRepository.save(new Task(null, "Outside custom range", null, false,
      LocalDateTime.now(), today.plusDays(5), Priority.MEDIUM, Set.of("outside")));
    taskRepository.save(new Task(null, "Without due date", null, false,
      LocalDateTime.now(), null, Priority.MEDIUM, Set.of("none")));

    List<Task> result = taskRepository.findTasksDueWithinNext7DaysUntil(today.plusDays(4));

    assertThat(result).extracting(Task::getTitle)
      .contains("Inside custom range")
      .doesNotContain("Outside custom range", "Without due date");
  }

  @Test
  void findTasksDueWithinNext7DaysUntil_WhenNothingMatches_ShouldReturnEmptyList() {
    LocalDate today = LocalDate.now();

    taskRepository.save(new Task(null, "Far future", null, false,
      LocalDateTime.now(), today.plusDays(30), Priority.LOW, Set.of("future")));
    taskRepository.save(new Task(null, "No date", null, false,
      LocalDateTime.now(), null, Priority.LOW, Set.of("none")));

    List<Task> result = taskRepository.findTasksDueWithinNext7DaysUntil(today.plusDays(2));

    assertThat(result).isEmpty();
  }

  @Test
  void findAllWithAttachments_ShouldLoadAttachmentsEagerly() {
    Task task = taskRepository.save(new Task(null, "Task with files", null, false,
      LocalDateTime.now(), LocalDate.now().plusDays(5), Priority.HIGH, Set.of("files")));
    taskRepository.flush();

    taskAttachmentRepository.save(TaskAttachment.builder()
      .task(task)
      .fileName("one.txt")
      .storedFileName("one-stored.txt")
      .contentType("text/plain")
      .size(10L)
      .uploadedAt(LocalDateTime.now())
      .build());
    taskAttachmentRepository.flush();

    assertThat(taskRepository.count()).isEqualTo(1);
    assertThat(taskAttachmentRepository.count()).isEqualTo(1);

    entityManager.clear();

    List<Task> result = taskRepository.findAllWithAttachments();

    assertThat(result).hasSize(1);
    assertThat(entityManagerFactory.getPersistenceUnitUtil().isLoaded(result.get(0).getAttachments())).isTrue();
    assertThat(result.get(0).getAttachments()).hasSize(1);
    assertThat(result.get(0).getAttachments().iterator().next().getFileName()).isEqualTo("one.txt");
  }
}



