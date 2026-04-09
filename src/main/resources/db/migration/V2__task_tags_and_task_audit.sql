ALTER TABLE tasks
    ADD COLUMN updated_at TIMESTAMP;

CREATE TABLE task_tags (
    task_id BIGINT NOT NULL,
    tag VARCHAR(255) NOT NULL,
    CONSTRAINT fk_task_tags_task
      FOREIGN KEY (task_id)
      REFERENCES tasks (id)
      ON DELETE CASCADE
);

CREATE INDEX idx_task_tags_task_id ON task_tags (task_id);

