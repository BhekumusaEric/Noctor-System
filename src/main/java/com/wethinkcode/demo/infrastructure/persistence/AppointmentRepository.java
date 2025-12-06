package com.wethinkcode.demo.infrastructure.persistence;

import com.wethinkcode.demo.domain.shared.Appointment;
import com.wethinkcode.demo.domain.shared.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByStatus(AppointmentStatus status);
    List<Appointment> findByDoctorId(Long doctorId);
    List<Appointment> findByPatientId(Long patientId);
}
