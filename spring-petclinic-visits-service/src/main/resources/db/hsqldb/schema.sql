DROP TABLE visits IF EXISTS;

CREATE TABLE visits (
  id          INTEGER IDENTITY PRIMARY KEY,
  pet_id      INTEGER NOT NULL,
  visit_date  DATE,
  description VARCHAR(8192),
  visit_type  VARCHAR(40) DEFAULT 'ROUTINE' NOT NULL,
  status      VARCHAR(20) DEFAULT 'SCHEDULED' NOT NULL
);

CREATE INDEX visits_pet_id ON visits (pet_id);
