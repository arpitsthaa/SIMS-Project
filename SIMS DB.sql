DROP DATABASE IF EXISTS sims_db;
CREATE DATABASE sims_db;
USE sims_db;

CREATE TABLE users (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    full_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(100),
    username    VARCHAR(50) UNIQUE NOT NULL,
    password    VARCHAR(255) NOT NULL,
    role        ENUM('STUDENT','TEACHER','ADMIN') NOT NULL,
    extra_field VARCHAR(100),
    last_login  TIMESTAMP NULL
);

CREATE TABLE subjects (
    id   INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE notices (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    title      VARCHAR(200) NOT NULL,
    body       TEXT,
    teacher    VARCHAR(100),
    subject    VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE attendance (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(50),
    subject    VARCHAR(100),
    teacher    VARCHAR(100),
    total      INT DEFAULT 0,
    present    INT DEFAULT 0,
    att_date   DATE NOT NULL DEFAULT (CURRENT_DATE),
    UNIQUE KEY unique_att
        (student_id, subject, att_date)
);

CREATE TABLE leave_requests (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(50),
    leave_type VARCHAR(50),
    from_date  DATE,
    to_date    DATE,
    reason     TEXT,
    status     ENUM('PENDING','APPROVED','REJECTED')
               DEFAULT 'PENDING',
    admin_note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE grades (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(50),
    subject    VARCHAR(100),
    exam_type  VARCHAR(50),
    marks      INT,
    grade      VARCHAR(5),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO users
    (full_name,email,username,password,role)
VALUES
    ('Admin','admin@sims.com','admin',
     SHA2('admin123',256),'ADMIN');

INSERT INTO users
    (full_name,email,username,password,role,extra_field)
VALUES
('Arpit Shrestha','arpit@sims.com','arpit',SHA2('123456',256),'STUDENT','STU001'),
('Pratik Sharma','pratik@sims.com','pratik',SHA2('123456',256),'STUDENT','STU002'),
('Riwaj Thapa','riwaj@sims.com','riwaj',SHA2('123456',256),'STUDENT','STU003'),
('Surya Karki','surya@sims.com','surya',SHA2('123456',256),'STUDENT','STU004'),
('Aarav Poudel','aarav@sims.com','aarav',SHA2('123456',256),'STUDENT','STU005'),
('Bikash Rai','bikash@sims.com','bikash',SHA2('123456',256),'STUDENT','STU006'),
('Suman Tamang','suman@sims.com','suman',SHA2('123456',256),'STUDENT','STU007'),
('Dipesh Gurung','dipesh@sims.com','dipesh',SHA2('123456',256),'STUDENT','STU008'),
('Niraj Basnet','niraj@sims.com','niraj',SHA2('123456',256),'STUDENT','STU009'),
('Rohit Adhikari','rohit@sims.com','rohit',SHA2('123456',256),'STUDENT','STU010'),
('Anil Bista','anil@sims.com','anil',SHA2('123456',256),'STUDENT','STU011'),
('Sunil Magar','sunil@sims.com','sunil',SHA2('123456',256),'STUDENT','STU012'),
('Kiran Lama','kiran@sims.com','kiran',SHA2('123456',256),'STUDENT','STU013'),
('Nabin Shrestha','nabin@sims.com','nabin',SHA2('123456',256),'STUDENT','STU014'),
('Prabesh Koirala','prabesh@sims.com','prabesh',SHA2('123456',256),'STUDENT','STU015'),
('Santosh Oli','santosh@sims.com','santosh',SHA2('123456',256),'STUDENT','STU016'),
('Bishal Giri','bishal@sims.com','bishal',SHA2('123456',256),'STUDENT','STU017'),
('Manish Pandey','manish@sims.com','manish',SHA2('123456',256),'STUDENT','STU018'),
('Rajesh Dahal','rajesh@sims.com','rajesh',SHA2('123456',256),'STUDENT','STU019'),
('Dinesh Chaudhary','dinesh@sims.com','dinesh',SHA2('123456',256),'STUDENT','STU020'),
('Ram Prasad','ram@sims.com','ram',SHA2('123456',256),'TEACHER','Mathematics'),
('Sita Devi','sita@sims.com','sita',SHA2('123456',256),'TEACHER','Physics'),
('Hari Bahadur','hari@sims.com','hari',SHA2('123456',256),'TEACHER','Computer Science');


