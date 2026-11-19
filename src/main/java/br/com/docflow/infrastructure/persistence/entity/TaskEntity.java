package br.com.docflow.infrastructure.persistence.entity;

import br.com.docflow.domain.model.TaskStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "tasks")
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private EventEntity event;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "prazo_data", nullable = false)
    private LocalDate prazoData;

    @Column(name = "prazo_data_fim")
    private LocalDate prazoDataFim;

    @Column(name = "prazo_horario", nullable = false)
    private LocalTime prazoHorario;

    @Column(name = "prazo_horario_fim")
    private LocalTime prazoHorarioFim;

    @Column(name = "all_day", nullable = false)
    private boolean allDay = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.A_FAZER;

    @Column(name = "tipo_servico")
    private String tipoServico;

    @Column(name = "automation_source")
    private String automationSource;

    @Column(name = "automation_contract_id")
    private Long automationContractId;

    @Column(name = "automation_key")
    private String automationKey;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC, id ASC")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<TaskChecklistItemEntity> checklistItems = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC, id ASC")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<TaskCommentEntity> comments = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<TaskLabelEntity> labels = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC, id ASC")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<TaskAttachmentEntity> attachments = new ArrayList<>();

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
