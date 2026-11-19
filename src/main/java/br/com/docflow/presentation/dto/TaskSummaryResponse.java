package br.com.docflow.presentation.dto;

import lombok.AllArgsConstructor;
import br.com.docflow.application.readmodel.TaskSummary;
import lombok.Data;

@Data
@AllArgsConstructor
public class TaskSummaryResponse {
    private long overdue;
    private long today;
    private long next7Days;

    public static TaskSummaryResponse from(TaskSummary summary) {
        return new TaskSummaryResponse(summary.overdue(), summary.today(), summary.next7Days());
    }
}
