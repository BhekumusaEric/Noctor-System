package com.wethinkcode.demo.domain.doctor;

import com.wethinkcode.demo.domain.shared.Appointment;
import com.wethinkcode.demo.domain.shared.MedicalRecord;
import com.wethinkcode.demo.domain.shared.User;
import com.wethinkcode.demo.domain.shared.AppointmentStatus;
import com.wethinkcode.demo.infrastructure.persistence.UserRepository;
import com.wethinkcode.demo.infrastructure.persistence.AppointmentRepository;
import com.wethinkcode.demo.infrastructure.persistence.MedicalRecordRepository;
import com.wethinkcode.demo.infrastructure.ai.LlamaAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DoctorService {
    
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final LlamaAiService llamaAiService;
    
    /**
     * Get all appointments for a specific doctor
     */
    public List<Appointment> getDoctorAppointments(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }
    
    /**
     * Update doctor status (AVAILABLE/BUSY/ON_LEAVE)
     */
    @Transactional
    public void updateDoctorStatus(Long doctorId, String status) {
        Optional<User> doctor = userRepository.findById(doctorId);
        if (doctor.isPresent()) {
            doctor.get().setStatus(status);
            userRepository.save(doctor.get());
        }
    }
    
    /**
     * Generate and save medical record with AI-generated SOAP note and patient summary
     */
    @Transactional
    public MedicalRecord generateAndSaveMedicalRecord(Long appointmentId, String roughNotes) {
        // Fetch the appointment
        Optional<Appointment> appointment = appointmentRepository.findById(appointmentId);
        if (appointment.isEmpty()) {
            throw new IllegalArgumentException("Appointment not found with ID: " + appointmentId);
        }
        
        Appointment apt = appointment.get();
        
        // Generate SOAP note using Llama AI
        String soapNote = llamaAiService.generateSoapNote(roughNotes);
        
        // Generate patient-friendly summary
        String patientSummary = llamaAiService.generatePatientSummary(soapNote);
        
        // Extract prescription
        String prescription = llamaAiService.extractPrescription(soapNote);
        
        // Create and save medical record
        MedicalRecord record = MedicalRecord.builder()
            .appointmentId(appointmentId)
            .soapNote(soapNote)
            .patientSummary(patientSummary)
            .prescription(prescription)
            .isSigned(false)
            .build();
        
        // Update appointment status to COMPLETED
        apt.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(apt);
        
        return medicalRecordRepository.save(record);
    }
    
    /**
     * Sign a medical record
     */
    @Transactional
    public MedicalRecord signMedicalRecord(Long recordId) {
        Optional<MedicalRecord> record = medicalRecordRepository.findById(recordId);
        if (record.isPresent()) {
            record.get().setIsSigned(true);
            return medicalRecordRepository.save(record.get());
        }
        throw new IllegalArgumentException("Medical record not found with ID: " + recordId);
    }
}
