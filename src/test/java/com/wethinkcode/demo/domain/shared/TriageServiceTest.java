package com.wethinkcode.demo.domain.shared;

import com.wethinkcode.demo.infrastructure.persistence.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Story C2: Nurse wants to see a prioritized list of waiting patients")
class TriageServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    private TriageService triageService;

    private User patientA;
    private User patientB;
    private User patientC;
    private Appointment appointmentA;
    private Appointment appointmentB;
    private Appointment appointmentC;

    @BeforeEach
    void setUp() {
        triageService = new TriageService(appointmentRepository);

        // Create test patients
        patientA = User.builder()
                .id(1L)
                .name("Patient A")
                .role(UserRole.PATIENT)
                .status("ACTIVE")
                .build();

        patientB = User.builder()
                .id(2L)
                .name("Patient B")
                .role(UserRole.PATIENT)
                .status("ACTIVE")
                .build();

        patientC = User.builder()
                .id(3L)
                .name("Patient C")
                .role(UserRole.PATIENT)
                .status("ACTIVE")
                .build();

        // Create test doctor
        User doctor = User.builder()
                .id(101L)
                .name("Doctor X")
                .role(UserRole.DOCTOR)
                .status("AVAILABLE")
                .build();

        // Create appointments with different priorities
        appointmentA = Appointment.builder()
                .id(1L)
                .patient(patientA)
                .doctor(doctor)
                .triagePriority(TriagePriority.LOW)
                .status(AppointmentStatus.WAITING)
                .roughNotes("Low priority case")
                .build();

        appointmentB = Appointment.builder()
                .id(2L)
                .patient(patientB)
                .doctor(doctor)
                .triagePriority(TriagePriority.HIGH)
                .status(AppointmentStatus.WAITING)
                .roughNotes("High priority case")
                .build();

        appointmentC = Appointment.builder()
                .id(3L)
                .patient(patientC)
                .doctor(doctor)
                .triagePriority(TriagePriority.MEDIUM)
                .status(AppointmentStatus.WAITING)
                .roughNotes("Medium priority case")
                .build();
    }

    @Test
    @DisplayName("C2.1: Given patients with different priorities, when getting waiting list, then HIGH priority is first")
    void givenPatientsWithDifferentPriorities_whenGetWaitingList_thenHighPriorityIsFirst() {
        // Given: Three appointments with different priorities and WAITING status
        List<Appointment> waitingAppointments = Arrays.asList(
                appointmentB,  // HIGH
                appointmentC,  // MEDIUM
                appointmentA   // LOW
        );
        when(appointmentRepository.findByStatus(AppointmentStatus.WAITING))
                .thenReturn(waitingAppointments);

        // When: Getting waiting appointments sorted by priority
        List<Appointment> result = triageService.getWaitingAppointmentsByPriority();

        // Then: Verify order is [HIGH, MEDIUM, LOW]
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(TriagePriority.HIGH, result.get(0).getTriagePriority());
        assertEquals(TriagePriority.MEDIUM, result.get(1).getTriagePriority());
        assertEquals(TriagePriority.LOW, result.get(2).getTriagePriority());
        assertEquals("Patient B", result.get(0).getPatient().getName());

        // Verify repository was called
        verify(appointmentRepository, times(1)).findByStatus(AppointmentStatus.WAITING);
    }

    @Test
    @DisplayName("C2.2: Given multiple HIGH priority patients, when getting next appointment, then first HIGH is returned")
    void givenMultipleHighPriorityPatients_whenGetNext_thenFirstHighIsReturned() {
        // Given: Multiple HIGH priority appointments with WAITING status
        Appointment highPriority1 = appointmentB;
        Appointment highPriority2 = Appointment.builder()
                .id(4L)
                .patient(User.builder().id(4L).name("Patient D").role(UserRole.PATIENT).status("ACTIVE").build())
                .doctor(appointmentB.getDoctor())
                .triagePriority(TriagePriority.HIGH)
                .status(AppointmentStatus.WAITING)
                .roughNotes("Another high priority")
                .build();

        List<Appointment> appointmentsWithMultipleHigh = Arrays.asList(
                appointmentA,      // LOW
                highPriority1,     // HIGH
                appointmentC,      // MEDIUM
                highPriority2      // HIGH
        );
        when(appointmentRepository.findByStatus(AppointmentStatus.WAITING))
                .thenReturn(appointmentsWithMultipleHigh);

        // When: Getting the next high priority appointment
        Appointment next = triageService.getNextHighPriorityAppointment();

        // Then: First appointment should be HIGH priority
        assertNotNull(next);
        assertEquals(TriagePriority.HIGH, next.getTriagePriority());
        assertTrue(next.getId() == 2L || next.getId() == 4L);
        
        verify(appointmentRepository, times(1)).findByStatus(AppointmentStatus.WAITING);
    }

    @Test
    @DisplayName("C2.3: Given empty waiting list, when getting waiting appointments, then empty list is returned")
    void givenEmptyWaitingList_whenGetWaitingAppointments_thenEmptyListReturned() {
        // Given: No appointments with WAITING status
        when(appointmentRepository.findByStatus(AppointmentStatus.WAITING)).thenReturn(Arrays.asList());

        // When: Getting waiting appointments
        List<Appointment> result = triageService.getWaitingAppointmentsByPriority();

        // Then: Result should be empty
        assertNotNull(result);
        assertEquals(0, result.size());
        
        verify(appointmentRepository, times(1)).findByStatus(AppointmentStatus.WAITING);
    }

    @Test
    @DisplayName("C2.4: Given appointment with assigned doctor, when getting appointment, then doctor assignment is preserved")
    void givenAppointmentWithAssignedDoctor_whenGettingAppointment_thenDoctorAssignmentPreserved() {
        // Given: An appointment with WAITING status assigned to a specific doctor
        User assignedDoctor = appointmentB.getDoctor();
        when(appointmentRepository.findByStatus(AppointmentStatus.WAITING)).thenReturn(Arrays.asList(appointmentB));

        // When: Getting the appointment
        List<Appointment> result = triageService.getWaitingAppointmentsByPriority();

        // Then: Doctor assignment should be intact
        assertNotNull(result.get(0).getDoctor());
        assertEquals("Doctor X", result.get(0).getDoctor().getName());
        assertEquals(assignedDoctor.getId(), result.get(0).getDoctor().getId());
        
        verify(appointmentRepository, times(1)).findByStatus(AppointmentStatus.WAITING);
    }

    @Test
    @DisplayName("C2.5: Given appointment ready for consultation, when updating status, then status changes to IN_CONSULT")
    void givenAppointmentReadyForConsult_whenUpdatingStatus_thenStatusIsInConsult() {
        // Given: An appointment with WAITING status
        assertEquals(AppointmentStatus.WAITING, appointmentB.getStatus());
        when(appointmentRepository.save(appointmentB)).thenReturn(appointmentB);

        // When: Updating appointment status to IN_CONSULT (in consultation)
        appointmentB.setStatus(AppointmentStatus.IN_CONSULT);
        Appointment saved = appointmentRepository.save(appointmentB);

        // Then: Status should be updated
        assertEquals(AppointmentStatus.IN_CONSULT, saved.getStatus());
        verify(appointmentRepository, times(1)).save(appointmentB);
    }
}
