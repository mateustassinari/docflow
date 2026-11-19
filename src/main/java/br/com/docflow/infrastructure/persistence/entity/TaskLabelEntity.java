package br.com.docflow.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "task_labels")
public class TaskLabelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TaskEntity task;

    @Column(nullable = false)
    private String label;
}
