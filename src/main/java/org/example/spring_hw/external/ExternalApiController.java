package org.example.spring_hw.external;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.example.spring_hw.dto.TaskRequest;
import org.example.spring_hw.dto.TaskResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/external/v1")
public class ExternalApiController {
  private final Map<Long, TaskResponse> tasks = new ConcurrentHashMap<>();
  private final AtomicLong ids = new AtomicLong(1);

  @PostMapping("/tasks")
  public ResponseEntity<TaskResponse> create(@RequestBody TaskRequest request) {
    Long id = ids.getAndIncrement();
    TaskResponse response = new TaskResponse(id, request.getTitle(), request.getDescription(), request.isCompleted());
    tasks.put(id, response);
    return ResponseEntity.created(URI.create("/external/v1/tasks/" + id)).body(response);
  }

  @GetMapping("/tasks/{id}")
  public ResponseEntity<?> getById(@PathVariable Long id) {
    TaskResponse task = tasks.get(id);
    if (task == null) {
      return notFound(id);
    }
    return ResponseEntity.ok(task);
  }

  @GetMapping("/tasks")
  public List<TaskResponse> findAll(@RequestParam(required = false) Boolean completed,
                                    @RequestParam(required = false) Integer limit) {
    return tasks.values().stream()
        .filter(task -> completed == null || task.isCompleted() == completed)
        .limit(limit == null ? Long.MAX_VALUE : limit)
        .collect(Collectors.toList());
  }

  @PutMapping("/tasks/{id}")
  public ResponseEntity<?> update(@PathVariable Long id, @RequestBody TaskRequest request) {
    if (!tasks.containsKey(id)) {
      return notFound(id);
    }
    TaskResponse response = new TaskResponse(id, request.getTitle(), request.getDescription(), request.isCompleted());
    tasks.put(id, response);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/tasks/{id}")
  public ResponseEntity<?> delete(@PathVariable Long id) {
    TaskResponse removed = tasks.remove(id);
    if (removed == null) {
      return notFound(id);
    }
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/unstable")
  public ResponseEntity<?> unstable(@RequestParam(defaultValue = "500") String mode) throws InterruptedException {
    if ("timeout".equals(mode)) {
      TimeUnit.SECONDS.sleep(3);
      return ResponseEntity.ok(new TaskResponse(1L, "late response", "timeout mode", false));
    }
    if ("429".equals(mode)) {
      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
          .header(HttpHeaders.RETRY_AFTER, "5")
          .body(problem(HttpStatus.TOO_MANY_REQUESTS, "External rate limit"));
    }
    if ("html".equals(mode)) {
      return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
          .contentType(MediaType.TEXT_HTML)
          .body("<html><body><h1>Bad Gateway</h1></body></html>");
    }
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(problem(HttpStatus.INTERNAL_SERVER_ERROR, "Simulated external failure"));
  }

  private ResponseEntity<ProblemDetail> notFound(Long id) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(problem(HttpStatus.NOT_FOUND, "Task " + id + " was not found"));
  }

  private ProblemDetail problem(HttpStatus status, String detail) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
    problem.setTitle(status.getReasonPhrase());
    return problem;
  }
}
