package com.wethinkcode.demo.presentation;

import com.wethinkcode.demo.domain.shared.Appointment;
import com.wethinkcode.demo.domain.shared.MedicalRecord;
import com.wethinkcode.demo.domain.shared.User;
import com.wethinkcode.demo.infrastructure.persistence.UserRepository;
import com.wethinkcode.demo.infrastructure.persistence.AppointmentRepository;
import com.wethinkcode.demo.infrastructure.persistence.MedicalRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/patient")
@RequiredArgsConstructor
public class PatientController {
    
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    
    /**
     * Patient View - shows their medical records and summaries
     */
    @GetMapping("/view")
    public String getPatientView(
            @RequestParam(defaultValue = "1") Long patientId,
            Model model) {
        
        Optional<User> patient = userRepository.findById(patientId);
        List<Appointment> appointments = appointmentRepository.findByPatientId(patientId);
        
        model.addAttribute("patient", patient.orElse(null));
        model.addAttribute("patientId", patientId);
        model.addAttribute("appointments", appointments);
        
        return "patient-view";
    }
    
    /**
     * View detailed medical record with simplified summary
     */
    @GetMapping("/record/{appointmentId}")
    public String viewMedicalRecord(
            @PathVariable Long appointmentId,
            @RequestParam(defaultValue = "1") Long patientId,
            Model model) {
        
        Optional<Appointment> appointment = appointmentRepository.findById(appointmentId);
        Optional<MedicalRecord> record = medicalRecordRepository.findByAppointmentId(appointmentId);
        Optional<User> patient = userRepository.findById(patientId);
        
        model.addAttribute("patient", patient.orElse(null));
        model.addAttribute("appointment", appointment.orElse(null));
        model.addAttribute("record", record.orElse(null));
        model.addAttribute("patientId", patientId);
        
        return "patient-record";
    }
}
