# Job Application Tracking System

A Java-based web application for managing and tracking job applications in one place.

## Features

- Add new job applications
- View all saved applications
- Update application details and status
- Delete applications
- Track statuses: Applied, Assessment, Interview, Selected, and Rejected
- Store company name, job role, application date, and job description
- Search and filter applications
- Dashboard statistics for application tracking
- MySQL database integration

## Tech Stack

- **Backend:** Java, Java Servlets, JDBC
- **Frontend:** HTML, CSS, JavaScript
- **Database:** MySQL
- **Server:** Apache Tomcat
- **Database Driver:** MySQL Connector/J

## Project Structure

```text
JobApplicationTrackingSystem/
├── .gitignore
├── lib/
├── src/
└── web-app/
    ├── index.html
    ├── style.css
    ├── script.js
    └── WEB-INF/
        ├── web.xml
        ├── src/
        ├── classes/
        └── lib/
```

## Database Setup

1. Install MySQL Server.
2. Create a database named `job_tracker`.
3. Create an `applications` table with the required fields:

```sql
CREATE DATABASE job_tracker;
USE job_tracker;

CREATE TABLE applications (
    id INT PRIMARY KEY AUTO_INCREMENT,
    company_name VARCHAR(100) NOT NULL,
    job_role VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL,
    application_date DATE,
    job_description TEXT
);
```

## Configuration

Configure the `JOBTRACKER_DB_PASSWORD` environment variable with your MySQL password before starting the application.

Never store database passwords or other secrets directly in source code or commit them to GitHub.

## Running the Application

1. Install a compatible JDK, MySQL Server, and Apache Tomcat.
2. Configure the database and environment variable.
3. Configure the application with your database username and database URL.
4. Compile and deploy the servlet and web application to Tomcat.
5. Start Tomcat and open the application in your browser.

## Learning Objectives

This project demonstrates Java programming, JDBC database connectivity, CRUD operations, servlet-based web development, and frontend integration.

## Author

Developed as a learning project to practice full-stack Java web development.
