package com.wethinkcode.demo.domain.shared;

import com.wethinkcode.demo.infrastructure.persistence.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TriageService {
    
    private final AppointmentRepository appointmentRepository;
    
    /**
     * Fetch all WAITING appointments sorted by priority (HIGH first)
     */
    public List<Appointment> getWaitingAppointmentsByPriority() {
        List<Appointment> waitingAppointments = appointmentRepository.findByStatus(AppointmentStatus.WAITING);
        
        // Sort by priority: HIGH -> MEDIUM -> LOW
        waitingAppointments.sort((apt1, apt2) -> {
            int priority1 = getPriorityValue(apt1.getTriagePriority());
            int priority2 = getPriorityValue(apt2.getTriagePriority());
            return Integer.compare(priority2, priority1); // Descending order (HIGH first)
        });
        
        return waitingAppointments;
    }
    
    /**
     * Get the next high-priority appointment
     */
    public Appointment getNextHighPriorityAppointment() {
        List<Appointment> prioritized = getWaitingAppointmentsByPriority();
        return prioritized.isEmpty() ? null : prioritized.get(0);
    }
    
    private int getPriorityValue(TriagePriority priority) {
        return switch (priority) {
            case HIGH -> 3;
            case MEDIUM -> 2;
            case LOW -> 1;
        };
    }
}
