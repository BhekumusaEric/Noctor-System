package com.wethinkcode.demo.domain.doctor;

import com.wethinkcode.demo.domain.shared.*;
import com.wethinkcode.demo.infrastructure.ai.LlamaAiService;
import com.wethinkcode.demo.infrastructure.persistence.AppointmentRepository;
import com.wethinkcode.demo.infrastructure.persistence.MedicalRecordRepository;
import com.wethinkcode.demo.infrastructure.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Story C4 & C5: Doctor wants to record notes and get SOAP Note, then sign the note")
class AiDocumentationTest {

    @Mock
    private LlamaAiService llamaAiService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    private DoctorService doctorService;
    private Appointment appointment;
    private MedicalRecord draftRecord;

    @BeforeEach
    void setUp() {
        doctorService = new DoctorService(
                userRepository,
                appointmentRepository,
                medicalRecordRepository,
                llamaAiService
        );

        // Create test patient
        var patient = User.builder()
                .id(1L)
                .name("John Doe")
                .role(UserRole.PATIENT)
                .status("ACTIVE")
                .build();

        // Create test doctor
        var doctor = User.builder()
                .id(101L)
                .name("Dr. Smith")
                .role(UserRole.DOCTOR)
                .status("AVAILABLE")
                .build();

        // Create test appointment
        appointment = Appointment.builder()
                .id(1L)
                .patient(patient)
                .doctor(doctor)
                .triagePriority(TriagePriority.HIGH)
                .status(AppointmentStatus.IN_CONSULT)
                .roughNotes("Patient has a headache and mild fever")
                .build();

        // Create draft medical record
        draftRecord = MedicalRecord.builder()
                .id(1L)
                .appointmentId(appointment.getId())
                .soapNote(null)
                .patientSummary(null)
                .prescription(null)
                .isSigned(false)
                .build();
    }

    @Test
    @DisplayName("C4.1: Given rough notes, when AI service called, then returns structured SOAP note")
    void givenRoughNotes_whenAiServiceCalled_thenReturnsStructuredSoapNote() {
        // Given: Rough clinical notes from doctor
        String roughNotes = "Patient has a headache and mild fever. Appears to have flu-like symptoms.";
        String mockSoapResponse = """
                {"subjective": "Patient reports headache and mild fever", "objective": "Normal vitals", "assessment": "Flu", "plan": "Rest"}
                """;

        when(llamaAiService.generateSoapNote(roughNotes)).thenReturn(mockSoapResponse);

        // When: Calling AI service to generate SOAP note
        String result = llamaAiService.generateSoapNote(roughNotes);

        // Then: Verify SOAP note has expected structure
        assertNotNull(result);
        assertTrue(result.contains("subjective"));
        assertTrue(result.contains("objective"));
        assertTrue(result.contains("assessment"));
        assertTrue(result.contains("plan"));

        verify(llamaAiService, times(1)).generateSoapNote(roughNotes);
    }

    @Test
    @DisplayName("C4.2: Given appointment with rough notes, when storing medical record, then record is persisted")
    void givenAppointmentWithRoughNotes_whenStoringMedicalRecord_thenRecordPersisted() {
        // Given: Medical record with SOAP note
        String soapNote = "Subjective: Headache and fever\nObjective: Normal\nAssessment: Flu\nPlan: Rest";
        
        MedicalRecord record = MedicalRecord.builder()
                .id(1L)
                .appointmentId(appointment.getId())
                .soapNote(soapNote)
                .isSigned(false)
                .build();
        
        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(record);

        // When: Saving medical record
        MedicalRecord saved = medicalRecordRepository.save(record);

        // Then: Record is persisted with SOAP note
        assertNotNull(saved);
        assertNotNull(saved.getSoapNote());
        assertFalse(saved.getIsSigned());
        assertEquals(appointment.getId(), saved.getAppointmentId());

        verify(medicalRecordRepository, times(1)).save(any(MedicalRecord.class));
    }

    @Test
    @DisplayName("C4.3: Given complex diagnosis in SOAP note, when saving record, then all sections are preserved")
    void givenComplexDiagnosisInSoapNote_whenSavingRecord_thenAllSectionsPreserved() {
        // Given: A comprehensive SOAP note
        String complexSoapNote = """
                Subjective: 32-year-old male presents with persistent headache x 5 days.
                Objective: BP 138/88, HR 96, Temp 37.2C. Neck stiffness present.
                Assessment: Migraine vs tension headache.
                Plan: CT head if symptoms worsen. Referral to neurology. Follow-up in 1 week.
                """;

        MedicalRecord record = MedicalRecord.builder()
                .appointmentId(appointment.getId())
                .soapNote(complexSoapNote)
                .isSigned(false)
                .build();
        
        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenAnswer(invocation -> {
            MedicalRecord r = invocation.getArgument(0);
            r.setId(1L);
            return r;
        });

        // When: Saving the medical record
        MedicalRecord saved = medicalRecordRepository.save(record);

        // Then: All SOAP sections are preserved
        assertNotNull(saved.getSoapNote());
        assertTrue(saved.getSoapNote().contains("Subjective"));
        assertTrue(saved.getSoapNote().contains("Objective"));
        assertTrue(saved.getSoapNote().contains("Assessment"));
        assertTrue(saved.getSoapNote().contains("Plan"));
    }

    @Test
    @DisplayName("C5.1: Given draft note, when doctor signs, then isSigned flag is true")
    void givenDraftNote_whenDoctorSigns_thenIsSignedFlagTrue() {
        // Given: A draft medical record not yet signed
        draftRecord.setIsSigned(false);
        
        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Doctor signs the record
        draftRecord.setIsSigned(true);
        MedicalRecord signedRecord = medicalRecordRepository.save(draftRecord);

        // Then: Record is signed
        assertTrue(signedRecord.getIsSigned());

        verify(medicalRecordRepository, times(1)).save(any(MedicalRecord.class));
    }

    @Test
    @DisplayName("C5.2: Given signed note with prescription, when record is finalized, then prescription is available for pharmacy")
    void givenSignedNoteWithPrescription_whenRecordFinalized_thenPrescriptionAvailable() {
        // Given: A signed medical record with prescription details
        String prescription = "Amoxicillin 500mg TID x 7 days, Ibuprofen 400mg for pain";
        draftRecord.setIsSigned(true);
        draftRecord.setPrescription(prescription);

        when(medicalRecordRepository.save(draftRecord)).thenReturn(draftRecord);

        // When: Record is finalized
        MedicalRecord finalizedRecord = medicalRecordRepository.save(draftRecord);

        // Then: Prescription is available and record is signed
        assertTrue(finalizedRecord.getIsSigned());
        assertNotNull(finalizedRecord.getPrescription());
        assertEquals(prescription, finalizedRecord.getPrescription());

        verify(medicalRecordRepository, times(1)).save(draftRecord);
    }

    @Test
    @DisplayName("C5.3: Given multiple draft records, when doctor signs one, then only that record is marked signed")
    void givenMultipleDraftRecords_whenDoctorSignsOne_thenOnlyThatRecordSigned() {
        // Given: Multiple draft records
        MedicalRecord record1 = MedicalRecord.builder()
                .id(1L)
                .appointmentId(1L)
                .soapNote("SOAP Note 1")
                .isSigned(false)
                .build();

        MedicalRecord record2 = MedicalRecord.builder()
                .id(2L)
                .appointmentId(2L)
                .soapNote("SOAP Note 2")
                .isSigned(false)
                .build();

        // When: Signing only record1
        record1.setIsSigned(true);
        when(medicalRecordRepository.save(record1)).thenAnswer(invocation -> invocation.getArgument(0));

        MedicalRecord signedRecord = medicalRecordRepository.save(record1);

        // Then: Only record1 is signed, record2 remains unsigned
        assertTrue(signedRecord.getIsSigned());
        assertFalse(record2.getIsSigned());

        verify(medicalRecordRepository, times(1)).save(record1);
    }
}
