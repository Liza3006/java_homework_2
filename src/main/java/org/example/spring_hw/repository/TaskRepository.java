package org.example.spring_hw.repository;

import org.example.spring_hw.model.Priority;
import org.example.spring_hw.model.Task;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

  List<Task> findByCompletedAndPriority(boolean completed, Priority priority);

  @EntityGraph(attributePaths = "attachments")
  @Query("""
    select t
    from Task t
    """)
  List<Task> findAllWithAttachments();

  default List<Task> findTasksDueWithinNext7Days() {
	return findTasksDueWithinNext7DaysUntil(LocalDate.now().plusDays(7));
  }

  @Query("""
	select t
	from Task t
	where t.dueDate is not null
	  and t.dueDate between CURRENT_DATE and :endDate
	""")
  List<Task> findTasksDueWithinNext7DaysUntil(@Param("endDate") LocalDate endDate);
}