package br.com.docflow.infrastructure.persistence.repository;

import br.com.docflow.domain.model.Event;
import br.com.docflow.domain.repository.EventRepository;
import br.com.docflow.infrastructure.persistence.entity.EventEntity;
import br.com.docflow.infrastructure.persistence.mapper.EventMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("null")
@Component
@RequiredArgsConstructor
public class EventRepositoryImpl implements EventRepository {

    private final SpringDataEventRepository springDataEventRepository;
    private final EventMapper eventMapper;

    @Override
    public Event save(Event event) {
        EventEntity entity = eventMapper.toEntity(event);
        EventEntity savedEntity = springDataEventRepository.save(entity);
        return eventMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Event> findById(Long id) {
        return springDataEventRepository.findById(id).map(eventMapper::toDomain);
    }

    @Override
    public List<Event> findAll() {
        return springDataEventRepository.findAll().stream()
                .map(eventMapper::toDomain)
                .toList();
    }

    @Override
    public List<Event> findAllById(Collection<Long> ids) {
        return springDataEventRepository.findAllById(ids).stream()
                .map(eventMapper::toDomain)
                .toList();
    }
}
