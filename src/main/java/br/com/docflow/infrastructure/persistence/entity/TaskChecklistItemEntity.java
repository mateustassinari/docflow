package br.com.docflow.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "task_checklist_items")
public class TaskChecklistItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TaskEntity task;

    @Column(nullable = false)
    private String texto;

    @Column(nullable = false)
    private boolean concluido;

    @Column(nullable = false)
    private Integer ordem = 0;
}
