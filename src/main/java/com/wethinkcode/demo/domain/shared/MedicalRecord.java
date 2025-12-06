package com.wethinkcode.demo.domain.shared;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "medical_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long appointmentId;
    
    @Column(columnDefinition = "TEXT")
    private String soapNote;
    
    @Column(columnDefinition = "TEXT")
    private String patientSummary;
    
    @Column(columnDefinition = "TEXT")
    private String prescription;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean isSigned = false;
}
