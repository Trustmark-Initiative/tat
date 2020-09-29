CREATE DATABASE assessments;
CREATE USER 'admin'@'localhost' IDENTIFIED BY 'admin11!';
GRANT ALL PRIVILEGES ON assessments.* TO 'admin'@'localhost';
FLUSH PRIVILEGES;

