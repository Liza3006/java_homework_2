package org.example.spring_hw.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.spring_hw.dto.TaskCreateDto;
import org.example.spring_hw.dto.TaskResponseDto;
import org.example.spring_hw.model.Priority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskApiIntegrationTest {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private ObjectMapper objectMapper;

  private String baseUrl;
  private String tasksUrl;
  private String attachmentsUrl;
  private String favoritesUrl;
  private String preferencesUrl;

  @BeforeEach
  void setUp() {
    baseUrl = "http://localhost:" + port;
    tasksUrl = baseUrl + "/api/tasks";
    attachmentsUrl = baseUrl + "/api/attachments";
    favoritesUrl = baseUrl + "/api/favorites";
    preferencesUrl = baseUrl + "/api/preferences";
  }

  

  @Test
  void createTask_ValidData_ShouldReturnCreatedTask() {
    
    TaskCreateDto createDto = new TaskCreateDto();
    createDto.setTitle("Integration Test Task");
    createDto.setDescription("Test Description");
    createDto.setDueDate(LocalDate.of(2026, 12, 31));
    createDto.setPriority(Priority.HIGH);
    createDto.setTags(Set.of("test", "integration"));

    
    ResponseEntity<TaskResponseDto> response = restTemplate.postForEntity(
      tasksUrl, createDto, TaskResponseDto.class);

    
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getId()).isNotNull();
    assertThat(response.getBody().getTitle()).isEqualTo("Integration Test Task");
    assertThat(response.getBody().isCompleted()).isFalse();
    assertThat(response.getHeaders().getFirst("X-API-Version")).isEqualTo("2.0.0");
  }

  @Test
  void createTask_InvalidData_ShouldReturnBadRequest() {
    
    TaskCreateDto invalidDto = new TaskCreateDto();
    invalidDto.setTitle("12"); 
    invalidDto.setDueDate(LocalDate.of(2020, 1, 1)); 

    
    ResponseEntity<Map> response = restTemplate.postForEntity(
      tasksUrl, invalidDto, Map.class);

    
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().get("status")).isEqualTo(400);
    assertThat(response.getBody().get("error")).isEqualTo("Validation Failed");
  }

  @Test
  void getAllTasks_ShouldReturnListWithHeaders() {
    
    ResponseEntity<TaskResponseDto[]> response = restTemplate.getForEntity(
      tasksUrl, TaskResponseDto[].class);

    
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getHeaders().getFirst("X-Total-Count")).isNotNull();
    assertThat(response.getHeaders().getFirst("X-API-Version")).isEqualTo("2.0.0");
  }

  @Test
  void getTaskById_ExistingId_ShouldReturnTask() {
    
    TaskCreateDto createDto = new TaskCreateDto();
    createDto.setTitle("Task for Get");
    createDto.setDueDate(LocalDate.of(2026, 12, 31));
    createDto.setPriority(Priority.MEDIUM);

    ResponseEntity<TaskResponseDto> createResponse = restTemplate.postForEntity(
      tasksUrl, createDto, TaskResponseDto.class);
    Long id = createResponse.getBody().getId();

    
    ResponseEntity<TaskResponseDto> getResponse = restTemplate.getForEntity(
      tasksUrl + "/" + id, TaskResponseDto.class);

    
    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(getResponse.getBody().getTitle()).isEqualTo("Task for Get");
  }

  @Test
  void getTaskById_NonExistingId_ShouldReturnNotFound() {
    
    ResponseEntity<Map> response = restTemplate.getForEntity(
      tasksUrl + "/99999", Map.class);

    
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void updateTask_ValidData_ShouldReturnUpdatedTask() {
    
    TaskCreateDto createDto = new TaskCreateDto();
    createDto.setTitle("Task to Update");
    createDto.setDueDate(LocalDate.of(2026, 12, 31));
    createDto.setPriority(Priority.LOW);

    ResponseEntity<TaskResponseDto> createResponse = restTemplate.postForEntity(
      tasksUrl, createDto, TaskResponseDto.class);
    Long id = createResponse.getBody().getId();

    
    Map<String, Object> updateDto = Map.of(
      "title", "Updated Task",
      "completed", true,
      "priority", "HIGH"
    );

    HttpEntity<Map<String, Object>> request = new HttpEntity<>(updateDto);
    ResponseEntity<TaskResponseDto> updateResponse = restTemplate.exchange(
      tasksUrl + "/" + id, HttpMethod.PUT, request, TaskResponseDto.class);

    
    assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(updateResponse.getBody().getTitle()).isEqualTo("Updated Task");
    assertThat(updateResponse.getBody().isCompleted()).isTrue();
    assertThat(updateResponse.getBody().getPriority()).isEqualTo(Priority.HIGH);
  }

  @Test
  void deleteTask_ExistingId_ShouldReturnNoContent() {
    
    TaskCreateDto createDto = new TaskCreateDto();
    createDto.setTitle("Task to Delete");
    createDto.setDueDate(LocalDate.of(2026, 12, 31));
    createDto.setPriority(Priority.MEDIUM);

    ResponseEntity<TaskResponseDto> createResponse = restTemplate.postForEntity(
      tasksUrl, createDto, TaskResponseDto.class);
    Long id = createResponse.getBody().getId();

    
    ResponseEntity<Void> deleteResponse = restTemplate.exchange(
      tasksUrl + "/" + id, HttpMethod.DELETE, null, Void.class);

    
    assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    
    ResponseEntity<Map> getResponse = restTemplate.getForEntity(
      tasksUrl + "/" + id, Map.class);
    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  

  @Test
  void uploadAttachment_ValidFile_ShouldReturnCreated() {
    
    TaskCreateDto createDto = new TaskCreateDto();
    createDto.setTitle("Task for Attachment");
    createDto.setDueDate(LocalDate.of(2026, 12, 31));
    createDto.setPriority(Priority.MEDIUM);

    ResponseEntity<TaskResponseDto> taskResponse = restTemplate.postForEntity(
      tasksUrl, createDto, TaskResponseDto.class);
    Long taskId = taskResponse.getBody().getId();

    
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", new ByteArrayResource("test content".getBytes()) {
      @Override
      public String getFilename() {
        return "test.txt";
      }
    });

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

    
    ResponseEntity<Map> response = restTemplate.exchange(
      tasksUrl + "/" + taskId + "/attachments",
      HttpMethod.POST, request, Map.class);

    
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().get("fileName")).isEqualTo("test.txt");
  }

  @Test
  void getTaskAttachments_ShouldReturnList() {
    
    TaskCreateDto createDto = new TaskCreateDto();
    createDto.setTitle("Task with Attachments");
    createDto.setDueDate(LocalDate.of(2026, 12, 31));
    createDto.setPriority(Priority.MEDIUM);

    ResponseEntity<TaskResponseDto> taskResponse = restTemplate.postForEntity(
      tasksUrl, createDto, TaskResponseDto.class);
    Long taskId = taskResponse.getBody().getId();

    
    ResponseEntity<List> response = restTemplate.getForEntity(
      tasksUrl + "/" + taskId + "/attachments", List.class);

    
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
  }

  

  @Test
  void addToFavorites_ValidTask_ShouldReturnCreated() {
    
    TaskCreateDto createDto = new TaskCreateDto();
    createDto.setTitle("Favorite Task");
    createDto.setDueDate(LocalDate.of(2026, 12, 31));
    createDto.setPriority(Priority.HIGH);

    ResponseEntity<TaskResponseDto> taskResponse = restTemplate.postForEntity(
      tasksUrl, createDto, TaskResponseDto.class);
    Long taskId = taskResponse.getBody().getId();

    
    ResponseEntity<Void> response = restTemplate.exchange(
      favoritesUrl + "/" + taskId, HttpMethod.POST, null, Void.class);

    
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    
    ResponseEntity<List> favoritesResponse = restTemplate.getForEntity(
      favoritesUrl, List.class);
    assertThat(favoritesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void removeFromFavorites_ShouldReturnNoContent() {
    
    TaskCreateDto createDto = new TaskCreateDto();
    createDto.setTitle("Remove from Favorites");
    createDto.setDueDate(LocalDate.of(2026, 12, 31));
    createDto.setPriority(Priority.LOW);

    ResponseEntity<TaskResponseDto> taskResponse = restTemplate.postForEntity(
      tasksUrl, createDto, TaskResponseDto.class);
    Long taskId = taskResponse.getBody().getId();

    
    restTemplate.exchange(favoritesUrl + "/" + taskId, HttpMethod.POST, null, Void.class);

    
    ResponseEntity<Void> response = restTemplate.exchange(
      favoritesUrl + "/" + taskId, HttpMethod.DELETE, null, Void.class);

    
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }

  

  @Test
  void setViewPreference_ShouldSetCookie() {
    
    ResponseEntity<Map> response = restTemplate.exchange(
      preferencesUrl + "/view?mode=compact",
      HttpMethod.POST, null, Map.class);

    
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().get("viewMode")).isEqualTo("compact");
  }

  @Test
  void getViewPreference_ShouldReturnValue() {
    
    restTemplate.exchange(preferencesUrl + "/view?mode=detailed", HttpMethod.POST, null, Void.class);

    
    ResponseEntity<Map> response = restTemplate.getForEntity(
      preferencesUrl + "/view", Map.class);

    
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().get("viewMode")).isEqualTo("detailed");
  }
  

  @Test
  void swaggerUi_ShouldBeAccessible() {
    
    ResponseEntity<String> response = restTemplate.getForEntity(
      baseUrl + "/swagger-ui.html", String.class);

    
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void openApiDocs_ShouldBeAccessible() {
    
    ResponseEntity<String> response = restTemplate.getForEntity(
      baseUrl + "/v3/api-docs", String.class);

    
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).contains("openapi");
  }
}