package br.com.docflow.presentation.dto;

import lombok.Data;

@Data
public class TaskChecklistItemRequest {
    private Long id;
    private String texto;
    private boolean concluido;
    private Integer ordem;
}
