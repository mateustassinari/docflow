package br.com.docflow.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "service_type_cost_defaults")
public class ServiceTypeCostDefaultEntity {

    @Id
    @Column(name = "tipo_servico")
    private String tipoServico;

    @Column(name = "custo_default", nullable = false)
    private BigDecimal custoDefault;
}
