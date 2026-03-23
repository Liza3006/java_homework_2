package org.example.spring_hw.controller;

import org.example.spring_hw.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TaskControllerTest {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  private String baseUrl;

  @BeforeEach
  void setUp() {
    baseUrl = "http://localhost:" + port + "/api/tasks";
  }

  @Test
  void getAllTasks() {
    ResponseEntity<Task[]> response = restTemplate.getForEntity(baseUrl, Task[].class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
  }

  @Test
  void getTaskById_existingId() {
    Task newTask = new Task(null, "Test", "Desc", false);
    ResponseEntity<Task> createResponse = restTemplate.postForEntity(baseUrl, newTask, Task.class);
    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    Long id = createResponse.getBody().getId();

    ResponseEntity<Task> getResponse = restTemplate.getForEntity(baseUrl + "/" + id, Task.class);
    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(getResponse.getBody().getTitle()).isEqualTo("Test");
  }

  @Test
  void getTaskById() {
    ResponseEntity<Task> response = restTemplate.getForEntity(baseUrl + "/9999", Task.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void createTask() {
    Task newTask = new Task(null, "New Task", "Description", false);
    ResponseEntity<Task> response = restTemplate.postForEntity(baseUrl, newTask, Task.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody().getId()).isNotNull();
    assertThat(response.getBody().getTitle()).isEqualTo("New Task");
  }

  @Test
  void updateTask() {
    Task newTask = new Task(null, "Original", "Desc", false);
    ResponseEntity<Task> createResponse = restTemplate.postForEntity(baseUrl, newTask, Task.class);
    Long id = createResponse.getBody().getId();

    Task updatedTask = new Task(id, "Updated", "New Desc", true);
    HttpEntity<Task> requestEntity = new HttpEntity<>(updatedTask);
    ResponseEntity<Task> updateResponse = restTemplate.exchange(
      baseUrl + "/" + id, HttpMethod.PUT, requestEntity, Task.class);
    assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(updateResponse.getBody().getTitle()).isEqualTo("Updated");
    assertThat(updateResponse.getBody().isCompleted()).isTrue();
  }

  @Test
  void updateTask_2() {
    Task updatedTask = new Task(9999L, "Updated", "Desc", true);
    HttpEntity<Task> requestEntity = new HttpEntity<>(updatedTask);
    ResponseEntity<Task> response = restTemplate.exchange(
      baseUrl + "/9999", HttpMethod.PUT, requestEntity, Task.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void deleteTask() {
    Task newTask = new Task(null, "ToDelete", "Desc", false);
    ResponseEntity<Task> createResponse = restTemplate.postForEntity(baseUrl, newTask, Task.class);
    Long id = createResponse.getBody().getId();

    ResponseEntity<Void> deleteResponse = restTemplate.exchange(
      baseUrl + "/" + id, HttpMethod.DELETE, null, Void.class);
    assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    ResponseEntity<Task> getResponse = restTemplate.getForEntity(baseUrl + "/" + id, Task.class);
    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void deleteTask_2() {
    ResponseEntity<Void> response = restTemplate.exchange(
      baseUrl + "/9999", HttpMethod.DELETE, null, Void.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}