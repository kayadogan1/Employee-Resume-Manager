# Employee Resume Manager

A Spring Boot application for managing employee resumes and candidate applications with PDF processing capabilities.

## Tech Stack

- **Framework**: Spring Boot 4.1.0
- **Language**: Java 21
- **Database**: PostgreSQL
- **Object Storage**: MinIO
- **Message Queue**: RabbitMQ
- **PDF Processing**: Apache PDFBox 3.0.5
- **Template Engine**: Thymeleaf
- **Database Migration**: Liquibase
- **Build Tool**: Maven

## Prerequisites

- Java 21 or higher
- Maven 3.9+
- Docker and Docker Compose

## Quick Start

### 1. Start Infrastructure Services

Run PostgreSQL, MinIO, and RabbitMQ using Docker Compose:

```bash
docker-compose up -d
```

This will start:
- PostgreSQL on port `5432`
- MinIO on port `9000` (API) and `9001` (Console)
- RabbitMQ on port `5672` (AMQP) and `15672` (Management UI)

### 2. Build and Run the Application

```bash
mvn clean package
mvn spring-boot:run
```

The application will start on `http://localhost:8080`.

## Project Structure

```
src/main/java/com/employeeresumemaneger/
├── EmployeeResumeManegerApplication.java   # Application entry point
├── model/
│   └── Candidate.java                       # Candidate entity
├── repository/
│   └── CandidateRepository.java             # Data access layer
└── service/
    └── PdfService.java                      # PDF text extraction service

src/main/resources/
├── application.properties                   # Application configuration
├── db/changelog/
│   ├── changelog-master.xml                 # Liquibase changelog master
│   └── changes/
│       ├── 01-create-employee-table-.xml    # Employees table
│       ├── 02-create-resumes-table.xml      # Resumes table
│       └── 03-create-hr-users-table.xml     # HR Users table
└── templates/
    └── resume/
        └── createResume.html                # Resume upload form
```

## Database Schema

### Employees Table

| Column | Type | Description |
|--------|------|-------------|
| id | BIGSERIAL | Primary key |
| first_name | VARCHAR(100) | Employee first name |
| last_name | VARCHAR(100) | Employee last name |
| email | VARCHAR(255) | Unique email address |
| department | VARCHAR(100) | Department name |
| position | VARCHAR(100) | Job position |
| hire_date | DATE | Date hired |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Update timestamp |

### Resumes Table

| Column | Type | Description |
|--------|------|-------------|
| id | BIGSERIAL | Primary key |
| employee_id | BIGINT | Foreign key to employees |
| original_file_name | VARCHAR(255) | Original filename |
| storage_key | VARCHAR(500) | MinIO storage key |
| content_type | VARCHAR(100) | MIME type |
| file_size_bytes | BIGINT | File size in bytes |
| status | VARCHAR(20) | Processing status |
| parsed_text | TEXT | Extracted PDF text |
| uploaded_at | TIMESTAMP | Upload timestamp |
| processed_at | TIMESTAMP | Processing timestamp |

### HR Users Table

| Column | Type | Description |
|--------|------|-------------|
| id | BIGSERIAL | Primary key |
| username | VARCHAR(50) | Unique username |
| email | VARCHAR(255) | Unique email |
| password_hash | VARCHAR(255) | Hashed password |
| role | VARCHAR(20) | User role |
| enabled | BOOLEAN | Account status |
| created_at | TIMESTAMP | Creation timestamp |

## API Endpoints

### Employee Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/employees` | List all employees |
| GET | `/employees/new` | New employee form |
| POST | `/employees` | Create new employee |
| GET | `/employees/{id}` | Get employee details |
| GET | `/employees/{id}/edit` | Edit employee form |
| POST | `/employees/{id}` | Update employee |
| POST | `/employees/{id}/delete` | Delete employee |

### Resume Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/resumes` | List all resumes |
| GET | `/employees/{employeeId}/resumes` | Get employee resumes |
| POST | `/employees/{employeeId}/resumes/upload` | Upload resume |
| GET | `/resumes/{resumeId}/download` | Download resume |
| POST | `/resumes/{resumeId}/delete` | Delete resume |

### Candidate Application

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/upload` | Candidate registration form |
| POST | `/upload` | Submit candidate application |

## Configuration

Key configuration properties in `application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/resume_manager

# MinIO
minio.url=http://localhost:9000
minio.bucket-name=resumes

# RabbitMQ
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672

# File Upload
spring.servlet.multipart.max-file-size=10MB
```

## Features

- Employee CRUD operations with web interface
- Resume/CV upload with PDF text extraction
- File storage using MinIO object storage
- Asynchronous processing with RabbitMQ
- Database schema management with Liquibase
- Web UI with Thymeleaf templates

## Testing

Run tests with Maven:

```bash
mvn test
```
