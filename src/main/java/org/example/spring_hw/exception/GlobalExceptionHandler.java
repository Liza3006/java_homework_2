package org.example.spring_hw.exception;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(TaskNotFoundException.class)
  ProblemDetail handleTaskNotFound(TaskNotFoundException ex) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    problem.setTitle("Task not found");
    return problem;
  }

  @ExceptionHandler(RequestNotPermitted.class)
  ProblemDetail handleRateLimit(RequestNotPermitted ex) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, "Too many requests to external API");
    problem.setTitle("Rate limit exceeded");
    return problem;
  }

  @ExceptionHandler(ExternalApiException.class)
  ProblemDetail handleExternalApi(ExternalApiException ex) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, ex.getMessage());
    problem.setTitle("External API error");
    return problem;
  }
}
