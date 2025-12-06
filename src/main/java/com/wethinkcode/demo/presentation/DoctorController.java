package com.wethinkcode.demo.presentation;

import com.wethinkcode.demo.domain.shared.Appointment;
import com.wethinkcode.demo.domain.shared.MedicalRecord;
import com.wethinkcode.demo.domain.shared.User;
import com.wethinkcode.demo.domain.doctor.DoctorService;
import com.wethinkcode.demo.domain.shared.TriageService;
import com.wethinkcode.demo.domain.shared.AppointmentStatus;
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
@RequestMapping("/doctor")
@RequiredArgsConstructor
public class DoctorController {
    
    private final DoctorService doctorService;
    private final TriageService triageService;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    
    /**
     * Doctor Dashboard - shows waiting appointments
     */
    @GetMapping("/dashboard")
    public String getDoctorDashboard(
            @RequestParam(defaultValue = "1") Long doctorId,
            Model model) {
        
        Optional<User> doctor = userRepository.findById(doctorId);
        List<Appointment> waitingAppointments = triageService.getWaitingAppointmentsByPriority();
        List<Appointment> doctorAppointments = doctorService.getDoctorAppointments(doctorId);
        
        model.addAttribute("doctor", doctor.orElse(null));
        model.addAttribute("doctorId", doctorId);
        model.addAttribute("waitingAppointments", waitingAppointments);
        model.addAttribute("doctorAppointments", doctorAppointments);
        
        return "doctor-dashboard";
    }
    
    /**
     * Accept an appointment (assign doctor)
     */
    @PostMapping("/accept-appointment/{appointmentId}")
    public String acceptAppointment(
            @PathVariable Long appointmentId,
            @RequestParam Long doctorId,
            Model model) {
        
        Optional<Appointment> appointment = appointmentRepository.findById(appointmentId);
        Optional<User> doctor = userRepository.findById(doctorId);
        
        if (appointment.isPresent() && doctor.isPresent()) {
            Appointment apt = appointment.get();
            apt.setDoctor(doctor.get());
            apt.setStatus(AppointmentStatus.IN_CONSULT);
            appointmentRepository.save(apt);
        }
        
        return "redirect:/doctor/dashboard?doctorId=" + doctorId;
    }
    
    /**
     * Show appointment details and recording interface
     */
    @GetMapping("/appointment/{appointmentId}")
    public String viewAppointment(
            @PathVariable Long appointmentId,
            @RequestParam(defaultValue = "1") Long doctorId,
            Model model) {
        
        Optional<Appointment> appointment = appointmentRepository.findById(appointmentId);
        Optional<MedicalRecord> record = medicalRecordRepository.findByAppointmentId(appointmentId);
        
        model.addAttribute("appointment", appointment.orElse(null));
        model.addAttribute("record", record.orElse(null));
        model.addAttribute("doctorId", doctorId);
        
        return "appointment-detail";
    }
    
    /**
     * Record consultation notes and generate medical record
     */
    @PostMapping("/record-consultation/{appointmentId}")
    public String recordConsultation(
            @PathVariable Long appointmentId,
            @RequestParam String roughNotes,
            @RequestParam Long doctorId,
            Model model) {
        
        try {
            MedicalRecord record = doctorService.generateAndSaveMedicalRecord(appointmentId, roughNotes);
            return "redirect:/doctor/appointment/" + appointmentId + "?doctorId=" + doctorId;
        } catch (Exception e) {
            model.addAttribute("error", "Error generating medical record: " + e.getMessage());
            return "redirect:/doctor/appointment/" + appointmentId + "?doctorId=" + doctorId;
        }
    }
    
    /**
     * Sign medical record
     */
    @PostMapping("/sign-record/{recordId}")
    public String signRecord(
            @PathVariable Long recordId,
            @RequestParam Long doctorId,
            @RequestParam Long appointmentId) {
        
        doctorService.signMedicalRecord(recordId);
        return "redirect:/doctor/appointment/" + appointmentId + "?doctorId=" + doctorId;
    }
}
