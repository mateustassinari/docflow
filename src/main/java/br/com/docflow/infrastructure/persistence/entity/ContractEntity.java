package br.com.docflow.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(
    name = "contracts",
    indexes = {
        @Index(name = "idx_contracts_status", columnList = "status"),
        @Index(name = "idx_contracts_autentique_id", columnList = "autentique_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_contracts_event_id", columnNames = "event_id")
    }
)
public class ContractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private EventEntity event;

    @ManyToOne(optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private TemplateContratoEntity template;

    @Column(nullable = false)
    private String status;

    @Column(name = "pdf_url")
    private String pdfUrl;

    @Column(name = "autentique_id")
    private String autentiqueId;
}
