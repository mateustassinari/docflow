package br.com.docflow.testsupport;

import br.com.docflow.domain.model.Event;
import br.com.docflow.domain.repository.EventRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/** EventRepository em memória para testes de aplicação. Conta os saves para verificar efeitos. */
public class InMemoryEventRepository implements EventRepository {

    private final Map<Long, Event> events = new LinkedHashMap<>();
    private final AtomicLong ids = new AtomicLong(1);
    private int saves;

    @Override
    public Event save(Event event) {
        if (event.getId() == null) {
            event.setId(ids.getAndIncrement());
        }
        events.put(event.getId(), event);
        saves++;
        return event;
    }

    /** Cadastra um evento sem contar como save do código testado. */
    public Event given(Event event) {
        Event saved = save(event);
        saves--;
        return saved;
    }

    @Override
    public Optional<Event> findById(Long id) {
        return Optional.ofNullable(events.get(id));
    }

    @Override
    public List<Event> findAll() {
        return new ArrayList<>(events.values());
    }

    @Override
    public List<Event> findAllById(Collection<Long> ids) {
        return ids.stream().map(events::get).filter(java.util.Objects::nonNull).toList();
    }

    public int saveCount() {
        return saves;
    }
}
