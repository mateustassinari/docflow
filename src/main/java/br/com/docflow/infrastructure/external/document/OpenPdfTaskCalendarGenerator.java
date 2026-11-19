package br.com.docflow.infrastructure.external.document;

import br.com.docflow.application.port.TaskCalendarDocumentGenerator;
import br.com.docflow.application.readmodel.TaskEventSummary;
import br.com.docflow.application.readmodel.TaskView;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPCellEvent;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class OpenPdfTaskCalendarGenerator implements TaskCalendarDocumentGenerator {

    private static final Locale LOCALE_BR = Locale.of("pt", "BR");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Color INK       = new Color(0x1E, 0x1E, 0x2E);
    private static final Color MUTED     = new Color(0x6B, 0x72, 0x80);
    private static final Color BORDER    = new Color(0xE5, 0xE7, 0xEB);
    private static final Color PANEL     = new Color(0xF9, 0xFA, 0xFB);
    private static final Color HEADER    = new Color(0x18, 0x18, 0x27);
    // Service palette — pastel fill + dark text for cards
    private static final Color SVC_PINTURA_FILL   = new Color(0xED, 0xE9, 0xFE); // violet-100
    private static final Color SVC_PINTURA_DARK   = new Color(0x4F, 0x46, 0xE5); // violet-600
    private static final Color SVC_AQUARELA_FILL  = new Color(0xCC, 0xFB, 0xF1); // teal-100
    private static final Color SVC_AQUARELA_DARK  = new Color(0x0F, 0x76, 0x6E); // teal-700
    private static final Color SVC_ENCOMENDA_FILL = new Color(0xFC, 0xE7, 0xF3); // pink-100
    private static final Color SVC_ENCOMENDA_DARK = new Color(0xDB, 0x27, 0x77); // pink-600
    private static final Color SVC_IDENTIDADE_FILL= new Color(0xFE, 0xF3, 0xC7); // amber-100
    private static final Color SVC_IDENTIDADE_DARK= new Color(0xD9, 0x77, 0x06); // amber-600
    private static final Color SVC_GERAL_FILL     = new Color(0xF1, 0xF5, 0xF9); // slate-100
    private static final Color SVC_GERAL_DARK     = new Color(0x47, 0x55, 0x69); // slate-600

    private static final Color STATUS_TODO     = new Color(0xEF, 0x44, 0x44);
    private static final Color STATUS_PROGRESS = new Color(0xF5, 0x9E, 0x0B);
    private static final Color STATUS_DONE     = new Color(0x10, 0xB9, 0x81);

    private static final Font TITLE        = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14,   Color.WHITE);
    private static final Font SUBTITLE     = FontFactory.getFont(FontFactory.HELVETICA,      7.2f, new Color(0xCB, 0xD5, 0xE1));
    private static final Font COUNT        = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18,   Color.WHITE);
    private static final Font DAY_NAME     = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 6.2f, MUTED);
    private static final Font DAY_NUMBER   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.4f, INK);
    private static final Font TASK_META    = FontFactory.getFont(FontFactory.HELVETICA,      5.2f, MUTED);
    private static final Font TASK_EVENT   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 6.1f, new Color(0x33, 0x41, 0x55));
    private static final Font MORE         = FontFactory.getFont(FontFactory.HELVETICA,      5.8f, MUTED);
    private static final Font LEGEND_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 6.8f, INK);
    private static final Font LEGEND       = FontFactory.getFont(FontFactory.HELVETICA,      6.2f, MUTED);
    private static final Font MUTED_DAY_NUMBER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.4f, new Color(0xC0, 0xC7, 0xD0));

    @Override
    public byte[] generate(List<TaskView> tasks, String view, LocalDate date) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4.rotate(), 12, 12, 10, 10);
            PdfWriter.getInstance(document, output);
            document.open();

            String normalizedView = normalizeView(view);
            LocalDate baseDate = date == null ? LocalDate.now() : date;
            addHeader(document, normalizedView, baseDate, tasks);
            addLegend(document);

            if ("week".equals(normalizedView)) {
                addWeek(document, tasks, baseDate);
            } else if ("day".equals(normalizedView)) {
                addDay(document, tasks, baseDate);
            } else {
                addMonth(document, tasks, baseDate);
            }

            document.close();
            return output.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Falha ao gerar PDF do calendario de tarefas.", ex);
        }
    }

    private void addHeader(Document document, String view, LocalDate date, List<TaskView> tasks) throws DocumentException {
        PdfPTable header = new PdfPTable(new float[]{3.3f, 1f});
        header.setWidthPercentage(100);
        header.setSpacingAfter(4);

        PdfPCell titleCell = new PdfPCell();
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setBackgroundColor(HEADER);
        titleCell.setPadding(6);
        Paragraph title = new Paragraph("Calendário de Tarefas", TITLE);
        title.setLeading(14);
        titleCell.addElement(title);
        titleCell.addElement(new Paragraph(buildPeriodLabel(view, date), SUBTITLE));
        header.addCell(titleCell);

        PdfPCell summaryCell = new PdfPCell();
        summaryCell.setBorder(Rectangle.NO_BORDER);
        summaryCell.setBackgroundColor(HEADER);
        summaryCell.setPadding(6);
        Paragraph summary = new Paragraph();
        summary.setAlignment(Element.ALIGN_RIGHT);
        summary.setLeading(12);
        summary.add(new Chunk(String.valueOf(tasks.size()), COUNT));
        summary.add(new Chunk("\n tarefas no período", SUBTITLE));
        summaryCell.addElement(summary);
        header.addCell(summaryCell);

        document.add(header);
    }

    private void addLegend(Document document) throws DocumentException {
        PdfPTable legend = new PdfPTable(new float[]{3.2f, 2f});
        legend.setWidthPercentage(100);
        legend.setSpacingAfter(5);
        legend.addCell(legendGroup("Serviços",
                new LegendEntry("Pintura ao Vivo", serviceColor("PINTURA_AO_VIVO")),
                new LegendEntry("Aquarela ao Vivo", serviceColor("AQUARELA_AO_VIVO")),
                new LegendEntry("Encomenda artística", serviceColor("ENCOMENDA_ARTISTICA")),
                new LegendEntry("Identidade visual", serviceColor("IDENTIDADE_VISUAL")),
                new LegendEntry("Geral", serviceColor(null))));
        legend.addCell(legendGroup("Status",
                new LegendEntry("A fazer", STATUS_TODO),
                new LegendEntry("Em andamento", STATUS_PROGRESS),
                new LegendEntry("Concluída", STATUS_DONE)));
        document.add(legend);
    }

    private PdfPCell legendGroup(String title, LegendEntry... items) {
        PdfPCell cell = new PdfPCell();
        cell.setBorderColor(BORDER);
        cell.setBackgroundColor(Color.WHITE);
        cell.setPadding(3.5f);

        PdfPTable content = new PdfPTable(items.length * 2 + 1);
        content.setWidthPercentage(100);
        float[] widths = new float[items.length * 2 + 1];
        widths[0] = 1.2f;
        for (int index = 1; index < widths.length; index += 2) {
            widths[index] = 0.18f;
            widths[index + 1] = 1f;
        }
        try {
            content.setWidths(widths);
        } catch (DocumentException ignored) {
            // Fixed widths are only cosmetic here; keep the legend available if OpenPDF rejects them.
        }
        content.addCell(textOnlyCell(title, LEGEND_TITLE));
        for (LegendEntry item : items) {
            content.addCell(dotCell(item.color()));
            content.addCell(textOnlyCell(item.label(), LEGEND));
        }
        cell.addElement(content);
        return cell;
    }

    private void addMonth(Document document, List<TaskView> tasks, LocalDate date) throws DocumentException {
        LocalDate first = date.withDayOfMonth(1);
        LocalDate start = first.minusDays(first.getDayOfWeek().getValue() - 1L);

        // A4 landscape usable height ≈ 595pt minus margins (24pt) minus header (~38pt) minus legend (~22pt)
        float cellHeight = 66f;

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 1, 1, 1, 1, 1, 1});
        table.setKeepTogether(true);
        addWeekHeader(table);

        Month month = first.getMonth();
        Map<LocalDate, List<TaskView>> byDate = tasks.stream()
                .flatMap(t -> expandTaskDates(t).stream().map(d -> Map.entry(d, t)))
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

        for (int week = 0; week < 6; week++) {
            LocalDate weekStart = start.plusDays(week * 7L);
            for (int d = 0; d < 7; d++) {
                LocalDate day = weekStart.plusDays(d);
                boolean otherMonth = day.getMonth() != month;
                List<TaskView> dayTasks = byDate.getOrDefault(day, List.of());
                table.addCell(monthDayCell(day, dayTasks, otherMonth, cellHeight));
            }
        }

        document.add(table);
    }

    private void addWeek(Document document, List<TaskView> tasks, LocalDate date) throws DocumentException {
        LocalDate start = date;
        Map<LocalDate, List<TaskView>> byDate = groupByDate(tasks);

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 1, 1, 1, 1, 1, 1});
        addWeekHeader(table);

        for (int index = 0; index < 7; index++) {
            LocalDate day = start.plusDays(index);
            addDayCell(table, day, byDate.getOrDefault(day, List.of()), false, 386, 12, true, 34);
        }

        document.add(table);
    }

    private void addDay(Document document, List<TaskView> tasks, LocalDate date) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        addDayCell(table, date, tasks, false, 430, 18, true, 90);
        document.add(table);
    }

    private void addWeekHeader(PdfPTable table) {
        for (DayOfWeek day : DayOfWeek.values()) {
            PdfPCell cell = new PdfPCell(new Phrase(day.getDisplayName(TextStyle.SHORT, LOCALE_BR).toUpperCase(LOCALE_BR), DAY_NAME));
            cell.setBorderColor(BORDER);
            cell.setBackgroundColor(PANEL);
            cell.setPadding(4);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private void addDayCell(PdfPTable table, LocalDate day, List<TaskView> tasks, boolean muted, float height, int taskLimit, boolean stackedInfo, int stackedEventLimit) {
        PdfPCell cell = new PdfPCell();
        cell.setFixedHeight(height);
        cell.setBorderColor(BORDER);
        cell.setPaddingTop(4f);
        cell.setPaddingBottom(4f);
        cell.setPaddingLeft(5f);
        cell.setPaddingRight(4f);
        cell.setBackgroundColor(Color.WHITE);

        Paragraph header = new Paragraph();
        header.setLeading(8.5f);
        header.add(new Chunk(String.valueOf(day.getDayOfMonth()), DAY_NUMBER));
        header.add(new Chunk("  " + day.getDayOfWeek().getDisplayName(TextStyle.SHORT, LOCALE_BR), DAY_NAME));
        header.setSpacingAfter(3f);
        cell.addElement(header);

        List<TaskView> sortedTasks = tasks.stream()
                .sorted(Comparator
                        .comparing((TaskView t) -> t.task().getPrazoHorario(), Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing((TaskView t) -> t.task().getTitulo(), String.CASE_INSENSITIVE_ORDER))
                .toList();

        sortedTasks.stream()
                .limit(taskLimit)
                .forEach(task -> cell.addElement(taskCard(task, stackedInfo, stackedEventLimit)));

        if (sortedTasks.size() > taskLimit) {
            Paragraph more = new Paragraph("+" + (sortedTasks.size() - taskLimit) + " tarefas", MORE);
            more.setLeading(7.2f);
            cell.addElement(more);
        }

        table.addCell(cell);
    }

    private PdfPTable taskCard(TaskView task, boolean stackedInfo, int stackedEventLimit) {
        PdfPTable card = new PdfPTable(new float[]{0.06f, 1f});
        card.setWidthPercentage(100);
        card.setSpacingAfter(stackedInfo ? 5.5f : 2.4f);

        // Left accent bar — color = status
        PdfPCell accent = new PdfPCell();
        accent.setBorder(Rectangle.NO_BORDER);
        accent.setBackgroundColor(statusColor(statusName(task)));
        accent.setPadding(0);
        card.addCell(accent);

        // Card body — pastel fill from service
        Color fill = serviceFillColor(task.task().getTipoServico());
        Color dark = serviceColor(task.task().getTipoServico());
        PdfPCell body = new PdfPCell();
        body.setBackgroundColor(fill);
        body.setBorder(Rectangle.NO_BORDER);
        body.setPaddingTop(stackedInfo ? 7f : 3.2f);
        body.setPaddingBottom(stackedInfo ? 7.2f : 3.6f);
        body.setPaddingLeft(stackedInfo ? 6f : 4f);
        body.setPaddingRight(stackedInfo ? 5f : 3f);

        Paragraph title = new Paragraph();
        title.setLeading(stackedInfo ? 9.4f : 7f);
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, stackedInfo ? 7.2f : 6f, dark);
        title.add(new Chunk(truncate(task.task().getTitulo(), stackedInfo ? stackedEventLimit : 30), titleFont));
        title.setSpacingAfter(stackedInfo ? 5.2f : 2f);
        body.addElement(title);

        body.addElement(stackedInfo ? stackedTaskInfo(task, stackedEventLimit) : taskInfoLine(task, 1.8f, 36));
        card.addCell(body);
        return card;
    }

    private PdfPTable stackedTaskInfo(TaskView task, int eventLimit) {
        PdfPTable info = new PdfPTable(1);
        info.setWidthPercentage(100);
        info.addCell(serviceChipFullCell(task.task().getTipoServico()));

        String event = eventLabel(task.event());
        if (!event.isBlank()) {
            PdfPCell eventCell = new PdfPCell(new Phrase(truncate(event, eventLimit), TASK_EVENT));
            eventCell.setBorder(Rectangle.NO_BORDER);
            eventCell.setPadding(0);
            eventCell.setPaddingTop(4.2f);
            info.addCell(eventCell);
        }
        return info;
    }

    private PdfPTable taskInfoLine(TaskView task, float chipWidth, int eventLimit) {
        PdfPTable info = new PdfPTable(new float[]{chipWidth, 5f});
        info.setWidthPercentage(100);
        info.addCell(serviceChipCell(task.task().getTipoServico()));

        PdfPCell eventCell = new PdfPCell(new Phrase(truncate(eventLabel(task.event()), eventLimit), TASK_META));
        eventCell.setBorder(Rectangle.NO_BORDER);
        eventCell.setPadding(0);
        eventCell.setPaddingLeft(3);
        eventCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        info.addCell(eventCell);
        return info;
    }

    private PdfPCell serviceChipCell(String service) {
        Color dark = serviceColor(service);
        Font chipFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 4.6f, dark);
        PdfPCell chip = new PdfPCell(new Phrase(truncate(serviceLabel(service), 16), chipFont));
        chip.setBorder(Rectangle.NO_BORDER);
        chip.setBackgroundColor(serviceFillColor(service));
        chip.setCellEvent(new RoundedChipCellEvent(dark));
        chip.setPaddingTop(1.4f);
        chip.setPaddingBottom(1.6f);
        chip.setPaddingLeft(3f);
        chip.setPaddingRight(3f);
        chip.setVerticalAlignment(Element.ALIGN_MIDDLE);
        chip.setHorizontalAlignment(Element.ALIGN_CENTER);
        return chip;
    }

    private PdfPCell serviceChipFullCell(String service) {
        Color dark = serviceColor(service);
        Font chipFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 5.5f, dark);
        PdfPCell chip = new PdfPCell(new Phrase(truncate(serviceLabel(service), 32), chipFont));
        chip.setBorder(Rectangle.NO_BORDER);
        chip.setBackgroundColor(serviceFillColor(service));
        chip.setCellEvent(new RoundedChipCellEvent(dark));
        chip.setPaddingTop(3f);
        chip.setPaddingBottom(3.2f);
        chip.setPaddingLeft(3f);
        chip.setPaddingRight(3f);
        chip.setVerticalAlignment(Element.ALIGN_MIDDLE);
        chip.setHorizontalAlignment(Element.ALIGN_CENTER);
        return chip;
    }

    private PdfPCell monthDayCell(LocalDate day, List<TaskView> tasks, boolean muted, float cellHeight) {
        PdfPCell outer = new PdfPCell();
        outer.setFixedHeight(cellHeight);
        outer.setBorderColor(BORDER);
        outer.setBorderWidth(0.4f);
        outer.setPadding(0);
        outer.setBackgroundColor(Color.WHITE);

        PdfPTable inner = new PdfPTable(1);
        inner.setWidthPercentage(100);

        // ── day number header ──────────────────────────────────────────────
        Font numFont = muted ? MUTED_DAY_NUMBER : DAY_NUMBER;

        PdfPCell numCell = new PdfPCell();
        numCell.setBorder(Rectangle.NO_BORDER);
        numCell.setPaddingTop(4f);
        numCell.setPaddingLeft(5f);
        numCell.setPaddingBottom(5f);
        numCell.setPaddingRight(3f);
        numCell.setBackgroundColor(Color.WHITE);

        Paragraph numPara = new Paragraph();
        numPara.setLeading(8f);
        numPara.add(new Chunk(String.valueOf(day.getDayOfMonth()), numFont));
        numCell.addElement(numPara);
        inner.addCell(numCell);

        // ── task bars ──────────────────────────────────────────────────────
        List<TaskView> sorted = tasks.stream()
                .sorted(Comparator
                        .comparing((TaskView t) -> t.task().getPrazoHorario(), Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing((TaskView t) -> t.task().getTitulo(), String.CASE_INSENSITIVE_ORDER))
                .toList();

        int maxBars = 3;
        sorted.stream().limit(maxBars).forEach(task -> {
            PdfPCell barCell = monthDaySingleBar(task);
            inner.addCell(barCell);
        });

        if (sorted.size() > maxBars) {
            PdfPCell moreCell = new PdfPCell();
            moreCell.setBorder(Rectangle.NO_BORDER);
            moreCell.setBackgroundColor(Color.WHITE);
            moreCell.setPaddingLeft(5f);
            moreCell.setPaddingTop(1f);
            moreCell.setPaddingBottom(1f);
            Paragraph morePara = new Paragraph("+" + (sorted.size() - maxBars) + " tarefas", MORE);
            morePara.setLeading(7f);
            moreCell.addElement(morePara);
            inner.addCell(moreCell);
        }

        // filler to push content to top
        PdfPCell filler = new PdfPCell();
        filler.setBorder(Rectangle.NO_BORDER);
        filler.setBackgroundColor(Color.WHITE);
        inner.addCell(filler);

        outer.addElement(inner);
        return outer;
    }

    private PdfPCell monthDaySingleBar(TaskView task) {
        Color fill   = serviceFillColor(task.task().getTipoServico());
        Color dark   = serviceColor(task.task().getTipoServico());
        Color status = statusColor(statusName(task));

        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setCellEvent(new LeftAccentCellEvent(status, fill));
        cell.setPaddingTop(1.8f);
        cell.setPaddingBottom(2f);
        cell.setPaddingLeft(7f);   // leaves room for the 2.8pt accent bar painted by event
        cell.setPaddingRight(3f);
        cell.setSpaceCharRatio(0);

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 5.6f, dark);
        Paragraph p = new Paragraph();
        p.setLeading(6f);
        p.add(new Chunk(truncate(task.task().getTitulo(), 26), titleFont));
        cell.addElement(p);

        // service label (tiny, muted)
        Color svcColor = new Color(Math.min(255, dark.getRed() + 40), Math.min(255, dark.getGreen() + 40), Math.min(255, dark.getBlue() + 40));
        Font svcFont = FontFactory.getFont(FontFactory.HELVETICA, 4.6f, svcColor);
        Paragraph svc = new Paragraph();
        svc.setLeading(5.5f);
        svc.add(new Chunk(serviceLabel(task.task().getTipoServico()), svcFont));
        cell.addElement(svc);

        return cell;
    }


    private PdfPCell dotCell(Color color) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setFixedHeight(8);
        cell.setCellEvent(new CircleCellEvent(color));
        return cell;
    }

    private PdfPCell textOnlyCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(0);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private Map<LocalDate, List<TaskView>> groupByDate(List<TaskView> tasks) {
        return tasks.stream()
                .flatMap(task -> expandTaskDates(task).stream().map(date -> Map.entry(date, task)))
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }

    private List<LocalDate> expandTaskDates(TaskView task) {
        LocalDate start = task.task().getPrazoData();
        LocalDate end = task.task().getPrazoDataFim() != null ? task.task().getPrazoDataFim() : task.task().getPrazoData();
        List<LocalDate> dates = new ArrayList<>();
        if (start == null || end == null) return dates;
        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            dates.add(day);
        }
        return dates;
    }

    private String eventLabel(TaskEventSummary event) {
        if (event == null || event.clienteNome() == null || event.clienteNome().isBlank()) {
            return "";
        }
        if (event.data() == null) {
            return event.clienteNome();
        }
        return event.clienteNome() + " - " + event.data().format(DATE);
    }

    private String statusName(TaskView task) {
        return task.task().getStatus() != null ? task.task().getStatus().name() : null;
    }

    private String buildPeriodLabel(String view, LocalDate date) {
        if ("week".equals(view)) {
            LocalDate start = date;
            return "Semana de " + start.format(DATE) + " a " + start.plusDays(6).format(DATE);
        }
        if ("day".equals(view)) {
            return date.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", LOCALE_BR));
        }
        return date.format(DateTimeFormatter.ofPattern("MMMM 'de' yyyy", LOCALE_BR));
    }

    private String normalizeView(String view) {
        if (view == null) return "month";
        if (view.contains("Week")) return "week";
        if (view.contains("Day")) return "day";
        if ("week".equals(view) || "day".equals(view)) return view;
        return "month";
    }

    private String serviceLabel(String service) {
        if ("PINTURA_AO_VIVO".equals(service)) return "Pintura ao Vivo";
        if ("AQUARELA_AO_VIVO".equals(service)) return "Aquarela ao Vivo";
        if ("ENCOMENDA_ARTISTICA".equals(service)) return "Encomenda artística";
        if ("IDENTIDADE_VISUAL".equals(service)) return "Identidade visual";
        return "Geral";
    }

    private Color serviceColor(String service) {
        if ("PINTURA_AO_VIVO".equals(service))    return SVC_PINTURA_DARK;
        if ("AQUARELA_AO_VIVO".equals(service))   return SVC_AQUARELA_DARK;
        if ("ENCOMENDA_ARTISTICA".equals(service)) return SVC_ENCOMENDA_DARK;
        if ("IDENTIDADE_VISUAL".equals(service))   return SVC_IDENTIDADE_DARK;
        return SVC_GERAL_DARK;
    }

    private Color serviceFillColor(String service) {
        if ("PINTURA_AO_VIVO".equals(service))    return SVC_PINTURA_FILL;
        if ("AQUARELA_AO_VIVO".equals(service))   return SVC_AQUARELA_FILL;
        if ("ENCOMENDA_ARTISTICA".equals(service)) return SVC_ENCOMENDA_FILL;
        if ("IDENTIDADE_VISUAL".equals(service))   return SVC_IDENTIDADE_FILL;
        return SVC_GERAL_FILL;
    }

    private Color statusColor(String status) {
        if ("EM_ANDAMENTO".equals(status)) return STATUS_PROGRESS;
        if ("CONCLUIDA".equals(status)) return STATUS_DONE;
        return STATUS_TODO;
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return "";
        if (value.length() <= maxLength) return value;
        if (maxLength <= 1) return value.substring(0, maxLength);
        return value.substring(0, maxLength - 1) + "...";
    }

    private record LegendEntry(String label, Color color) {
    }

    private static class CircleCellEvent implements PdfPCellEvent {
        private final Color color;

        private CircleCellEvent(Color color) {
            this.color = color;
        }

        @Override
        public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
            PdfContentByte canvas = canvases[PdfPTable.BACKGROUNDCANVAS];
            float x = position.getLeft() + 3.2f;
            float y = position.getBottom() + ((position.getTop() - position.getBottom()) / 2f);
            canvas.saveState();
            canvas.setColorFill(color);
            canvas.circle(x, y, 2.1f);
            canvas.fill();
            canvas.restoreState();
        }
    }

    /** Draws a rounded pill border over the chip cell */
    private static class RoundedChipCellEvent implements PdfPCellEvent {
        private final Color borderColor;

        private RoundedChipCellEvent(Color borderColor) {
            this.borderColor = borderColor;
        }

        @Override
        public void cellLayout(PdfPCell cell, Rectangle pos, PdfContentByte[] canvases) {
            PdfContentByte cb = canvases[PdfPTable.BACKGROUNDCANVAS];
            cb.saveState();
            float r = Math.min((pos.getTop() - pos.getBottom()) / 2f, 3.5f);
            cb.setColorStroke(borderColor);
            cb.setLineWidth(0.5f);
            cb.roundRectangle(pos.getLeft() + 0.3f, pos.getBottom() + 0.3f,
                    pos.getRight() - pos.getLeft() - 0.6f,
                    pos.getTop() - pos.getBottom() - 0.6f, r);
            cb.stroke();
            cb.restoreState();
        }
    }

    /** Paints a coloured left accent bar and the pastel fill side by side */
    private static class LeftAccentCellEvent implements PdfPCellEvent {
        private final Color accentColor;
        private final Color fillColor;

        private LeftAccentCellEvent(Color accentColor, Color fillColor) {
            this.accentColor = accentColor;
            this.fillColor   = fillColor;
        }

        @Override
        public void cellLayout(PdfPCell cell, Rectangle pos, PdfContentByte[] canvases) {
            PdfContentByte bg = canvases[PdfPTable.BACKGROUNDCANVAS];
            float h = pos.getTop() - pos.getBottom();
            float w = pos.getRight() - pos.getLeft();
            float accent = 2.8f;
            bg.saveState();
            // pastel body
            bg.setColorFill(fillColor);
            bg.rectangle(pos.getLeft() + accent, pos.getBottom(), w - accent, h);
            bg.fill();
            // status accent bar
            bg.setColorFill(accentColor);
            bg.rectangle(pos.getLeft(), pos.getBottom(), accent, h);
            bg.fill();
            bg.restoreState();
        }
    }
}
