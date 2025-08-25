-- MySQL initialization script for Talki application

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS talki_db;

-- Use the database
USE talki_db;

-- Grant privileges to the application user
GRANT ALL PRIVILEGES ON talki_db.* TO 'talki1234'@'%';
FLUSH PRIVILEGES;

-- Create tables (these will be created by JPA, but we can add any custom initialization here)

-- Insert initial data if needed
-- INSERT INTO some_table (column1, column2) VALUES ('value1', 'value2');

-- Create indexes for better performance
-- CREATE INDEX idx_created_at ON chat_messages(created_at);
-- CREATE INDEX idx_session_id ON chat_messages(session_id);

