package org.example.apigateway.responses;
import lombok.Data;
import org.example.apigateway.requests.SlotItemType;

@Data
public class DraftItemResponse {
    private SlotItemType itemType ;
    private long itemId ;

    private int publicationYear;
    private String title;

    private double points ;
    private double slotValue ;
    private double pointsRecalc ;
}
