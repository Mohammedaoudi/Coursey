package com.ensa.projet.participantservice.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "training_enrollments")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "participant_id")
    private Participant participant;

    private Integer trainingId;

    @Column(name = "enrollment_date")
    private Date enrollmentDate=new Date();


    @Enumerated(EnumType.STRING)
    private EnrollmentStatus status=EnrollmentStatus.IN_PROGRESS;

    @OneToMany(mappedBy = "enrollment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ModuleProgress> moduleProgresses = new ArrayList<>();


}
