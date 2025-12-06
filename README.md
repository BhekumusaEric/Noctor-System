# MediScribe AI - Clinical Operations System

A cutting-edge clinical operations system powered by Llama 3.2 AI, designed to eliminate documentation burden and reduce clinician burnout through intelligent automation.

## 🏥 Features

### Core Capabilities
- **SOAP Note Generation**: Automatically converts raw consultation notes into structured SOAP notes using Llama 3.2
- **Patient-Friendly Summaries**: Generates 5th-grade level health summaries for patient comprehension
- **Prescription Extraction**: Intelligently extracts and formats prescriptions from medical records
- **Triage Queue Management**: Automatically prioritizes waiting patients (HIGH/MEDIUM/LOW)
- **Mobile-First UI**: Fully responsive Bootstrap 5 interface optimized for all devices

### User Roles
1. **Doctor**: Record consultations, review/sign medical records, manage patient list
2. **Nurse**: Register new patients, assign doctors, manage triage queue
3. **Patient**: View medical summaries, access health records in simple language

## 🛠 Tech Stack

- **Backend**: Java 21, Spring Boot 3.5.9
- **Database**: H2 In-Memory (rapid development)
- **Frontend**: Thymeleaf + Bootstrap 5 + HTMX
- **AI Integration**: Llama 3.2 via Ollama (REST API)
- **Build**: Maven 3.6.3+
- **Code Quality**: Lombok, Clean Architecture pattern

## 📋 Project Structure

```
src/
├── main/java/com/wethinkcode/demo/
│   ├── model/
│   │   ├── User, Appointment, MedicalRecord (JPA Entities)
│   │   ├── UserRole, AppointmentStatus, TriagePriority (Enums)
│   ├── repository/
│   │   ├── UserRepository
│   │   ├── AppointmentRepository
│   │   └── MedicalRecordRepository
│   ├── service/
│   │   ├── LlamaAiService (AI/Ollama integration)
│   │   ├── TriageService (Patient prioritization)
│   │   └── DoctorService (Medical record management)
│   ├── controller/
│   │   ├── MainController (Landing page & role switcher)
│   │   ├── DoctorController
│   │   ├── NurseController
│   │   └── PatientController
│   └── NoctorSystemApplication.java
└── resources/
    ├── application.yaml (H2 & Ollama config)
    └── templates/ (Thymeleaf views)
```

## 🚀 Getting Started

### Prerequisites
- Java 21 or higher
- Maven 3.6.3+
- Ollama running with Llama 2 model (optional, for AI features)

### Installation

1. **Clone/Navigate to project**:
```bash
cd /home/wtc/Noctor-System
```

2. **Build the project**:
```bash
mvn clean package -DskipTests
```

3. **Run the application**:
```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

### Setting up Ollama (for AI features)

1. **Install Ollama** from https://ollama.ai
2. **Pull Llama 2 model**:
```bash
ollama pull llama2
```

3. **Run Ollama service**:
```bash
ollama serve
```

The application is configured to connect to `http://localhost:11434` by default. To change, update `application.yaml`:
```yaml
spring:
  ai:
    ollama:
      base-url: http://your-ollama-server:11434
```

## 💻 Usage Guide

### 1. Landing Page
- Navigate to `http://localhost:8080`
- Use the **Seed Test Data** button to populate sample users
- Select a role (Doctor/Nurse/Patient) and user to log in

### 2. Doctor Dashboard
- View waiting patients sorted by triage priority
- Accept appointments to assign to yourself
- Record consultation notes → AI generates SOAP notes
- Review, edit, and sign medical records
- Patients receive auto-generated summaries

### 3. Nurse Dashboard
- Register new patients with triage priority
- View waiting patient queue
- Assign available doctors to waiting patients
- Monitor triage metrics

### 4. Patient Portal
- View appointment history
- Access medical summaries (easy-to-read format)
- View prescriptions
- Print health records

## 🤖 AI Integration Details

### LlamaAiService
The service uses REST API calls to Ollama to:

**1. generateSoapNote(roughNotes)**
- Inputs: Raw consultation text
- Outputs: Structured JSON with Subjective/Objective/Assessment/Plan

**2. generatePatientSummary(soapNote)**
- Inputs: Clinical SOAP note
- Outputs: Patient-friendly summary in simple language

**3. extractPrescription(soapNote)**
- Inputs: Medical note
- Outputs: Formatted prescription list

## 📊 Database Schema

### User Entity
```
id (PK)
name (String)
role (UserRole enum: DOCTOR, NURSE, PATIENT, PHARMACIST)
status (String: AVAILABLE, BUSY, ON_LEAVE)
```

### Appointment Entity
```
id (PK)
patient_id (FK to User)
doctor_id (FK to User)
triage_priority (TriagePriority enum: HIGH, MEDIUM, LOW)
status (AppointmentStatus enum: WAITING, IN_CONSULT, COMPLETED)
rough_notes (TEXT)
```

### MedicalRecord Entity
```
id (PK)
appointment_id (FK)
soap_note (TEXT - AI generated)
patient_summary (TEXT - AI generated)
prescription (TEXT)
is_signed (Boolean)
```

## 🔧 Configuration

### application.yaml
```yaml
spring:
  application:
    name: Noctor System
  datasource:
    url: jdbc:h2:mem:testdb
    driverClassName: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
  ai:
    ollama:
      base-url: http://localhost:11434
```

##  Key Design Patterns

1. **Service-Repository Pattern**: Clean separation of concerns
2. **MVC Architecture**: Controllers → Services → Repositories → Entities
3. **Transactional Operations**: @Transactional ensures data consistency
4. **Mobile-First UI**: Bootstrap 5 responsive components
5. **Enum-based States**: Type-safe status management

## 📱 Mobile Responsiveness

All UI components include:
- Fluid grid layouts
- Touch-friendly button sizes (large touch targets)
- Mobile-optimized navigation
- Responsive typography
- Fixed action buttons for primary actions

##  Security Notes

For hackathon mode, this implementation uses:
- Simple user switcher (no OAuth/Login)
- H2 in-memory database (no production security)
- HTTP only (no HTTPS)

**For production**, implement:
- Spring Security with JWT/OAuth2
- Password encryption (BCrypt)
- HTTPS/SSL
- Database audit logging
- API rate limiting

##  Testing the System

1. **Seed test data**: Click "Seed Test Data" button
2. **Doctor flow**:
   - Select a doctor
   - Accept a waiting patient
   - Enter consultation notes
   - AI generates SOAP note
   - Sign the record

3. **Nurse flow**:
   - Register new patient with priority
   - Assign available doctor

4. **Patient flow**:
   - View appointments
   - Read easy-to-understand health summary
   - View prescriptions

## 📝 Sample Workflow

1. Nurse registers patient "John Doe" with HIGH priority
2. System shows patient in waiting queue
3. Doctor accepts the appointment
4. Doctor enters: "Patient complains of persistent cough for 2 weeks, fever, no breathing difficulty. Chest clear on exam, O2 sat 98%. Likely viral bronchitis."
5. AI generates structured SOAP note
6. AI creates patient summary: "You have inflammation in your breathing tubes, likely from a virus. Take rest, drink fluids, and monitor for signs of worsening."
7. AI extracts prescription: "Cough syrup 10ml thrice daily for 5 days"
8. Doctor signs record
9. Patient can view summary and prescription

##  Performance Optimizations

- H2 in-memory database for instant queries
- Lazy loading for User relationships
- Minimal template processing
- Static resource caching
- RESTful API design

##  API Endpoints

```
GET  / → Landing page with role selector
POST /switch-user → Redirect to dashboard
POST /api/seed-data → Generate test data

Doctor Endpoints:
GET  /doctor/dashboard → Dashboard with waiting patients
POST /doctor/accept-appointment/{id} → Accept appointment
GET  /doctor/appointment/{id} → View appointment details
POST /doctor/record-consultation/{id} → Generate medical record
POST /doctor/sign-record/{id} → Sign medical record

Nurse Endpoints:
GET  /nurse/dashboard → Nurse dashboard
POST /nurse/register-patient → Register new patient
POST /nurse/assign-doctor/{id} → Assign doctor to patient

Patient Endpoints:
GET  /patient/view → Patient appointments
GET  /patient/record/{id} → View medical record
```

##  Troubleshooting

**Cannot connect to Ollama**
- Ensure Ollama is running: `ollama serve`
- Check base-url in application.yaml
- Verify port 11434 is accessible

**H2 Database reset**
- Database is recreated on each application restart (create-drop mode)
- Use seed-data endpoint to repopulate

**Port 8080 already in use**
- Change in application.yaml: `server.port: 8081`

## 📄 License

Built for hackathon purposes. Use freely with attribution.

## 👥 Contributors

Developed as part of "Noctor System" hackathon project.

---

**Status**:  Ready for deployment and testing

**Last Updated**: December 2025
