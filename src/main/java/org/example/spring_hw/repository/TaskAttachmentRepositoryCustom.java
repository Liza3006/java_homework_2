package org.example.spring_hw.repository;

import org.example.spring_hw.model.TaskAttachment;

import java.util.List;

public interface TaskAttachmentRepositoryCustom {

  List<TaskAttachment> findByTaskId(Long taskId);
}

