package org.example.apigateway.requests.articles;

import lombok.Data;

@Data
public class ListPublicationRequest {
    public long typeId;
    public long disciplineId;
    public long cycleId;
    public int page;
    public int size;
    public String sortBy;
    public String sortDir;
}
