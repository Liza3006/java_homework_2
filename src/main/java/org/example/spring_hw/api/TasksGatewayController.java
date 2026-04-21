package org.example.spring_hw.api;

import java.net.URI;
import java.util.List;
import org.example.spring_hw.client.ExternalTasksClient;
import org.example.spring_hw.dto.TaskRequest;
import org.example.spring_hw.dto.TaskResponse;
import org.example.spring_hw.service.TasksGatewayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class TasksGatewayController {
  private final TasksGatewayService service;

  public TasksGatewayController(TasksGatewayService service) {
    this.service = service;
  }

  @PostMapping("/tasks")
  public ResponseEntity<TaskResponse> create(@RequestBody TaskRequest request) {
    ExternalTasksClient.CreatedTask created = service.create(request);
    URI location = created.getLocation();
    if (location == null) {
      return ResponseEntity.accepted().body(created.getTask());
    }
    return ResponseEntity.created(location).body(created.getTask());
  }

  @GetMapping("/tasks/{id}")
  public TaskResponse getById(@PathVariable Long id) {
    return service.getById(id);
  }

  @GetMapping("/tasks")
  public List<TaskResponse> findAll(@RequestParam(required = false) Boolean completed,
                                    @RequestParam(required = false) Integer limit) {
    return service.findAll(completed, limit);
  }

  @DeleteMapping("/tasks/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    boolean deleted = service.delete(id);
    if (!deleted) {
      return ResponseEntity.accepted().build();
    }
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/unstable")
  public TaskResponse unstable(@RequestParam(defaultValue = "500") String mode) {
    return service.unstable(mode);
  }
}
