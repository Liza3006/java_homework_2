package org.example.spring_hw.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import java.util.List;
import org.example.spring_hw.client.ExternalTasksClient;
import org.example.spring_hw.dto.TaskRequest;
import org.example.spring_hw.dto.TaskResponse;
import org.example.spring_hw.exception.TaskNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class TasksGatewayService {
  private final ExternalTasksClient client;

  public TasksGatewayService(ExternalTasksClient client) {
    this.client = client;
  }

  @RateLimiter(name = "externalApi")
  @CircuitBreaker(name = "externalApi", fallbackMethod = "createFallback")
  public ExternalTasksClient.CreatedTask create(TaskRequest request) {
    return client.create(request);
  }

  @RateLimiter(name = "externalApi")
  @CircuitBreaker(name = "externalApi", fallbackMethod = "getByIdFallback")
  public TaskResponse getById(Long id) {
    return client.getById(id);
  }

  @RateLimiter(name = "externalApi")
  @CircuitBreaker(name = "externalApi", fallbackMethod = "findAllFallback")
  public List<TaskResponse> findAll(Boolean completed, Integer limit) {
    return client.findAll(completed, limit);
  }

  @RateLimiter(name = "externalApi")
  @CircuitBreaker(name = "externalApi", fallbackMethod = "deleteFallback")
  public boolean delete(Long id) {
    client.delete(id);
    return true;
  }

  @RateLimiter(name = "externalApi")
  @CircuitBreaker(name = "externalApi", fallbackMethod = "unstableFallback")
  public TaskResponse unstable(String mode) {
    return client.unstable(mode);
  }

  public ExternalTasksClient.CreatedTask createFallback(TaskRequest request, Throwable cause) {
    rethrowBusinessErrors(cause);
    return new ExternalTasksClient.CreatedTask(TaskResponse.degraded("External task creation is temporarily unavailable"), null);
  }

  public TaskResponse getByIdFallback(Long id, Throwable cause) {
    rethrowBusinessErrors(cause);
    return TaskResponse.degraded("External task lookup is temporarily unavailable");
  }

  public List<TaskResponse> findAllFallback(Boolean completed, Integer limit, Throwable cause) {
    rethrowBusinessErrors(cause);
    return List.of(TaskResponse.degraded("External task list is temporarily unavailable"));
  }

  public boolean deleteFallback(Long id, Throwable cause) {
    rethrowBusinessErrors(cause);
    return false;
  }

  public TaskResponse unstableFallback(String mode, Throwable cause) {
    rethrowBusinessErrors(cause);
    return TaskResponse.degraded("External unstable endpoint fallback for mode=" + mode);
  }

  private void rethrowBusinessErrors(Throwable cause) {
    if (cause instanceof TaskNotFoundException taskNotFoundException) {
      throw taskNotFoundException;
    }
    if (cause instanceof RequestNotPermitted requestNotPermitted) {
      throw requestNotPermitted;
    }
  }
}
