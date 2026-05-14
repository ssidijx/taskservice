ALTER TABLE tasks
    ADD COLUMN external_id VARCHAR(100);

UPDATE tasks SET external_id = 'legacy-' || id WHERE external_id IS NULL;

ALTER TABLE tasks
    ALTER COLUMN external_id SET NOT NULL,
    ADD CONSTRAINT uq_tasks_external_id UNIQUE (external_id);