package org.example.spring_hw.exception;

public class AttachmentNotFoundException extends RuntimeException {
  public AttachmentNotFoundException(String message) {
    super(message);
  }
}