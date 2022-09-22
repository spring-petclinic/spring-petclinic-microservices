DROP TABLE visitrequests IF EXISTS;
CREATE TABLE visitrequests (
  id INTEGER IDENTITY PRIMARY KEY,
  pet_id INTEGER NOT NULL,
  message VARCHAR(8192),
  response VARCHAR(8192),
  accepted INT
);