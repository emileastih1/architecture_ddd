ALTER TABLE users alter column ID
ADD GENERATED BY DEFAULT AS IDENTITY (MINVALUE 1 START WITH 1 INCREMENT BY 1);
