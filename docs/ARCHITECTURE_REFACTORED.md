# Noctor System - Refactored Architecture

## Overview

The Noctor System has been reorganized using **Domain-Driven Design (DDD)** principles with a clear separation of concerns. This refactoring improves code organization, maintainability, and understanding of domain boundaries.

## Directory Structure

```
src/main/java/com/wethinkcode/demo/
├── domain/                          # Business logic organized by domain
│   ├── doctor/
│   │   └── DoctorService.java       # Doctor-specific operations & medical record generation
│   ├── nurse/                       # Ready for future NurseService implementation
│   ├── patient/                     # Ready for future PatientService implementation
│   └── shared/                      # Shared domain models & services
│       ├── User.java                # JPA entity: healthcare professionals & patients
│       ├── UserRole.java            # Enum: DOCTOR, NURSE, PATIENT, ADMIN
│       ├── Appointment.java         # JPA entity: patient-doctor consultations
│       ├── AppointmentStatus.java   # Enum: PENDING, IN_PROGRESS, COMPLETED, CANCELLED
│       ├── MedicalRecord.java       # JPA entity: AI-generated SOAP notes & summaries
│       ├── TriagePriority.java      # Enum: HIGH, MEDIUM, LOW
│       └── TriageService.java       # Patient queue management with priority sorting
│
├── infrastructure/                  # Technical concerns & external integrations
│   ├── ai/
│   │   └── LlamaAiService.java      # Ollama REST API client for SOAP generation
│   └── persistence/
│       ├── UserRepository.java      # Spring Data JPA for User entities
│       ├── AppointmentRepository.java# Spring Data JPA for Appointment entities
│       └── MedicalRecordRepository.java# Spring Data JPA for MedicalRecord entities
│
├── presentation/                    # HTTP request handling & view routing
│   ├── DoctorController.java        # Routes: /doctor/* - doctor dashboard & operations
│   ├── NurseController.java         # Routes: /nurse/* - nurse triage & status updates
│   ├── PatientController.java       # Routes: /patient/* - patient medical history
│   └── MainController.java          # Routes: / - main landing page & role selection
│
├── NoctorSystemApplication.java     # Spring Boot application entry point
└── resources/
    ├── application.yaml             # Configuration: H2 database, Ollama endpoint
    └── templates/                   # Thymeleaf HTML templates (6 files)
        ├── index.html               # Main landing page with role selector
        ├── doctor-dashboard.html    # Doctor view: waiting appointments
        ├── doctor-record.html       # Doctor view: create medical records
        ├── nurse-triage.html        # Nurse view: patient queue & prioritization
        ├── patient-history.html     # Patient view: medical history
        └── fragments.html           # Reusable Bootstrap 5 components
```

## Architectural Layers

### 1. **Domain Layer** (`domain/`)
Contains business logic and domain models:
- **Models**: JPA entities (User, Appointment, MedicalRecord) with database persistence
- **Enums**: Domain constants (UserRole, AppointmentStatus, TriagePriority)
- **Services**: Business logic (DoctorService for medical records, TriageService for queue management)
- **Subdomain Organization**: `doctor/`, `nurse/`, `patient/` for role-specific logic

**Responsibilities**:
- Implement business rules and workflows
- Manage domain object lifecycle
- Express domain knowledge in code

### 2. **Infrastructure Layer** (`infrastructure/`)
Technical concerns and external integrations:
- **AI (`ai/`)**: Ollama REST API integration for SOAP note generation
- **Persistence (`persistence/`)**: Spring Data JPA repositories for database access

**Responsibilities**:
- Abstract technical details from domain
- Handle external service calls
- Provide data access abstraction

### 3. **Presentation Layer** (`presentation/`)
HTTP request handling and view routing:
- 4 Spring MVC controllers for role-based endpoints
- Thymeleaf view rendering
- Request parameter validation

**Responsibilities**:
- Map HTTP requests to domain operations
- Render HTML responses
- Handle cross-cutting presentation concerns

## Key Design Patterns

### Separation of Concerns
- **Domain** focuses on what the system does
- **Infrastructure** focuses on how to do it technically
- **Presentation** focuses on exposing functionality via HTTP

### Dependency Inversion
```
Presentation Controllers
    ↓
Domain Services (business logic)
    ↓
Infrastructure Services & Repositories
    ↓
External Systems (Ollama, H2 Database)
```
Controllers depend on domain services, which depend on infrastructure components. Infrastructure doesn't depend on higher layers.

### Repository Pattern
Spring Data JPA repositories abstract database operations, allowing the domain to focus on business logic without database implementation details.

## Service Layer Flow

### Medical Record Generation (Doctor)
```
DoctorController (/doctor/record)
    ↓
DoctorService.generateAndSaveMedicalRecord()
    ↓
LlamaAiService.generateSoapNote() → Ollama REST API
    ↓
MedicalRecord entity → saved via MedicalRecordRepository
```

### Patient Triage (Nurse)
```
NurseController (/nurse/triage)
    ↓
TriageService.getWaitingAppointmentsByPriority()
    ↓
AppointmentRepository.findAll() → prioritized by enum
    ↓
Rendered to nurse-triage.html template
```

## Migration from Flat Structure

### Before (Flat Packages)
```
controller/  (4 controllers)
model/       (3 entities, 3 enums)
repository/  (3 repositories)
service/     (2 services + AI service)
```

### After (Domain-Driven)
```
domain/doctor/         (1 service)
domain/shared/         (3 entities, 3 enums, 1 service)
domain/nurse/          (placeholder)
domain/patient/        (placeholder)
infrastructure/ai/     (1 AI service)
infrastructure/persistence/ (3 repositories)
presentation/          (4 controllers)
```

## Benefits of This Architecture

1. **Clear Domain Boundaries**: Services and models grouped by domain (`doctor/`, `nurse/`, `patient/`)
2. **Dependency Management**: Infrastructure depends on domain, not vice versa
3. **Testability**: Services can be tested independently with mock repositories
4. **Scalability**: Easy to add new services (e.g., PatientService in `domain/patient/`)
5. **Maintainability**: New developers understand code organization quickly
6. **Reduced Coupling**: Controllers don't directly know about database implementation

## Technology Stack

- **Java 21** - Latest LTS JVM
- **Spring Boot 3.5.9** - Application framework with embedded Tomcat
- **Spring Data JPA 3.5.x** - Database abstraction via Hibernate
- **H2 2.3.x** - In-memory SQL database (zero configuration)
- **Thymeleaf 3.x** - Server-side HTML templating
- **Bootstrap 5** - Responsive CSS framework
- **Ollama API** - Local LLM inference for SOAP note generation
- **Maven 3.6+** - Build tool with 64MB JAR output

## Package Naming Convention

- `com.wethinkcode.demo.domain.{subdomain}` - Business logic
- `com.wethinkcode.demo.infrastructure.{concern}` - Technical implementations
- `com.wethinkcode.demo.presentation` - HTTP controllers

## Future Enhancements

1. **NurseService** - Move nurse-specific logic from controller to `domain/nurse/NurseService.java`
2. **PatientService** - Create patient-specific operations in `domain/patient/`
3. **Event-Driven Architecture** - Add domain events for appointment status changes
4. **API Layer** - Create `api/` layer for REST API endpoints alongside MVC controllers
5. **Validation Layer** - Add `domain/validation/` for business rule validation

## Building & Running

```bash
# Compile with new architecture
mvn clean compile

# Run tests
mvn test

# Package application
mvn clean package

# Run the application
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

Application starts on `http://localhost:8080` with:
- H2 Database Console: `http://localhost:8080/h2-console`
- Web Interface: `http://localhost:8080/`

---

**Refactored:** December 6, 2025 | **Status:** ✅ Compilation successful, 64MB JAR built
