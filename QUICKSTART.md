# Quick Start Guide - MediScribe AI

## 🚀 30-Second Setup

### 1. Start the Application
```bash
cd /home/wtc/Noctor-System
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

Open browser: **http://localhost:8080**

### 2. Seed Test Data
Click the **"🌱 Seed Test Data"** button to create:
- 2 Doctors (Emily Stone, James Wilson)
- 1 Nurse (Sarah Johnson)
- 2 Patients (John Doe, Jane Smith)

### 3. Try Doctor Flow
1. Select **Dr. Emily Stone** and click login
2. Click **"Accept"** on a waiting patient
3. Click **"View"** on the appointment
4. Paste this consultation note:
   ```
   Patient presents with persistent cough for 2 weeks, 
   fever 38.5°C, no dyspnea. Chest clear on examination, 
   O2 saturation 98% on room air. No signs of pneumonia.
   ```
5. Click **"Generate SOAP Note & AI Summary"**
6. Review AI-generated note and patient summary
7. Click **"Sign & Finalize Record"**

### 4. Try Patient Flow
1. Go back to landing page (Click "Switch Role")
2. Select **John Doe** (patient)
3. Click **"View My Health Summary"**
4. See doctor's note in simple language + prescription

### 5. Try Nurse Flow
1. Go back to landing page
2. Select **Nurse Sarah Johnson**
3. Register new patient: Name = "Alice Cooper", Priority = HIGH
4. Select "Dr. James Wilson" and click **"Assign"**
5. Doctor can now see patient in their queue

## 🤖 Optional: Use Real AI (Ollama)

To use actual Llama 3.2 AI:

### Install & Run Ollama
```bash
# Download from https://ollama.ai
# Or on Linux:
curl https://ollama.ai/install.sh | sh

# Pull Llama 2 model (first time only)
ollama pull llama2

# Start Ollama server (keep running)
ollama serve
```

Application auto-connects to http://localhost:11434

### Without Ollama
Application will return error responses, but UI still works for testing workflows.

## 📋 File Locations

- **JAR**: `/home/wtc/Noctor-System/target/demo-0.0.1-SNAPSHOT.jar`
- **Source**: `/home/wtc/Noctor-System/src/main/java/com/wethinkcode/demo/`
- **Templates**: `/home/wtc/Noctor-System/src/main/resources/templates/`
- **Config**: `/home/wtc/Noctor-System/src/main/resources/application.yaml`

## 🎯 What Each Role Can Do

### 👨‍⚕️ Doctor
- View waiting patients (sorted by priority)
- Accept appointments
- Record consultation notes
- AI generates SOAP note + patient summary + prescriptions
- Sign and finalize medical records
- View completed records

### 👩‍⚕️ Nurse
- Register new patients
- Set triage priority
- Assign available doctors
- View waiting queue

### 🧑‍💼 Patient
- View appointment history
- Read easy-to-understand health summaries
- View prescriptions
- Print medical records

## ⚡ Key Features Demonstrated

✅ **SOAP Note Generation** - Doctor enters notes → AI creates structured format
✅ **Patient Summaries** - Medical jargon converted to 5th-grade English
✅ **Prescription Extraction** - Auto-extracted from medical notes
✅ **Triage Queue** - Patients auto-sorted by priority (HIGH/MEDIUM/LOW)
✅ **Mobile UI** - Bootstrap 5 responsive design works on all devices
✅ **Service Layer** - Clean architecture with repositories and services
✅ **H2 Database** - Instant in-memory database, no setup needed

## 🔍 Explore the Code

**Key Classes**:
- `LlamaAiService.java` - AI integration via Ollama REST API
- `TriageService.java` - Patient priority sorting logic
- `DoctorService.java` - Medical record generation and signing
- `MainController.java` - Role switcher and landing page
- `DoctorController.java` - Doctor dashboard and workflows

**Templates**:
- `index.html` - Landing page with role selector
- `doctor-dashboard.html` - Doctor's main interface
- `appointment-detail.html` - Consultation recording
- `nurse-dashboard.html` - Triage queue management
- `patient-view.html` - Patient portal
- `patient-record.html` - Detailed medical summary

## 🛑 Stop the Application

Press `Ctrl+C` in the terminal running the app.

## 📊 Database

- **Type**: H2 In-Memory
- **Auto-reset**: On application restart
- **Admin Console**: http://localhost:8080/h2-console
  - URL: `jdbc:h2:mem:testdb`
  - User: `sa`
  - Password: (empty)

## 🎓 Learning Path

1. Start with **index.html** to understand role-based navigation
2. Explore **DoctorController** for CRUD patterns
3. Check **LlamaAiService** for external API integration
4. Review **bootstrap templates** for responsive design
5. Study **Service layer** for business logic

## 💡 Pro Tips

- Use "Seed Test Data" button to reset users anytime
- Doctors stay "AVAILABLE" after signing records - they can accept more patients
- Completed appointments show in a separate "Completed Records" section
- Patient summaries are formatted with alerts for readability
- Floating button (🎤) on doctor dashboard for quick notes

## ❓ FAQ

**Q: Do I need Ollama to test?**
A: No! UI works without it. Without Ollama, AI responses will be error messages, but all workflows function.

**Q: How do I change the port?**
A: Edit application.yaml and add: `server.port: 8081`

**Q: Can I modify the SOAP note template?**
A: Yes! Edit the prompt in `LlamaAiService.generateSoapNote()`

**Q: Is this production-ready?**
A: No! For production, add authentication, HTTPS, persistent database, error handling, and comprehensive testing.

---

**Now you're ready!** 🚀 Open http://localhost:8080 and start using MediScribe AI!
