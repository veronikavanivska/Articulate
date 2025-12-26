package org.example.apigateway.responses;

import lombok.Data;

@Data
public class ListProfileItem {
    private long id;
    private String fullname;

    private Boolean hasWorker;
    private Boolean hasAdmin;

    private String DegreeTitle;
    private String workerUnitName;
    private String adminUnitName;
}
