package com.ensa.projet.trainingservice.model.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "training")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Training {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    private String iconPath;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level")
    private DifficultyLevel difficultyLevel;

    @Column(name = "estimated_duration")
    private Integer estimatedDurationMinutes;

    private String goals;
    private String prerequisites;

    @Column(name = "support_ar")
    private boolean supportAR;

    @Column(name = "support_ai")
    private boolean supportAI;

    @Column(name = "url_ytb")
    private String urlYtb;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")  // This will create the foreign key
    private Category category;

    @ElementCollection
    @CollectionTable(
            name = "training_instructions",
            joinColumns = @JoinColumn(name = "training_id")
    )
    @Column(name = "instruction", length = 500)
    @OrderColumn(name = "instruction_order")
    private List<String> instructions = new ArrayList<>();

    @OneToMany(mappedBy = "training", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Module> modules = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_published")
    private boolean published;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}