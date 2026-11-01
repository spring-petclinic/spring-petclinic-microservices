-- Insert only: rows are never updated or deleted by this service.
CREATE TABLE IF NOT EXISTS audit_record (
  id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  event_id    UUID NOT NULL UNIQUE,
  event_type  TEXT NOT NULL,
  severity    TEXT NOT NULL,
  visit_id    INTEGER NOT NULL,
  pet_id      INTEGER NOT NULL,
  owner_id    INTEGER NOT NULL,
  visit_date  DATE NOT NULL,
  visit_type  TEXT NOT NULL,
  description TEXT NOT NULL,
  occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
  received_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS audit_record_pet_id ON audit_record (pet_id);
CREATE INDEX IF NOT EXISTS audit_record_owner_id ON audit_record (owner_id);
