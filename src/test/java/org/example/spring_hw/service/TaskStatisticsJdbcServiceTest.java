package org.example.spring_hw.service;

import org.example.spring_hw.config.JpaAuditingConfig;
import org.example.spring_hw.dto.TaskPriorityCountDto;
import org.example.spring_hw.model.Priority;
import org.example.spring_hw.model.Task;
import org.example.spring_hw.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(JpaAuditingConfig.class)
@Transactional
class TaskStatisticsJdbcServiceTest {

  @Autowired
  private TaskRepository taskRepository;

  @Autowired
  private TaskStatisticsJdbcService taskStatisticsJdbcService;

  @Test
  void getTasksCountByPriority_ShouldReturnGroupedCounts() {
    taskRepository.deleteAllInBatch();

    taskRepository.save(new Task(null, "Low 1", null, false,
      LocalDateTime.now(), LocalDate.now().plusDays(1), Priority.LOW, Set.of("low")));
    taskRepository.save(new Task(null, "Low 2", null, false,
      LocalDateTime.now(), LocalDate.now().plusDays(2), Priority.LOW, Set.of("low")));
    taskRepository.save(new Task(null, "High 1", null, false,
      LocalDateTime.now(), LocalDate.now().plusDays(3), Priority.HIGH, Set.of("high")));

    List<TaskPriorityCountDto> result = taskStatisticsJdbcService.getTasksCountByPriority();

    assertThat(result).extracting(TaskPriorityCountDto::priority)
      .containsExactly(Priority.HIGH, Priority.LOW);
    assertThat(result).extracting(TaskPriorityCountDto::count)
      .containsExactly(1L, 2L);
  }
}


