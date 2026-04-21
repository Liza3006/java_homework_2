package org.example.spring_hw.dto;

public class TaskResponse {
  private Long id;
  private String title;
  private String description;
  private boolean completed;
  private String message;

  public TaskResponse() {
  }

  public TaskResponse(Long id, String title, String description, boolean completed) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.completed = completed;
  }

  public static TaskResponse degraded(String message) {
    TaskResponse response = new TaskResponse();
    response.setId(-1L);
    response.setTitle("Temporarily unavailable");
    response.setDescription("Fallback response");
    response.setMessage(message);
    return response;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isCompleted() {
    return completed;
  }

  public void setCompleted(boolean completed) {
    this.completed = completed;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
