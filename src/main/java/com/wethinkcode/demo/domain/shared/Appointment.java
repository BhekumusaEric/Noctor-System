package com.wethinkcode.demo.domain.shared;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "appointments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private User doctor;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TriagePriority triagePriority;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;
    
    @Column(columnDefinition = "TEXT")
    private String roughNotes;
}
