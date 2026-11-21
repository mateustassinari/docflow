package br.com.docflow.application.service;

import br.com.docflow.domain.model.Contract;
import br.com.docflow.domain.model.ContractStatus;
import br.com.docflow.domain.model.Event;
import br.com.docflow.domain.model.TipoServico;
import br.com.docflow.domain.repository.ContractRepository;
import br.com.docflow.domain.repository.EventRepository;
import br.com.docflow.application.readmodel.TaskView;
import br.com.docflow.domain.model.TaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.TreeMap;

/**
 * Monta um resumo textual compacto dos dados do sistema (financeiro, margem, eventos,
 * contratos, cobrança, histórico e tarefas) para enviar como contexto ao assistente de IA.
 * Somente leitura.
 */
@Slf4j
@SuppressWarnings("null")
@Service
@RequiredArgsConstructor
public class AiContextBuilder {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String CONTRATO_ASSINADO = "assinado";
    private static final String CONTRATO_AGUARDANDO = "aguardando assinatura da cliente";
    private static final String CONTRATO_GERADO = "gerado, aguardando envio";
    private static final String SEM_CONTRATO = "sem contrato";
    private static final String CONTRATO_DISPENSADO = "dispensado";
    private static final DateTimeFormatter WEEKDAY = DateTimeFormatter.ofPattern("EEEE", Locale.of("pt", "BR"));

    private final EventRepository eventRepository;
    private final ContractRepository contractRepository;
    private final TaskService taskService;
    private final Clock clock;

    @Transactional(readOnly = true)
    public String build() {
        List<Event> events = eventRepository.findAll();
        LocalDate today = LocalDate.now(clock);
        Map<Long, ContractStatus> contracts = contractStatusByEvent();
        TaskBuckets tasks = taskBuckets(today);
        StringBuilder sb = new StringBuilder();

        sb.append("Data de hoje: ").append(today.format(WEEKDAY)).append(", ").append(today.format(DATE)).append("\n\n");

        // O modelo pequeno erra quando precisa contar, comparar datas ou cruzar seções.
        // Por isso as respostas mais pedidas já vão calculadas: ele só precisa ler e redigir.
        appendSummary(sb, events, contracts, tasks, today);
        appendFinancials(sb, events, today);
        appendServiceMix(sb, events);
        appendMarginByService(sb, events);
        appendUpcomingEvents(sb, events, contracts, today);
        appendContractsAndBilling(sb, events, contracts, today);
        appendHistory(sb, events, today);
        appendTasks(sb, tasks, today);

        String context = sb.toString();
        // Estimativa grosseira (~4 caracteres por token) para conferir que cabe no num_ctx do modelo.
        log.debug("Contexto da IA: {} caracteres (~{} tokens)", context.length(), context.length() / 4);
        return context;
    }

    /**
     * Para perguntas sobre prazos de tarefas (atrasadas, de hoje, da semana, dos próximos 7 dias),
     * devolve só o grupo que responde à pergunta, já filtrado. Com o contexto inteiro, o modelo
     * pequeno puxava tarefas do grupo vizinho; com só o grupo certo, ele apenas redige a lista.
     * Vazio quando a pergunta não é desse tipo, e o assistente usa o contexto completo.
     */
    @Transactional(readOnly = true)
    public Optional<String> taskDeadlineFocus(String question) {
        String q = normalize(question);
        if (q.matches(".*(pagament|receb|cobran|valor|dinheiro|financ).*")) {
            return Optional.empty();
        }
        LocalDate today = LocalDate.now(clock);
        TaskBuckets tasks = taskBuckets(today);
        StringBuilder sb = new StringBuilder("Data de hoje: ").append(today.format(WEEKDAY)).append(", ").append(today.format(DATE)).append("\n");
        if (q.contains("atrasad")) {
            appendTaskGroup(sb, "TAREFAS ATRASADAS (prazo antes de hoje)", tasks.atrasadas());
        } else if (q.contains("semana")) {
            appendTaskGroup(sb, "TAREFAS QUE VENCEM ESTA SEMANA (de hoje até domingo, " + tasks.domingo().format(DATE) + ")",
                    Stream.concat(tasks.hoje().stream(), tasks.restoDaSemana().stream()).toList());
        } else if (q.matches(".*(7 dias|sete dias|proximos dias).*")) {
            appendTaskGroup(sb, "TAREFAS QUE VENCEM NOS PRÓXIMOS 7 DIAS (depois de hoje até " + tasks.fim7Dias().format(DATE) + ")", tasks.proximos7Dias());
        } else if (q.contains("hoje") && q.matches(".*(tarefa|vence|prazo|fazer).*")) {
            appendTaskGroup(sb, "TAREFAS PARA HOJE", tasks.hoje());
        } else {
            return Optional.empty();
        }
        return Optional.of(sb.toString());
    }

    private static String normalize(String text) {
        String semAcento = Normalizer.normalize(text == null ? "" : text, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(Locale.ROOT);
    }

    private void appendSummary(StringBuilder sb, List<Event> events, Map<Long, ContractStatus> contracts, TaskBuckets tasks, LocalDate today) {
        List<Event> futuros = events.stream()
                .filter(e -> isFuture(e, today))
                .sorted(Comparator.comparing(Event::getData))
                .toList();
        BigDecimal aReceber = futuros.stream().map(this::pendenteDoEvento).filter(v -> v.signum() > 0).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal emAtraso = events.stream().filter(e -> isOverdue(e, today))
                .map(this::pendenteDoEvento).filter(v -> v.signum() > 0).reduce(BigDecimal.ZERO, BigDecimal::add);

        sb.append("== RESUMO (números já calculados pelo sistema: use-os como estão, sem recontar) ==\n");
        sb.append("Eventos futuros: ").append(futuros.size());
        if (!futuros.isEmpty()) {
            Event proximo = futuros.getFirst();
            sb.append(" | próximo: ").append(clientName(proximo)).append(" em ").append(proximo.getData().format(DATE))
                    .append(" (").append(label(proximo.getTipoServico())).append(")");
        }
        sb.append("\n");
        sb.append("A receber de eventos futuros: ").append(money(aReceber))
                .append(" | em atraso (eventos já realizados e não quitados): ").append(money(emAtraso)).append("\n");
        sb.append("Contratos dos eventos futuros:");
        for (String status : List.of(CONTRATO_ASSINADO, CONTRATO_AGUARDANDO, CONTRATO_GERADO, SEM_CONTRATO, CONTRATO_DISPENSADO)) {
            long n = futuros.stream().filter(e -> contractLabel(e, contracts).equals(status)).count();
            sb.append(" ").append(status).append(": ").append(n).append(";");
        }
        sb.append("\n");
        sb.append("Tarefas abertas: atrasadas ").append(tasks.atrasadas().size())
                .append(" | para hoje ").append(tasks.hoje().size())
                .append(" | no resto desta semana ").append(tasks.restoDaSemana().size())
                .append(" | nos próximos 7 dias ").append(tasks.proximos7Dias().size()).append("\n\n");
    }

    private void appendFinancials(StringBuilder sb, List<Event> events, LocalDate today) {
        YearMonth currentMonth = YearMonth.from(today);
        BigDecimal contratado = BigDecimal.ZERO;
        BigDecimal contratadoFuturo = BigDecimal.ZERO;
        BigDecimal recebido = BigDecimal.ZERO;
        BigDecimal recebidoNoMes = BigDecimal.ZERO;
        BigDecimal emAtraso = BigDecimal.ZERO;
        BigDecimal aReceberFuturo = BigDecimal.ZERO;
        int recebimentosSemData = 0;

        for (Event e : events) {
            contratado = contratado.add(nz(e.getValor()));
            if (isFuture(e, today)) {
                contratadoFuturo = contratadoFuturo.add(nz(e.getValor()));
            }
            recebido = recebido.add(recebidoDoEvento(e));

            // "Recebido no mês" usa a data em que o dinheiro entrou, não a data do evento.
            if (e.isSinalRecebido()) {
                if (e.getDataRecebimentoSinal() == null) {
                    recebimentosSemData++;
                } else if (YearMonth.from(e.getDataRecebimentoSinal()).equals(currentMonth)) {
                    recebidoNoMes = recebidoNoMes.add(nz(e.getValorSinal()));
                }
            }
            if (e.isRestanteRecebido()) {
                if (e.getDataRecebimentoRestante() == null) {
                    recebimentosSemData++;
                } else if (YearMonth.from(e.getDataRecebimentoRestante()).equals(currentMonth)) {
                    recebidoNoMes = recebidoNoMes.add(nz(e.getValor()).subtract(nz(e.getValorSinal())));
                }
            }

            BigDecimal pendente = pendenteDoEvento(e);
            if (pendente.signum() > 0) {
                if (isOverdue(e, today)) {
                    emAtraso = emAtraso.add(pendente);
                } else {
                    aReceberFuturo = aReceberFuturo.add(pendente);
                }
            }
        }

        sb.append("== FINANCEIRO ==\n");
        sb.append("Total de eventos cadastrados: ").append(events.size()).append("\n");
        sb.append("Valor contratado (soma de todos os eventos, passados e futuros): ").append(money(contratado)).append("\n");
        sb.append("Valor contratado de eventos futuros: ").append(money(contratadoFuturo)).append("\n");
        sb.append("Recebido no mês atual (pela data de recebimento): ").append(money(recebidoNoMes)).append("\n");
        if (recebimentosSemData > 0) {
            sb.append("Recebimentos sem data informada (fora do cálculo do mês): ").append(recebimentosSemData).append("\n");
        }
        sb.append("Total já recebido: ").append(money(recebido)).append("\n");
        sb.append("Em atraso (evento já realizado e não quitado): ").append(money(emAtraso)).append("\n");
        sb.append("A receber de eventos futuros (ainda não vencido): ").append(money(aReceberFuturo)).append("\n\n");
    }

    private void appendServiceMix(StringBuilder sb, List<Event> events) {
        Map<TipoServico, Integer> mix = new EnumMap<>(TipoServico.class);
        for (Event e : events) {
            if (e.getTipoServico() != null) {
                mix.merge(e.getTipoServico(), 1, Integer::sum);
            }
        }
        sb.append("== SERVIÇOS (quantidade por tipo) ==\n");
        if (mix.isEmpty()) {
            sb.append("Nenhum serviço registrado.\n");
        } else {
            mix.forEach((tipo, qtd) -> sb.append("- ").append(label(tipo)).append(": ").append(qtd).append("\n"));
        }
        sb.append("\n");
    }

    private void appendMarginByService(StringBuilder sb, List<Event> events) {
        Map<TipoServico, BigDecimal[]> agg = new EnumMap<>(TipoServico.class); // [receita, custo]
        int semCusto = 0;
        for (Event e : events) {
            if (e.getTipoServico() == null) {
                continue;
            }
            // Evento sem custo informado entraria como margem de 100% e distorceria a média.
            if (!hasCostInformed(e)) {
                semCusto++;
                continue;
            }
            BigDecimal[] acc = agg.computeIfAbsent(e.getTipoServico(), k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            acc[0] = acc[0].add(nz(e.getValor()));
            acc[1] = acc[1].add(custoDoEvento(e));
        }
        sb.append("== MARGEM POR SERVIÇO (só eventos com custo informado) ==\n");
        if (semCusto > 0) {
            sb.append("Eventos sem custo informado (fora da margem): ").append(semCusto).append("\n");
        }
        if (agg.isEmpty()) {
            sb.append("Sem dados.\n\n");
            return;
        }
        agg.forEach((tipo, acc) -> sb.append("- ").append(label(tipo))
                .append(": receita ").append(money(acc[0]))
                .append(" | custo ").append(money(acc[1]))
                .append(" | margem ").append(money(acc[0].subtract(acc[1])))
                .append("\n"));
        sb.append("\n");
    }

    private void appendUpcomingEvents(StringBuilder sb, List<Event> events, Map<Long, ContractStatus> contracts, LocalDate today) {
        List<Event> proximos = events.stream()
                .filter(e -> e.getData() != null && !e.getData().isBefore(today))
                .sorted(Comparator.comparing(Event::getData))
                .limit(10)
                .toList();

        sb.append("== PRÓXIMOS EVENTOS ==\n");
        if (proximos.isEmpty()) {
            sb.append("Nenhum evento futuro agendado.\n\n");
            return;
        }
        for (Event e : proximos) {
            sb.append("- ").append(e.getData().format(DATE));
            if (e.getHorarioInicio() != null) {
                sb.append(" ").append(e.getHorarioInicio());
            }
            sb.append(" | ").append(clientName(e))
                    .append(" | ").append(orDash(e.getTipoEvento()))
                    .append(" | ").append(label(e.getTipoServico()))
                    .append(" | ").append(money(nz(e.getValor())))
                    .append(" | pagamento: ").append(paymentStatus(e))
                    .append(" | a receber: ").append(money(pendenteDoEvento(e).max(BigDecimal.ZERO)))
                    .append(" | contrato: ").append(contractLabel(e, contracts));
            appendIf(sb, "local", e.getLocal());
            if (e.getNumeroConvidados() != null) {
                sb.append(" | convidados: ").append(e.getNumeroConvidados());
            }
            appendIf(sb, "técnica", e.getTecnica());
            appendIf(sb, "tela", e.getTamanhoTela());
            appendIf(sb, "obs", e.getObservacoes());
            sb.append("\n");
        }
        sb.append("\n");
    }

    private Map<Long, ContractStatus> contractStatusByEvent() {
        Map<Long, ContractStatus> byEvent = new HashMap<>();
        for (Contract c : contractRepository.findAll()) {
            if (c.getEvent() != null && c.getEvent().getId() != null) {
                byEvent.put(c.getEvent().getId(), c.getStatus());
            }
        }
        return byEvent;
    }

    private String contractLabel(Event e, Map<Long, ContractStatus> contracts) {
        ContractStatus st = contracts.get(e.getId());
        if (st == ContractStatus.ASSINADO) {
            return CONTRATO_ASSINADO;
        }
        if (st == ContractStatus.ENVIADO) {
            return CONTRATO_AGUARDANDO;
        }
        if (st == ContractStatus.RASCUNHO) {
            return CONTRATO_GERADO;
        }
        return e.isContratoDispensado() ? CONTRATO_DISPENSADO : SEM_CONTRATO;
    }

    private void appendContractsAndBilling(StringBuilder sb, List<Event> events, Map<Long, ContractStatus> byEvent, LocalDate today) {

        int gerados = 0;
        int aguardando = 0;
        int assinados = 0;
        int semContrato = 0;
        int dispensados = 0;
        for (Event e : events) {
            ContractStatus st = byEvent.get(e.getId());
            if (st == ContractStatus.RASCUNHO) {
                gerados++;
            } else if (st == ContractStatus.ENVIADO) {
                aguardando++;
            } else if (st == ContractStatus.ASSINADO) {
                assinados++;
            } else if (e.isContratoDispensado()) {
                dispensados++;
            } else {
                semContrato++;
            }
        }

        sb.append("== CONTRATOS ==\n");
        sb.append("Gerados aguardando envio: ").append(gerados)
                .append(" | aguardando assinatura: ").append(aguardando)
                .append(" | assinados: ").append(assinados)
                .append(" | sem contrato: ").append(semContrato)
                .append(" | dispensados: ").append(dispensados).append("\n");
        // Pendências acionáveis (eventos futuros que precisam de ação)
        List<Event> pendGerados = eventsByStatus(events, byEvent, ContractStatus.RASCUNHO, today);
        List<Event> pendAssinatura = eventsByStatus(events, byEvent, ContractStatus.ENVIADO, today);
        List<Event> pendSemContrato = events.stream()
                .filter(e -> e.getData() != null && !e.getData().isBefore(today))
                .filter(e -> !byEvent.containsKey(e.getId()) && !e.isContratoDispensado())
                .sorted(Comparator.comparing(Event::getData))
                .limit(10)
                .toList();
        appendEventGroup(sb, "Aguardando envio para assinatura", pendGerados);
        appendEventGroup(sb, "Aguardando assinatura da cliente", pendAssinatura);
        appendEventGroup(sb, "Eventos futuros sem contrato", pendSemContrato);
        sb.append("\n");

        // Cobrança: separa dinheiro em atraso (evento já realizado) do que ainda vai vencer.
        List<Event> pendentes = events.stream()
                .filter(e -> pendenteDoEvento(e).signum() > 0)
                .sorted(Comparator.comparing(e -> e.getData() == null ? LocalDate.MAX : e.getData()))
                .toList();
        List<Event> emAtraso = pendentes.stream().filter(e -> isOverdue(e, today)).limit(15).toList();
        List<Event> aVencer = pendentes.stream().filter(e -> !isOverdue(e, today)).limit(10).toList();

        sb.append("== COBRANÇA ==\n");
        if (pendentes.isEmpty()) {
            sb.append("Nenhum pagamento pendente.\n\n");
            return;
        }
        appendBillingGroup(sb, "EM ATRASO (evento já realizado e não quitado)", emAtraso, "Nenhum pagamento em atraso.");
        appendBillingGroup(sb, "A VENCER (eventos futuros)", aVencer, "Nenhum valor a receber de eventos futuros.");
        sb.append("\n");
    }

    private void appendBillingGroup(StringBuilder sb, String titulo, List<Event> events, String vazio) {
        sb.append(titulo).append(":\n");
        if (events.isEmpty()) {
            sb.append("  ").append(vazio).append("\n");
            return;
        }
        for (Event e : events) {
            sb.append("  - ").append(clientName(e))
                    .append(" | evento ").append(e.getData() != null ? e.getData().format(DATE) : "sem data")
                    .append(" | pendente ").append(money(pendenteDoEvento(e)))
                    .append(" (").append(paymentStatus(e)).append(")\n");
        }
    }

    private void appendHistory(StringBuilder sb, List<Event> events, LocalDate today) {
        long passados = events.stream().filter(e -> e.getData() != null && e.getData().isBefore(today)).count();
        long futuros = events.stream().filter(e -> e.getData() != null && !e.getData().isBefore(today)).count();

        YearMonth currentMonth = YearMonth.from(today);
        Map<YearMonth, BigDecimal> porMes = new TreeMap<>();
        for (Event e : events) {
            // Meses futuros ficam de fora: são agenda, não histórico.
            if (e.getData() != null && !YearMonth.from(e.getData()).isAfter(currentMonth)) {
                porMes.merge(YearMonth.from(e.getData()), nz(e.getValor()), BigDecimal::add);
            }
        }

        sb.append("== HISTÓRICO (valor contratado por mês do evento, até o mês atual) ==\n");
        sb.append("Eventos passados: ").append(passados).append(" | futuros: ").append(futuros).append("\n");
        // últimos 12 meses com movimento
        List<YearMonth> meses = porMes.keySet().stream().sorted(Comparator.reverseOrder()).limit(12).sorted().toList();
        for (YearMonth ym : meses) {
            sb.append("- ").append(ym).append(": ").append(money(porMes.get(ym))).append("\n");
        }
        sb.append("\n");
    }

    /** Tarefas abertas separadas por prazo, já com as datas comparadas pelo sistema. */
    private record TaskBuckets(int total, int concluidas, List<TaskView> atrasadas, List<TaskView> hoje,
                               List<TaskView> restoDaSemana, List<TaskView> proximos7Dias, List<TaskView> depois,
                               LocalDate domingo, LocalDate fim7Dias) {
    }

    private TaskBuckets taskBuckets(LocalDate today) {
        List<TaskView> all = taskService.list(null, null, null, null, null, null);
        List<TaskView> open = all.stream()
                .filter(t -> t.task().getStatus() != TaskStatus.CONCLUIDA)
                .sorted(Comparator.comparing(this::deadline))
                .toList();
        LocalDate domingo = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        LocalDate fim7Dias = today.plusDays(7);
        return new TaskBuckets(
                all.size(),
                all.size() - open.size(),
                open.stream().filter(t -> deadline(t).isBefore(today)).toList(),
                open.stream().filter(t -> deadline(t).isEqual(today)).toList(),
                open.stream().filter(t -> deadline(t).isAfter(today) && !deadline(t).isAfter(domingo)).toList(),
                open.stream().filter(t -> deadline(t).isAfter(today) && !deadline(t).isAfter(fim7Dias)).toList(),
                open.stream().filter(t -> deadline(t).isAfter(fim7Dias)).limit(15).toList(),
                domingo, fim7Dias);
    }

    private void appendTasks(StringBuilder sb, TaskBuckets tasks, LocalDate today) {
        int abertas = tasks.total() - tasks.concluidas();
        sb.append("== TAREFAS (cada grupo já filtrado pelo prazo; não mova tarefas de um grupo para outro) ==\n");
        sb.append("Total: ").append(tasks.total())
                .append(" | abertas: ").append(abertas)
                .append(" | concluídas: ").append(tasks.concluidas()).append("\n");

        appendTaskGroup(sb, "ATRASADAS (prazo antes de hoje)", tasks.atrasadas());
        appendTaskGroup(sb, "PARA HOJE (" + today.format(DATE) + ")", tasks.hoje());
        appendTaskGroup(sb, "NO RESTO DESTA SEMANA (depois de hoje até domingo, " + tasks.domingo().format(DATE) + ")", tasks.restoDaSemana());
        appendTaskGroup(sb, "NOS PRÓXIMOS 7 DIAS (depois de hoje até " + tasks.fim7Dias().format(DATE) + ")", tasks.proximos7Dias());
        appendTaskGroup(sb, "DEPOIS DE " + tasks.fim7Dias().format(DATE), tasks.depois());
    }

    private void appendTaskGroup(StringBuilder sb, String titulo, List<TaskView> tasks) {
        sb.append(titulo).append(":\n");
        if (tasks.isEmpty()) {
            sb.append("  nenhuma\n");
            return;
        }
        for (TaskView view : tasks) {
            var t = view.task();
            sb.append("  - ").append(t.getTitulo() == null || t.getTitulo().isBlank() ? "(sem título)" : t.getTitulo())
                    .append(" — prazo ").append(deadline(view).format(DATE));
            if (t.getPrazoHorario() != null) {
                sb.append(" ").append(t.getPrazoHorario());
            }
            if (view.event() != null && view.event().clienteNome() != null) {
                sb.append(" | cliente: ").append(view.event().clienteNome());
            }
            if (t.getEtiquetas() != null && !t.getEtiquetas().isEmpty()) {
                sb.append(" | etiquetas: ").append(String.join(", ", t.getEtiquetas()));
            }
            sb.append(" [").append(t.getStatus()).append("]\n");
        }
    }

    // ----------------- helpers -----------------

    private List<Event> eventsByStatus(List<Event> events, Map<Long, ContractStatus> byEvent, ContractStatus status, LocalDate today) {
        return events.stream()
                .filter(e -> byEvent.get(e.getId()) == status)
                .sorted(Comparator.comparing(e -> e.getData() == null ? LocalDate.MAX : e.getData()))
                .limit(10)
                .toList();
    }

    private void appendEventGroup(StringBuilder sb, String titulo, List<Event> events) {
        if (events.isEmpty()) {
            return;
        }
        sb.append(titulo).append(":\n");
        for (Event e : events) {
            sb.append("  - ").append(clientName(e))
                    .append(" | ").append(e.getData() != null ? e.getData().format(DATE) : "sem data")
                    .append(" | ").append(orDash(e.getTipoEvento()))
                    .append("\n");
        }
    }

    private void appendIf(StringBuilder sb, String rotulo, String valor) {
        if (valor != null && !valor.isBlank()) {
            sb.append(" | ").append(rotulo).append(": ").append(valor.trim());
        }
    }

    private LocalDate deadline(TaskView view) {
        var t = view.task();
        if (t.getPrazoDataFim() != null) {
            return t.getPrazoDataFim();
        }
        if (t.getPrazoData() != null) {
            return t.getPrazoData();
        }
        return LocalDate.MAX;
    }

    private BigDecimal recebidoDoEvento(Event e) {
        BigDecimal valor = nz(e.getValor());
        BigDecimal sinal = nz(e.getValorSinal());
        BigDecimal recebido = BigDecimal.ZERO;
        if (e.isSinalRecebido()) {
            recebido = recebido.add(sinal);
        }
        if (e.isRestanteRecebido()) {
            recebido = recebido.add(valor.subtract(sinal));
        }
        return recebido;
    }

    private BigDecimal pendenteDoEvento(Event e) {
        return nz(e.getValor()).subtract(recebidoDoEvento(e));
    }

    private boolean isOverdue(Event e, LocalDate today) {
        return e.getData() != null && e.getData().isBefore(today);
    }

    private boolean isFuture(Event e, LocalDate today) {
        return e.getData() != null && !e.getData().isBefore(today);
    }

    private boolean hasCostInformed(Event e) {
        return e.getCustoTotal() != null || e.getCustosExtras() != null;
    }

    private BigDecimal custoDoEvento(Event e) {
        return nz(e.getCustoTotal()).add(nz(e.getCustosExtras()));
    }

    private String paymentStatus(Event e) {
        if (e.isRestanteRecebido()) {
            return "quitado";
        }
        if (e.isSinalRecebido()) {
            return "sinal recebido, restante pendente";
        }
        return "pendente";
    }

    private String clientName(Event e) {
        return e.getClient() != null && e.getClient().getNome() != null ? e.getClient().getNome() : "Cliente";
    }

    private static String orDash(String v) {
        return v == null || v.isBlank() ? "-" : v.trim();
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static String money(BigDecimal v) {
        return "R$ " + v.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static String label(TipoServico t) {
        if (t == null) {
            return "Serviço";
        }
        return switch (t) {
            case PINTURA_AO_VIVO -> "Pintura ao Vivo";
            case AQUARELA_AO_VIVO -> "Aquarela ao Vivo";
            case ENCOMENDA_ARTISTICA -> "Encomenda Artística";
            case IDENTIDADE_VISUAL -> "Identidade Visual";
        };
    }
}
