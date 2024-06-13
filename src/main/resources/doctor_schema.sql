CREATE SCHEMA cst363;
USE cst363;
CREATE TABLE doctor (
  id int NOT NULL AUTO_INCREMENT,
  last_name varchar(50) NOT NULL,
  first_name varchar(50) NOT NULL, 
  practice_since char(4) DEFAULT NULL,
  specialty varchar(25) DEFAULT NULL,
  ssn char(11) NOT NULL,
  PRIMARY KEY (id)
  );