package org.example.spring_hw.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.example.spring_hw.dto.TaskRequest;
import org.example.spring_hw.dto.TaskResponse;
import org.example.spring_hw.exception.ExternalApiException;
import org.example.spring_hw.exception.TaskNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;

@Component
public class ExternalTasksClient {
  private static final Logger log = LoggerFactory.getLogger(ExternalTasksClient.class);

  private final RestClient restClient;
  private final ObjectMapper objectMapper;

  public ExternalTasksClient(RestClient externalRestClient, ObjectMapper objectMapper) {
    this.restClient = externalRestClient;
    this.objectMapper = objectMapper;
  }

  public CreatedTask create(TaskRequest request) {
    ResponseEntity<TaskResponse> response = restClient.post()
        .uri("/external/v1/tasks")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .body(request)
        .retrieve()
        .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), this::handleError)
        .toEntity(TaskResponse.class);
    if (response.getStatusCode() != HttpStatus.CREATED) {
      throw new ExternalApiException("External API did not return 201 Created");
    }
    return new CreatedTask(response.getBody(), response.getHeaders().getLocation());
  }

  public TaskResponse getById(Long id) {
    return restClient.get()
        .uri("/external/v1/tasks/{id}", id)
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), this::handleError)
        .body(TaskResponse.class);
  }

  public List<TaskResponse> findAll(Boolean completed, Integer limit) {
    return restClient.get()
        .uri(uriBuilder -> {
          var builder = uriBuilder.path("/external/v1/tasks");
          if (completed != null) {
            builder.queryParam("completed", completed);
          }
          if (limit != null) {
            builder.queryParam("limit", limit);
          }
          return builder.build();
        })
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), this::handleError)
        .body(new ParameterizedTypeReference<List<TaskResponse>>() {});
  }

  public void delete(Long id) {
    ResponseEntity<Void> response = restClient.delete()
        .uri("/external/v1/tasks/{id}", id)
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), this::handleError)
        .toBodilessEntity();
    if (response.getStatusCode() != HttpStatus.NO_CONTENT) {
      throw new ExternalApiException("External API did not return 204 No Content");
    }
  }

  public TaskResponse unstable(String mode) {
    return restClient.get()
        .uri(uriBuilder -> uriBuilder.path("/external/v1/unstable").queryParam("mode", mode).build())
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), this::handleError)
        .body(TaskResponse.class);
  }

  private void handleError(org.springframework.http.HttpRequest request,
                           org.springframework.http.client.ClientHttpResponse response) throws IOException {
    byte[] body = StreamUtils.copyToByteArray(response.getBody());
    MediaType contentType = response.getHeaders().getContentType();

    if (response.getStatusCode().value() == 404) {
      throw new TaskNotFoundException(problemDetail(body));
    }

    if (contentType == null || !MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
      log.warn("External API returned unexpected contentType={} status={} body={}",
          contentType, response.getStatusCode().value(), limitedBody(body));
    }

    throw new ExternalApiException("External API returned status " + response.getStatusCode().value());
  }

  private String problemDetail(byte[] body) {
    try {
      ProblemDetail problem = objectMapper.readValue(body, ProblemDetail.class);
      return problem.getDetail();
    } catch (Exception ex) {
      return "Task was not found in external API";
    }
  }

  private String limitedBody(byte[] body) {
    String text = new String(body, StandardCharsets.UTF_8);
    if (text.length() <= 300) {
      return text;
    }
    return text.substring(0, 300);
  }

  public static class CreatedTask {
    private final TaskResponse task;
    private final URI location;

    public CreatedTask(TaskResponse task, URI location) {
      this.task = task;
      this.location = location;
    }

    public TaskResponse getTask() {
      return task;
    }

    public URI getLocation() {
      return location;
    }
  }
}
