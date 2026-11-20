package br.com.docflow.application.service;

import br.com.docflow.domain.model.ChecklistItem;
import br.com.docflow.domain.model.Contract;
import br.com.docflow.domain.model.Event;
import br.com.docflow.domain.model.Task;
import br.com.docflow.domain.model.TaskAutomation;
import br.com.docflow.domain.repository.EventRepository;
import br.com.docflow.domain.repository.TaskRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cria as tarefas de um evento quando o contrato é assinado, conforme o tipo de serviço, com prazos
 * relativos à data do evento (pintura e aquarela) ou à data da assinatura (identidade visual e
 * encomenda). É idempotente: uma tarefa já criada para o mesmo contrato e chave não é recriada.
 */
@SuppressWarnings("null")
@Service
@RequiredArgsConstructor
public class ContractSignedTaskAutomationService {

    private static final String SOURCE = "CONTRACT_SIGNED";
    private static final String DESCRIPTION = "Tarefa criada automaticamente após assinatura do contrato.";
    private static final LocalTime DEFAULT_START = LocalTime.of(9, 0);
    private static final LocalTime DEFAULT_END = LocalTime.of(18, 0);
    private static final String MOLDURA_CHECKLIST_ITEM = "Moldura e Caixa?";
    private static final Pattern ESTIMATED_DEADLINE_PATTERN = Pattern.compile("(\\d+)\\s*(dias?\\s*uteis|dias?|meses?|m[eê]s)");

    private final TaskRepository taskRepository;
    private final EventRepository eventRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Transactional
    public void createTasksForSignedContract(Contract contract) {
        if (contract == null || contract.getId() == null || contract.getEvent() == null || contract.getEvent().getId() == null) {
            return;
        }

        Event event = eventRepository.findById(contract.getEvent().getId()).orElse(null);
        if (event == null || event.getTipoServico() == null) {
            return;
        }

        LocalDate signedDate = LocalDate.now(clock);
        switch (event.getTipoServico()) {
            case PINTURA_AO_VIVO -> createPaintingTasks(contract.getId(), event);
            case AQUARELA_AO_VIVO -> createWatercolorTasks(contract.getId(), event);
            case IDENTIDADE_VISUAL -> createIdentityTasks(contract.getId(), event, signedDate);
            case ENCOMENDA_ARTISTICA -> createCommissionTasks(contract.getId(), event, signedDate);
        }
    }

    /** Cria retoque, emoldurar e envio da pintura, disparados ao concluir "Moldura e Caixa". */
    @Transactional
    public void createPaintingConditionalTasks(Task materialTask) {
        if (materialTask == null
                || !materialTask.isPaintingMaterialsAutomation()
                || materialTask.getAutomation().contractId() == null
                || materialTask.getEventId() == null) {
            return;
        }

        Event event = eventRepository.findById(materialTask.getEventId()).orElse(null);
        if (event == null) {
            return;
        }

        Long contractId = materialTask.getAutomation().contractId();
        createTask(contractId, "PINTURA_RETOQUE", event, "Retoque Pintura", eventDate(event).plusDays(2), null, List.of());
        createTask(contractId, "PINTURA_EMOLDURAR_PREPARAR", event, "Emoldurar e Preparar Encomenda", eventDate(event).plusDays(5), null, List.of());
        createTask(contractId, "PINTURA_ENVIAR", event, "Enviar Pintura", eventDate(event).plusDays(10), null, List.of());
    }

    private void createPaintingTasks(Long contractId, Event event) {
        createTask(contractId, "PINTURA_REUNIAO_NOIVOS", event, "Reunião com os noivos", eventDate(event).minusDays(30), null, List.of());
        createTask(contractId, "PINTURA_HOSPEDAGEM", event, "Organizar hospedagem", eventDate(event).minusDays(15), null, List.of());
        createTask(contractId, Task.PAINTING_MATERIALS_KEY, event, "Compra de material", eventDate(event).minusDays(15), null, List.of("Tela", "Tintas", MOLDURA_CHECKLIST_ITEM));
        createTask(contractId, "PINTURA_INFRAESTRUTURA", event, "Confirmar infraestrutura com cliente/cerimonialista", eventDate(event).minusDays(7), null, List.of());
    }

    private void createWatercolorTasks(Long contractId, Event event) {
        createTask(contractId, "AQUARELA_REUNIAO_NOIVOS", event, "Reunião com os noivos", eventDate(event).minusDays(30), null, List.of());
        createTask(contractId, "AQUARELA_HOSPEDAGEM", event, "Organizar hospedagem", eventDate(event).minusDays(15), null, List.of());
        createTask(contractId, "AQUARELA_COMPRA_MATERIAL", event, "Compra de material", eventDate(event).minusDays(15), null, List.of("Papel", "Impressão", "Molduras"));
        createTask(contractId, "AQUARELA_INFRAESTRUTURA", event, "Confirmar infraestrutura com cliente/cerimonialista", eventDate(event).minusDays(7), null, List.of());
    }

    private void createIdentityTasks(Long contractId, Event event, LocalDate signedDate) {
        LocalDate deliveryDate = resolveIdentityDeliveryDate(event, signedDate);
        updateEventDate(event, deliveryDate);

        createTask(contractId, "IDENTIDADE_BRIEFING", event, "Mandar Briefing", signedDate.plusDays(1), null, List.of());
        createTask(contractId, "IDENTIDADE_PRIMEIRA_REUNIAO", event, "Fazer primeira reunião", signedDate.plusDays(15), null, List.of());
        createTask(contractId, "IDENTIDADE_PESQUISA_VISUAL", event, "Iniciar pesquisa visual e direção criativa", signedDate.plusDays(17), null, List.of());
        createTask(contractId, "IDENTIDADE_PRIMEIRA_APRESENTACAO", event, "Preparar primeira apresentação", signedDate.plusDays(22), null, List.of());
        createTask(contractId, "IDENTIDADE_PRIMEIRA_ENTREGA", event, "Primeira Entrega", deliveryDate, null, List.of());
    }

    private void createCommissionTasks(Long contractId, Event event, LocalDate signedDate) {
        LocalDate deliveryDate = resolveCommissionDeliveryDate(event, signedDate);
        updateEventDate(event, deliveryDate);

        createTask(contractId, "ENCOMENDA_PROJETO", event, "Fazer projeto", signedDate.plusDays(2), null, List.of());
        createTask(contractId, "ENCOMENDA_COMPRA_MATERIAL", event, "Compra de material", signedDate.plusDays(7), null, List.of("Tela", "Tecido", "Papel", "Tinta", "Linha", "Bastidor", "Moldura", "Alça"));
        createTask(contractId, "ENCOMENDA_PRODUCAO", event, "Produção", signedDate.plusDays(10), signedDate.plusDays(15), List.of());
        createTask(contractId, "ENCOMENDA_ENTREGA_ENVIO", event, "Entrega/Envio", deliveryDate, null, List.of());
    }

    private void updateEventDate(Event event, LocalDate date) {
        if (date == null || date.equals(event.getData())) {
            return;
        }

        event.setData(date);
        eventRepository.save(event);
    }

    private void createTask(Long contractId, String key, Event event, String title, LocalDate startDate, LocalDate endDate, List<String> checklist) {
        if (taskRepository.existsByAutomation(contractId, key)) {
            return;
        }

        Task task = new Task();
        task.setEventId(event.getId());
        task.setTitulo(title);
        task.setDescricao(DESCRIPTION);
        task.setPrazoData(startDate);
        task.setPrazoDataFim(endDate != null && !endDate.isEqual(startDate) ? endDate : null);
        task.setPrazoHorario(DEFAULT_START);
        task.setPrazoHorarioFim(DEFAULT_END);
        task.setAllDay(false);
        task.setTipoServico(event.getTipoServico().name());
        task.setAutomation(new TaskAutomation(SOURCE, contractId, key));

        List<ChecklistItem> items = new ArrayList<>();
        for (int i = 0; i < checklist.size(); i++) {
            items.add(new ChecklistItem(null, checklist.get(i), false, i));
        }
        task.setChecklist(items);

        taskRepository.save(task);
    }

    private LocalDate eventDate(Event event) {
        return event.getData() != null ? event.getData() : LocalDate.now(clock).plusDays(1);
    }

    private LocalDate resolveIdentityDeliveryDate(Event event, LocalDate signedDate) {
        return parseIntegerServiceData(event, "prazoDiasUteis")
                .filter(days -> days > 0)
                .map(days -> addBusinessDays(signedDate, days))
                .orElse(signedDate.plusDays(90));
    }

    private LocalDate resolveCommissionDeliveryDate(Event event, LocalDate signedDate) {
        return parseTextServiceData(event, "prazoEstimado")
                .flatMap(value -> parseEstimatedDeadline(value, signedDate))
                .map(date -> date.minusDays(3))
                .orElse(signedDate.plusDays(21));
    }

    private Optional<LocalDate> parseEstimatedDeadline(String value, LocalDate baseDate) {
        String normalized = normalize(value);
        Matcher matcher = ESTIMATED_DEADLINE_PATTERN.matcher(normalized);
        if (!matcher.find()) {
            return Optional.empty();
        }

        int amount = Integer.parseInt(matcher.group(1));
        String unit = matcher.group(2);
        if (unit.contains("uteis")) {
            return Optional.of(addBusinessDays(baseDate, amount));
        }
        if (unit.startsWith("mes")) {
            return Optional.of(baseDate.plusMonths(amount));
        }
        return Optional.of(baseDate.plusDays(amount));
    }

    private LocalDate addBusinessDays(LocalDate baseDate, int days) {
        LocalDate date = baseDate;
        int added = 0;
        while (added < days) {
            date = date.plusDays(1);
            if (date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                added++;
            }
        }
        return date;
    }

    private Optional<Integer> parseIntegerServiceData(Event event, String key) {
        return parseTextServiceData(event, key)
                .flatMap(value -> {
                    Matcher matcher = Pattern.compile("(\\d+)").matcher(value);
                    return matcher.find() ? Optional.of(Integer.parseInt(matcher.group(1))) : Optional.empty();
                });
    }

    private Optional<String> parseTextServiceData(Event event, String key) {
        if (event.getDadosServico() == null || event.getDadosServico().isBlank()) {
            return Optional.empty();
        }

        try {
            JsonNode node = objectMapper.readTree(event.getDadosServico()).get(key);
            if (node == null || node.isNull()) {
                return Optional.empty();
            }
            String value = node.asString();
            return value == null || value.isBlank() ? Optional.empty() : Optional.of(value);
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private String normalize(String value) {
        String text = value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }
}
