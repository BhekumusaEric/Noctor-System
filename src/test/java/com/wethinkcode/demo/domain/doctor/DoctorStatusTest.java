package com.wethinkcode.demo.domain.doctor;

import com.wethinkcode.demo.domain.shared.User;
import com.wethinkcode.demo.domain.shared.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Story C1: Doctor toggles availability status")
class DoctorStatusTest {

    private User doctor;

    @BeforeEach
    void setUp() {
        // Create test doctor
        doctor = User.builder()
                .id(101L)
                .name("Dr. Smith")
                .role(UserRole.DOCTOR)
                .status("AVAILABLE")
                .build();
    }

    @Test
    @DisplayName("C1.1: Given doctor available, when toggling to IN_SURGERY, then status updates correctly")
    void givenDoctorAvailable_whenToggleToSurgery_thenStatusIsSurgery() {
        // Given: Doctor with AVAILABLE status
        assertEquals("AVAILABLE", doctor.getStatus());
        assertEquals(UserRole.DOCTOR, doctor.getRole());

        // When: Toggling status to IN_SURGERY
        doctor.setStatus("IN_SURGERY");

        // Then: Status should be updated to IN_SURGERY
        assertEquals("IN_SURGERY", doctor.getStatus());
        assertEquals("Dr. Smith", doctor.getName());
        assertEquals(UserRole.DOCTOR, doctor.getRole());
    }

    @Test
    @DisplayName("C1.2: Given doctor in surgery, when toggling to ON_BREAK, then status updates")
    void givenDoctorInSurgery_whenToggleToOnBreak_thenStatusUpdates() {
        // Given: Doctor with IN_SURGERY status
        doctor.setStatus("IN_SURGERY");
        assertEquals("IN_SURGERY", doctor.getStatus());

        // When: Toggling status to ON_BREAK
        doctor.setStatus("ON_BREAK");

        // Then: Status should be updated to ON_BREAK
        assertEquals("ON_BREAK", doctor.getStatus());
    }

    @Test
    @DisplayName("C1.3: Given doctor on break, when toggling to AVAILABLE, then doctor becomes available")
    void givenDoctorOnBreak_whenToggleToAvailable_thenDoctorBecomesAvailable() {
        // Given: Doctor with ON_BREAK status
        doctor.setStatus("ON_BREAK");
        assertEquals("ON_BREAK", doctor.getStatus());

        // When: Toggling status back to AVAILABLE
        doctor.setStatus("AVAILABLE");

        // Then: Doctor should be AVAILABLE again
        assertEquals("AVAILABLE", doctor.getStatus());
    }

    @Test
    @DisplayName("C1.4: Given available doctor, when toggling OFF_DUTY, then doctor is not accepting appointments")
    void givenAvailableDoctor_whenToggleOffDuty_thenDoctorNotAcceptingAppointments() {
        // Given: Available doctor
        assertEquals("AVAILABLE", doctor.getStatus());

        // When: Setting status to OFF_DUTY
        doctor.setStatus("OFF_DUTY");

        // Then: Doctor should not be available for appointments
        assertEquals("OFF_DUTY", doctor.getStatus());
        assertNotEquals("AVAILABLE", doctor.getStatus());
    }

    @Test
    @DisplayName("C1.5: Given multiple doctors with different statuses, then each maintains their own status")
    void givenMultipleDoctorsWithDifferentStatuses_thenEachMaintainsOwnStatus() {
        // Given: Multiple doctors with different statuses
        User doctor1 = User.builder()
                .id(101L)
                .name("Dr. Smith")
                .role(UserRole.DOCTOR)
                .status("AVAILABLE")
                .build();

        User doctor2 = User.builder()
                .id(102L)
                .name("Dr. Johnson")
                .role(UserRole.DOCTOR)
                .status("IN_SURGERY")
                .build();

        User doctor3 = User.builder()
                .id(103L)
                .name("Dr. Williams")
                .role(UserRole.DOCTOR)
                .status("AVAILABLE")
                .build();

        // Then: Each doctor maintains their own status
        assertEquals("AVAILABLE", doctor1.getStatus());
        assertEquals("IN_SURGERY", doctor2.getStatus());
        assertEquals("AVAILABLE", doctor3.getStatus());

        // And: Only available doctors can see patients
        assertTrue(doctor1.getStatus().equals("AVAILABLE"));
        assertFalse(doctor2.getStatus().equals("AVAILABLE"));
        assertTrue(doctor3.getStatus().equals("AVAILABLE"));
    }

    @Test
    @DisplayName("C1.6: Given doctor status change, then change is reflected in entity")
    void givenDoctorStatusChange_thenChangeIsReflected() {
        // Given: Doctor with initial status
        String initialStatus = doctor.getStatus();
        assertEquals("AVAILABLE", initialStatus);

        // When: Changing doctor status
        doctor.setStatus("IN_SURGERY");

        // Then: Change should be reflected
        assertEquals("IN_SURGERY", doctor.getStatus());
        assertNotEquals(initialStatus, doctor.getStatus());
    }

    @Test
    @DisplayName("C1.7: Given doctor with status, then correct status can be retrieved")
    void givenDoctorWithStatus_thenCorrectStatusRetrieved() {
        // Given: Doctor saved with specific status
        doctor.setStatus("ON_BREAK");

        // When: Retrieving doctor status
        String retrievedStatus = doctor.getStatus();

        // Then: Retrieved doctor should have correct status
        assertEquals("ON_BREAK", retrievedStatus);
        assertEquals("Dr. Smith", doctor.getName());
        assertEquals(UserRole.DOCTOR, doctor.getRole());
    }
}
