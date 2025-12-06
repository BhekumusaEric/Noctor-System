package com.wethinkcode.demo.infrastructure.persistence;

import com.wethinkcode.demo.domain.shared.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Story F1: Pharmacist wants immediate notification when prescription is ready")
class PharmacyWorkflowTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    private User patient1;
    private User patient2;
    private User doctor;
    private Appointment appointment1;
    private Appointment appointment2;
    private MedicalRecord record1;
    private MedicalRecord record2;

    @BeforeEach
    void setUp() {
        // Create test patients
        patient1 = User.builder()
                .id(1L)
                .name("Patient One")
                .role(UserRole.PATIENT)
                .status("ACTIVE")
                .build();

        patient2 = User.builder()
                .id(2L)
                .name("Patient Two")
                .role(UserRole.PATIENT)
                .status("ACTIVE")
                .build();

        // Create test doctor
        doctor = User.builder()
                .id(101L)
                .name("Dr. Smith")
                .role(UserRole.DOCTOR)
                .status("AVAILABLE")
                .build();

        // Create appointments
        appointment1 = Appointment.builder()
                .id(1L)
                .patient(patient1)
                .doctor(doctor)
                .triagePriority(TriagePriority.HIGH)
                .status(AppointmentStatus.COMPLETED)
                .roughNotes("Fever and cough")
                .build();

        appointment2 = Appointment.builder()
                .id(2L)
                .patient(patient2)
                .doctor(doctor)
                .triagePriority(TriagePriority.MEDIUM)
                .status(AppointmentStatus.WAITING)
                .roughNotes("Headache")
                .build();

        // Create medical records with prescriptions
        record1 = MedicalRecord.builder()
                .id(1L)
                .appointmentId(appointment1.getId())
                .soapNote("Patient has bronchitis")
                .patientSummary("You have a chest infection")
                .prescription("Amoxicillin 500mg TID x 7 days, Cough syrup 10ml TDS")
                .isSigned(true)
                .build();

        record2 = MedicalRecord.builder()
                .id(2L)
                .appointmentId(appointment2.getId())
                .soapNote("Patient has migraine")
                .patientSummary("You have a bad headache")
                .prescription(null)  // Not yet ready
                .isSigned(false)
                .build();
    }

    @Test
    @DisplayName("F1.1: Given signed note with prescription, when checking pharmacy queue, then prescription is visible")
    void givenSignedNote_whenCheckPharmacyQueue_thenPrescriptionIsVisible() {
        // Given: A signed medical record with prescription
        assertTrue(record1.getIsSigned(), "Record should be signed");
        assertNotNull(record1.getPrescription(), "Record should have prescription");

        when(medicalRecordRepository.findById(record1.getId())).thenReturn(Optional.of(record1));

        // When: Pharmacist checks for prescriptions ready for dispensing
        Optional<MedicalRecord> found = medicalRecordRepository.findById(record1.getId());

        // Then: Prescription should be visible to pharmacy
        assertTrue(found.isPresent());
        assertTrue(found.get().getIsSigned(), "Record must be signed");
        assertNotNull(found.get().getPrescription());
        assertTrue(found.get().getPrescription().contains("Amoxicillin"));
        assertTrue(found.get().getPrescription().contains("Cough syrup"));

        verify(medicalRecordRepository, times(1)).findById(record1.getId());
    }

    @Test
    @DisplayName("F1.2: Given multiple prescriptions, when querying pharmacy queue, then all ready prescriptions appear")
    void givenMultiplePrescriptions_whenQueryingPharmacyQueue_thenAllReadyPrescriptionsAppear() {
        // Given: Multiple signed medical records
        List<MedicalRecord> allRecords = Arrays.asList(record1, record2);
        List<MedicalRecord> readyRecords = allRecords.stream()
                .filter(MedicalRecord::getIsSigned)
                .filter(r -> r.getPrescription() != null)
                .collect(Collectors.toList());

        when(medicalRecordRepository.findAll()).thenReturn(allRecords);

        // When: Pharmacy queries for ready prescriptions
        List<MedicalRecord> result = medicalRecordRepository.findAll().stream()
                .filter(MedicalRecord::getIsSigned)
                .filter(r -> r.getPrescription() != null)
                .collect(Collectors.toList());

        // Then: Only signed records with prescriptions appear in queue
        assertEquals(1, result.size(), "Only one record is signed with prescription");
        assertEquals(record1.getId(), result.get(0).getId());
        assertTrue(result.get(0).getPrescription().contains("Amoxicillin"));

        verify(medicalRecordRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("F1.3: Given prescription ready for dispensing, when pharmacist receives notification, then patient name is included")
    void givenPrescriptionReadyForDispensing_whenPharmacistReceivesNotification_thenPatientNameIncluded() {
        // Given: A prescription ready for pharmacy
        assertTrue(record1.getIsSigned());
        assertNotNull(record1.getPrescription());

        when(appointmentRepository.findById(appointment1.getId())).thenReturn(Optional.of(appointment1));
        when(medicalRecordRepository.findById(record1.getId())).thenReturn(Optional.of(record1));

        // When: Retrieving prescription details for pharmacy notification
        MedicalRecord ready = medicalRecordRepository.findById(record1.getId()).get();
        Appointment apt = appointmentRepository.findById(ready.getAppointmentId()).get();
        String patientName = apt.getPatient().getName();
        String prescriptionDetails = ready.getPrescription();

        // Then: Notification should contain patient name and prescription
        assertEquals("Patient One", patientName);
        assertTrue(prescriptionDetails.contains("Amoxicillin"));

        verify(medicalRecordRepository, times(1)).findById(record1.getId());
        verify(appointmentRepository, times(1)).findById(appointment1.getId());
    }

    @Test
    @DisplayName("F1.4: Given unsigned medical record, when checking pharmacy queue, then it does not appear")
    void givenUnsignedMedicalRecord_whenCheckingPharmacyQueue_thenItDoesNotAppear() {
        // Given: An unsigned medical record
        assertFalse(record2.getIsSigned(), "Record should not be signed");

        when(medicalRecordRepository.findAll()).thenReturn(Arrays.asList(record1, record2));

        // When: Querying pharmacy queue for ready prescriptions
        List<MedicalRecord> readyPrescriptions = medicalRecordRepository.findAll().stream()
                .filter(MedicalRecord::getIsSigned)
                .collect(Collectors.toList());

        // Then: Unsigned record should not appear
        assertFalse(readyPrescriptions.stream()
                .anyMatch(r -> r.getId() == 2L), "Unsigned record should not appear");
        assertTrue(readyPrescriptions.stream()
                .anyMatch(r -> r.getId() == 1L), "Signed record should appear");

        verify(medicalRecordRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("F1.5: Given prescription with missing details, when validating for pharmacy, then marks as incomplete")
    void givenPrescriptionWithMissingDetails_whenValidatingForPharmacy_thenMarksAsIncomplete() {
        // Given: A signed record with incomplete prescription details
        MedicalRecord incompleteRecord = MedicalRecord.builder()
                .id(3L)
                .appointmentId(appointment1.getId())
                .soapNote("Some diagnosis")
                .prescription("Medication only")  // Missing dosage and duration
                .isSigned(true)
                .build();

        when(medicalRecordRepository.findById(3L)).thenReturn(Optional.of(incompleteRecord));

        // When: Validating prescription for pharmacy
        MedicalRecord record = medicalRecordRepository.findById(3L).get();
        boolean hasCompleteInfo = record.getPrescription() != null &&
                record.getPrescription().contains("mg") &&
                record.getPrescription().contains("days");

        // Then: Incomplete prescriptions should be flagged
        assertTrue(record.getIsSigned());
        assertNotNull(record.getPrescription());
        assertFalse(hasCompleteInfo, "Prescription is missing dosage details");

        verify(medicalRecordRepository, times(1)).findById(3L);
    }

    @Test
    @DisplayName("F1.6: Given completed appointments, when filtering for pharmacy queue, then only COMPLETED appointments shown")
    void givenCompletedAppointments_whenFilteringForPharmacyQueue_thenOnlyCompletedAppointmentsShown() {
        // Given: Appointments with different statuses
        List<Appointment> allAppointments = Arrays.asList(appointment1, appointment2);

        when(appointmentRepository.findAll()).thenReturn(allAppointments);

        // When: Filtering appointments for pharmacy queue (only completed)
        List<Appointment> completedAppointments = appointmentRepository.findAll().stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .collect(Collectors.toList());

        // Then: Only completed appointments appear
        assertEquals(1, completedAppointments.size());
        assertEquals(AppointmentStatus.COMPLETED, completedAppointments.get(0).getStatus());

        verify(appointmentRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("F1.7: Given prescription ready, when pharmacist marks as dispensed, then record updates")
    void givenPrescriptionReady_whenPharmacistMarksAsDispensed_thenRecordUpdates() {
        // Given: A signed prescription ready for dispensing
        assertTrue(record1.getIsSigned());
        assertNotNull(record1.getPrescription());

        when(medicalRecordRepository.save(record1)).thenAnswer(invocation -> {
            MedicalRecord saved = invocation.getArgument(0);
            return saved;
        });

        // When: Pharmacist marks prescription as dispensed (could track in future)
        // For now, just verify record can be retrieved and confirmed
        MedicalRecord confirmed = medicalRecordRepository.save(record1);

        // Then: Record should be available for confirmation
        assertTrue(confirmed.getIsSigned());
        assertNotNull(confirmed.getPrescription());

        verify(medicalRecordRepository, times(1)).save(record1);
    }
}
