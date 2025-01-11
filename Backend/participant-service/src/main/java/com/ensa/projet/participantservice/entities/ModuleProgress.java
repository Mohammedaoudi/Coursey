package com.ensa.projet.participantservice.entities;

import com.ensa.projet.participantservice.dto.ModuleProgressDto;
import jakarta.persistence.*;
import lombok.*;


import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "module_progress")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "enrollment_id")
    private Enrollment enrollment;

    private Integer moduleId;
    private String moduleName;

    @OneToMany(mappedBy = "moduleProgress")
    private List<ContentProgress> contentProgress=new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private ModuleStatus status;

    public ModuleProgressDto toDto() {
        return ModuleProgressDto.builder()
                .id(this.id)
                .moduleId(this.moduleId)
                .status(this.status)
                .moduleName(this.moduleName)
                .build();
    }



}