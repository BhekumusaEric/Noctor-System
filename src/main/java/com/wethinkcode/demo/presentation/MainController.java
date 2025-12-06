package com.wethinkcode.demo.presentation;

import com.wethinkcode.demo.domain.shared.User;
import com.wethinkcode.demo.domain.shared.Appointment;
import com.wethinkcode.demo.domain.shared.UserRole;
import com.wethinkcode.demo.infrastructure.persistence.UserRepository;
import com.wethinkcode.demo.infrastructure.persistence.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class MainController {
    
    private final UserRepository userRepository;
    
    /**
     * Main landing page with user role selector
     */
    @GetMapping
    public String index(Model model) {
        List<User> doctors = userRepository.findByRole(UserRole.DOCTOR);
        List<User> nurses = userRepository.findByRole(UserRole.NURSE);
        List<User> patients = userRepository.findByRole(UserRole.PATIENT);
        
        model.addAttribute("doctors", doctors);
        model.addAttribute("nurses", nurses);
        model.addAttribute("patients", patients);
        
        return "index";
    }
    
    /**
     * Quick role switcher - redirect to appropriate dashboard
     */
    @PostMapping("/switch-user")
    public String switchUser(
            @RequestParam Long userId,
            Model model) {
        
        java.util.Optional<User> user = userRepository.findById(userId);
        
        if (user.isPresent()) {
            switch (user.get().getRole()) {
                case DOCTOR:
                    return "redirect:/doctor/dashboard?doctorId=" + userId;
                case NURSE:
                    return "redirect:/nurse/dashboard?nurseId=" + userId;
                case PATIENT:
                    return "redirect:/patient/view?patientId=" + userId;
                default:
                    return "redirect:/";
            }
        }
        
        return "redirect:/";
    }
    
    /**
     * API endpoint to seed sample data for testing
     */
    @PostMapping("/api/seed-data")
    public String seedTestData(Model model) {
        // Create sample doctors
        User doctor1 = User.builder()
            .name("Dr. Emily Stone")
            .role(UserRole.DOCTOR)
            .status("AVAILABLE")
            .build();
        
        User doctor2 = User.builder()
            .name("Dr. James Wilson")
            .role(UserRole.DOCTOR)
            .status("AVAILABLE")
            .build();
        
        // Create sample nurses
        User nurse1 = User.builder()
            .name("Nurse Sarah Johnson")
            .role(UserRole.NURSE)
            .status("AVAILABLE")
            .build();
        
        // Create sample patients
        User patient1 = User.builder()
            .name("John Doe")
            .role(UserRole.PATIENT)
            .status("WAITING")
            .build();
        
        User patient2 = User.builder()
            .name("Jane Smith")
            .role(UserRole.PATIENT)
            .status("WAITING")
            .build();
        
        userRepository.saveAll(List.of(doctor1, doctor2, nurse1, patient1, patient2));
        
        return "redirect:/";
    }
}
