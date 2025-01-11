package com.ensa.projet.participantservice.service.imple;

import com.ensa.projet.participantservice.client.TrainingServiceClient;
import com.ensa.projet.participantservice.dto.EnrollmentDto;
import com.ensa.projet.participantservice.dto.TrainingDTO;
import com.ensa.projet.participantservice.entities.Enrollment;
import com.ensa.projet.participantservice.entities.ModuleProgress;
import com.ensa.projet.participantservice.entities.ModuleStatus;
import com.ensa.projet.participantservice.entities.Participant;
import com.ensa.projet.participantservice.entities.EnrollmentStatus;
import com.ensa.projet.participantservice.kafka.NotificationProducer;
import com.ensa.projet.participantservice.repository.ModuleProgressRepository;
import com.ensa.projet.participantservice.repository.TrainingEnrollmentRepository;
import com.ensa.projet.participantservice.repository.ParticipantRepository;
import com.ensa.projet.participantservice.service.interfaces.EnrollmentService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {
    private final TrainingServiceClient trainingServiceClient;
    private final TrainingEnrollmentRepository enrollmentRepository;
    private final ParticipantRepository participantRepository;
    private final ModuleProgressRepository moduleProgressRepository;
    private final NotificationProducer notificationProducer;


    public EnrollmentServiceImpl(TrainingServiceClient trainingServiceClient,
                                 TrainingEnrollmentRepository enrollmentRepository,
                                 ParticipantRepository participantRepository,
                                 ModuleProgressRepository moduleProgressRepository,
                                 NotificationProducer notificationProducer) {
        this.trainingServiceClient = trainingServiceClient;
        this.enrollmentRepository = enrollmentRepository;
        this.participantRepository = participantRepository;
        this.moduleProgressRepository = moduleProgressRepository;
        this.notificationProducer = notificationProducer;

    }

    @Override
    public EnrollmentDto enrollInTraining(Integer participantId, Integer trainingId) {
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found"));

        if (enrollmentRepository.findByParticipantIdAndTrainingId(participantId, trainingId) != null) {
            throw new IllegalStateException("Already enrolled");
        }

        TrainingDTO training = trainingServiceClient.getTraining(trainingId);
        Enrollment enrollment = createEnrollment(participant, trainingId, training);
        enrollmentRepository.save(enrollment);

        return mapToEnrollmentDto(enrollment);
    }

    @Override
    public EnrollmentDto getEnrollment(Integer participantId, Integer trainingId) {
        Enrollment enrollment = enrollmentRepository.findByParticipantIdAndTrainingId(participantId, trainingId);
        if (enrollment == null) {
            return createEmptyEnrollmentDto(participantId, trainingId);
        }
        return mapToEnrollmentDto(enrollment);
    }

    @Override
    public List<EnrollmentDto> getParticipantEnrollments(Integer participantId) {
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found"));

        return participant.getEnrollments().stream()
                .map(this::mapToEnrollmentDto)
                .toList();
    }

    @Override
    public void markModuleComplete(Integer enrollmentId, Integer moduleId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found"));

        ModuleProgress currentModule = findModuleProgress(enrollment, moduleId);
        updateModuleStatus(currentModule, enrollment);
    }

    @Override
    public EnrollmentDto findEnrollemntById(Integer enrollmentId) {
        // Find the enrollment by its ID using the repository
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found"));

        // Map the entity to a DTO using the existing mapping function
        return mapToEnrollmentDto(enrollment);
    }


    private Enrollment createEnrollment(Participant participant, Integer trainingId, TrainingDTO training) {
        if (training == null) {
            throw new IllegalArgumentException("Training not found");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setParticipant(participant);
        enrollment.setTrainingId(trainingId);
        enrollment.setStatus(EnrollmentStatus.IN_PROGRESS);
        enrollment.setEnrollmentDate(new Date());

        List<ModuleProgress> moduleProgresses = createModuleProgresses(training, enrollment);
        enrollment.setModuleProgresses(moduleProgresses);

        return enrollment;
    }

    private List<ModuleProgress> createModuleProgresses(TrainingDTO training, Enrollment enrollment) {
        if (training.getModules() == null) {
            return new ArrayList<>();
        }

        return training.getModules().stream()
                .map(module -> ModuleProgress.builder()
                        .moduleId(module.getId())
                        .moduleName(module.getTitle())
                        .status(module.getOrderIndex() == 1 ? ModuleStatus.IN_PROGRESS : ModuleStatus.ENROLLED)
                        .enrollment(enrollment)
                        .build())
                .toList();
    }

    private EnrollmentDto createEmptyEnrollmentDto(Integer participantId, Integer trainingId) {
        return EnrollmentDto.builder()
                .id(null)
                .participantId(participantId)
                .trainingId(trainingId)
                .status(null)
                .enrollmentDate(null)
                .moduleProgresses(new ArrayList<>())
                .build();
    }

    private EnrollmentDto mapToEnrollmentDto(Enrollment enrollment) {
        return EnrollmentDto.builder()
                .id(enrollment.getId())
                .participantId(enrollment.getParticipant().getId())
                .trainingId(enrollment.getTrainingId())
                .status(enrollment.getStatus())
                .enrollmentDate(enrollment.getEnrollmentDate())
                .moduleProgresses(enrollment.getModuleProgresses().stream()
                        .map(ModuleProgress::toDto)
                        .toList())
                .build();
    }

    private ModuleProgress findModuleProgress(Enrollment enrollment, Integer moduleId) {
        return enrollment.getModuleProgresses().stream()
                .filter(mp -> mp.getModuleId().equals(moduleId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Module not found"));
    }

    private void updateModuleStatus(ModuleProgress currentModule, Enrollment enrollment) {
        if (currentModule.getStatus() != ModuleStatus.IN_PROGRESS) {
            throw new IllegalStateException("Module must be IN_PROGRESS to be marked complete");
        }

        currentModule.setStatus(ModuleStatus.COMPLETED);
        moduleProgressRepository.save(currentModule);

        updateNextModule(enrollment);
        checkAndUpdateEnrollmentStatus(enrollment);
    }

    private void updateNextModule(Enrollment enrollment) {
        enrollment.getModuleProgresses().stream()
                .filter(mp -> mp.getStatus() == ModuleStatus.ENROLLED)
                .min(Comparator.comparing(ModuleProgress::getModuleId))
                .ifPresent(nextModule -> {
                    nextModule.setStatus(ModuleStatus.IN_PROGRESS);
                    moduleProgressRepository.save(nextModule);
                });
    }

    private void checkAndUpdateEnrollmentStatus(Enrollment enrollment) {
        boolean allCompleted = enrollment.getModuleProgresses().stream()
                .allMatch(mp -> mp.getStatus() == ModuleStatus.COMPLETED);

        if (allCompleted) {
            enrollment.setStatus(EnrollmentStatus.COMPLETED);
            Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

            // After completing all modules, send a certificate notification
            String certificateNumber = "CERT-" + enrollment.getParticipant().getId() +
                    "-" + enrollment.getTrainingId() + "-" + System.currentTimeMillis();

            notificationProducer.sendCertificationNotification(
                    savedEnrollment.getParticipant().getId(),
                    savedEnrollment.getTrainingId(),
                    certificateNumber
            );
        }
    }
}