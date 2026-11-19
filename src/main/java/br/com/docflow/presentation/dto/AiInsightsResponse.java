package br.com.docflow.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AiInsightsResponse {
    private List<String> insights;
}
