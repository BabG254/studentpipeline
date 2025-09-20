# Student Data Pipeline - Backend

A Spring Boot 3.4.5 application for processing large datasets of student data with Excel generation, CSV conversion, and database storage capabilities.

## Features

- **Excel Generation**: Generate Excel files with up to 10M student records using streaming (SXSSF)
- **Excel to CSV Conversion**: Convert Excel files to CSV with score adjustments (+10)
- **CSV to Database**: Batch upload CSV data to PostgreSQL with transaction support
- **Student Reports**: Paginated API with filtering and export capabilities (Excel/CSV/PDF)
- **Performance Optimized**: Memory-efficient streaming for large datasets

## Tech Stack

- Java 17
- Spring Boot 3.4.5
- PostgreSQL
- Apache POI (SXSSF for streaming Excel)
- OpenCSV
- iText7 (PDF generation)
- Maven

## Prerequisites

- Java 17 or higher
- PostgreSQL 15+
- Maven 3.6+

## Installation & Setup

### 1. Clone the repository
```bash
git clone <repository-url>
cd backend
```

### 2. Database Setup
```bash
# Start PostgreSQL with Docker
docker-compose up -d

# Or install PostgreSQL locally and create database
createdb studentpipeline
```

### 3. Configuration
Set environment variables or modify `application.yml`:

```bash
# Database Configuration
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/studentpipeline
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres

# Data Path Configuration (Windows)
export DATAPATH_BASE=C:/var/log/applications/API/dataprocessing

# Data Path Configuration (Linux/macOS)
export DATAPATH_BASE=/var/log/applications/API/dataprocessing
```

### 4. Create Data Directory
Windows:
```cmd
# Run as Administrator
scripts\setup-data-dir.bat
```

Linux/macOS:
```bash
chmod +x scripts/setup-data-dir.sh
./scripts/setup-data-dir.sh
```

### 5. Build and Run
```bash
# Build the application
mvn clean compile

# Run the application
mvn spring-boot:run

# Or build JAR and run
mvn clean package
java -jar target/student-data-pipeline-1.0.0.jar
```

The application will start on http://localhost:8080

## API Endpoints

### Data Generation
```bash
# Generate Excel file with N student records
POST /api/generate-excel
Content-Type: application/json

{
  "records": 1000000,
  "fileName": "students-1m.xlsx"
}
```

### Data Processing
```bash
# Convert Excel to CSV (adds +10 to scores)
POST /api/convert-excel-to-csv
Content-Type: multipart/form-data

file=@C:/var/log/applications/API/dataprocessing/students-1000000.xlsx
```

### Data Upload
```bash
# Upload CSV to database (final DB score = original Excel score + 5)
POST /api/upload-csv-to-db
Content-Type: multipart/form-data

file=@C:/var/log/applications/API/dataprocessing/students-1000000-processed.csv
```

### Student Reports
```bash
# Get paginated students with filters
GET /api/students?page=0&size=20&studentId=123&className=Class1

# Export students (Excel/CSV/PDF)
GET /api/students/export?format=excel&className=Class1&fileName=class1-report.xlsx

# Get student statistics
GET /api/students/stats
```

## Example API Usage with curl

### Generate Excel
```bash
curl -X POST http://localhost:8080/api/generate-excel \
  -H "Content-Type: application/json" \
  -d '{"records":1000000,"fileName":"students-1000000.xlsx"}'
```

### Convert Excel to CSV
```bash
curl -X POST http://localhost:8080/api/convert-excel-to-csv \
  -F file=@C:/var/log/applications/API/dataprocessing/students-1000000.xlsx
```

### Upload CSV to Database
```bash
curl -X POST http://localhost:8080/api/upload-csv-to-db \
  -F file=@C:/var/log/applications/API/dataprocessing/students-1000000-processed.csv
```

### Fetch Students
```bash
curl "http://localhost:8080/api/students?page=0&size=20&className=Class1"
```

## Performance Guidelines

| Records | Generation Time | Memory Usage | File Size |
|---------|----------------|--------------|-----------|
| 1K      | ~1-2 seconds   | ~50MB       | ~100KB    |
| 10K     | ~5-10 seconds  | ~100MB      | ~1MB      |
| 100K    | ~30-60 seconds | ~200MB      | ~10MB     |
| 1M      | ~5-10 minutes  | ~500MB      | ~100MB    |

## Architecture

### Key Components

- **ExcelGenerationService**: Streaming Excel generation using SXSSF
- **ExcelToCsvService**: Memory-efficient Excel to CSV conversion
- **CsvToDatabaseService**: Batch database operations with transaction management
- **StudentReportService**: Paginated queries with filtering
- **ExportService**: Multi-format export (Excel/CSV/PDF)

### Database Schema

```sql
CREATE TABLE student (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    dob DATE NOT NULL,
    class_name VARCHAR(20) NOT NULL,
    score INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_student_student_id ON student(student_id);
CREATE INDEX idx_student_class_name ON student(class_name);
CREATE INDEX idx_student_score ON student(score);
```

### Score Calculation Logic

1. **Excel Generation**: Original scores 55-75
2. **CSV Conversion**: Excel score + 10 → 65-85
3. **Database Storage**: Original Excel score + 5 → 60-80

The system automatically detects CSV score format and adjusts accordingly.

## Configuration Properties

```yaml
# Application Configuration
server:
  port: 8080

# Database Configuration
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/studentpipeline
    username: postgres
    password: postgres
    
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        jdbc:
          batch_size: 5000

# File Configuration
datapath:
  base: ${DATAPATH_BASE:C:/var/log/applications/API/dataprocessing}

# File Upload Limits
spring:
  servlet:
    multipart:
      max-file-size: 500MB
      max-request-size: 500MB
```

## Testing

```bash
# Run unit tests
mvn test

# Run integration tests
mvn test -Dtest=**/*IntegrationTest

# Run specific test
mvn test -Dtest=ExcelGenerationServiceTest
```

## Troubleshooting

### Common Issues

1. **OutOfMemoryError**: Increase JVM heap size
   ```bash
   export JAVA_OPTS="-Xmx2g -Xms1g"
   mvn spring-boot:run
   ```

2. **File Permission Errors**: Ensure data directory exists and is writable
   ```bash
   mkdir -p /var/log/applications/API/dataprocessing
   chmod 755 /var/log/applications/API/dataprocessing
   ```

3. **Database Connection**: Verify PostgreSQL is running and accessible
   ```bash
   psql -h localhost -U postgres -d studentpipeline
   ```

4. **Large File Processing**: Monitor logs for progress
   ```bash
   tail -f logs/application.log
   ```

## Monitoring

The application includes several monitoring endpoints:

- Health: `GET /actuator/health`
- Application Info: `GET /actuator/info`

## Production Deployment

### Environment Variables
```bash
export SPRING_PROFILES_ACTIVE=production
export SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db:5432/studentpipeline
export SPRING_DATASOURCE_USERNAME=prod_user
export SPRING_DATASOURCE_PASSWORD=secure_password
export DATAPATH_BASE=/opt/studentpipeline/data
```

### JVM Tuning
```bash
export JAVA_OPTS="-Xmx4g -Xms2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

### Build for Production
```bash
mvn clean package -Pprod
java $JAVA_OPTS -jar target/student-data-pipeline-1.0.0.jar
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License.