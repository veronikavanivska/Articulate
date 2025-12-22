package org.example.apigateway.responses;

import lombok.Data;

@Data
public class WorkerStatementResponse {
    private Long userId;
    private Long disciplineId;
    private Integer evalYear;

    private Double fte;
    private Double sharePercent;
    private Double slotInDiscipline;

    private Double maxSlots;
    private Double maxMonoSlots;
}
