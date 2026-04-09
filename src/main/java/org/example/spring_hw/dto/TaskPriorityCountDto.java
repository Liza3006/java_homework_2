package org.example.spring_hw.dto;

import org.example.spring_hw.model.Priority;

public record TaskPriorityCountDto(Priority priority, long count) {
}

