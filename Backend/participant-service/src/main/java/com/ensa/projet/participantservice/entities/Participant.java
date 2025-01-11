package com.ensa.projet.participantservice.entities;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "participants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Participant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;



    @Column(unique = true)
    private String userId;

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL)
    private List<Enrollment> enrollments = new ArrayList<>();

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL)
    private List<Certification> certifications = new ArrayList<>();


}
