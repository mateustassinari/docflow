package br.com.docflow.domain.repository;

import br.com.docflow.domain.model.Event;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EventRepository {
    Event save(Event event);
    Optional<Event> findById(Long id);
    List<Event> findAll();
    List<Event> findAllById(Collection<Long> ids);
}
