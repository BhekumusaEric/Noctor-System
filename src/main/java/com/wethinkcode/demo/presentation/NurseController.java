package com.wethinkcode.demo.presentation;

import com.wethinkcode.demo.domain.shared.Appointment;
import com.wethinkcode.demo.domain.shared.User;
import com.wethinkcode.demo.domain.shared.TriagePriority;
import com.wethinkcode.demo.domain.shared.UserRole;
import com.wethinkcode.demo.domain.shared.AppointmentStatus;
import com.wethinkcode.demo.domain.shared.TriageService;
import com.wethinkcode.demo.infrastructure.persistence.AppointmentRepository;
import com.wethinkcode.demo.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/nurse")
@RequiredArgsConstructor
public class NurseController {
    
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final TriageService triageService;
    
    /**
     * Nurse Dashboard - shows all patients and triage queue
     */
    @GetMapping("/dashboard")
    public String getNurseDashboard(
            @RequestParam(defaultValue = "1") Long nurseId,
            Model model) {
        
        Optional<User> nurse = userRepository.findById(nurseId);
        List<Appointment> waitingAppointments = triageService.getWaitingAppointmentsByPriority();
        List<User> availableDoctors = userRepository.findByStatus("AVAILABLE");
        
        model.addAttribute("nurse", nurse.orElse(null));
        model.addAttribute("nurseId", nurseId);
        model.addAttribute("waitingAppointments", waitingAppointments);
        model.addAttribute("availableDoctors", availableDoctors);
        
        return "nurse-dashboard";
    }
    
    /**
     * Register a new patient appointment
     */
    @PostMapping("/register-patient")
    public String registerPatient(
            @RequestParam String patientName,
            @RequestParam String triagePriority,
            @RequestParam Long nurseId,
            Model model) {
        
        // Create new patient user
        User patient = User.builder()
            .name(patientName)
            .role(UserRole.PATIENT)
            .status("WAITING")
            .build();
        User savedPatient = userRepository.save(patient);
        
        // Create appointment
        Appointment appointment = Appointment.builder()
            .patient(savedPatient)
            .triagePriority(TriagePriority.valueOf(triagePriority))
            .status(AppointmentStatus.WAITING)
            .roughNotes("")
            .build();
        appointmentRepository.save(appointment);
        
        return "redirect:/nurse/dashboard?nurseId=" + nurseId;
    }
    
    /**
     * Assign a doctor to a waiting patient
     */
    @PostMapping("/assign-doctor/{appointmentId}")
    @Transactional
    public String assignDoctor(
            @PathVariable Long appointmentId,
            @RequestParam Long doctorId,
            @RequestParam Long nurseId) {
        
        Optional<Appointment> appointment = appointmentRepository.findById(appointmentId);
        Optional<User> doctor = userRepository.findById(doctorId);
        
        if (appointment.isPresent() && doctor.isPresent()) {
            Appointment apt = appointment.get();
            apt.setDoctor(doctor.get());
            apt.setStatus(AppointmentStatus.IN_CONSULT);
            appointmentRepository.save(apt);
        }
        
        return "redirect:/nurse/dashboard?nurseId=" + nurseId;
    }
}
