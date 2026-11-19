package br.com.docflow.infrastructure.persistence.mapper;

import br.com.docflow.domain.model.ServiceTypeCostDefault;
import br.com.docflow.infrastructure.persistence.entity.ServiceTypeCostDefaultEntity;

public class ServiceTypeCostDefaultMapper {

    public static ServiceTypeCostDefault toDomain(ServiceTypeCostDefaultEntity entity) {
        if (entity == null) return null;
        ServiceTypeCostDefault domain = new ServiceTypeCostDefault();
        domain.setTipoServico(entity.getTipoServico());
        domain.setCustoDefault(entity.getCustoDefault());
        return domain;
    }

    public static ServiceTypeCostDefaultEntity toEntity(ServiceTypeCostDefault domain) {
        if (domain == null) return null;
        ServiceTypeCostDefaultEntity entity = new ServiceTypeCostDefaultEntity();
        entity.setTipoServico(domain.getTipoServico());
        entity.setCustoDefault(domain.getCustoDefault());
        return entity;
    }
}
