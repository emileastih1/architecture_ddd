CREATE TABLE DOCUMENT (
  ID serial PRIMARY KEY NOT NULL,
  NAME VARCHAR(200) NOT NULL,
  FILE_SIZE VARCHAR(100),
  OWNER_ID INTEGER,
  UNIT_OF_MEASUREMENT VARCHAR(10),
  LOCATION VARCHAR(10),
  CONTENT BYTEA,
  CREATION_DATE timestamp with time zone DEFAULT Now(),
  MODIFICATION_DATE timestamp with time zone,
  FOREIGN KEY (OWNER_ID) REFERENCES USERS(ID)
);