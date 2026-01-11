package org.example.apigateway.controllers;

import org.example.apigateway.clients.SlotsClient;
import org.example.apigateway.config.SecurityConfig;
import org.example.apigateway.mappers.DraftViewMapper;
import org.example.apigateway.requests.GetDraftRequest;
import org.example.apigateway.requests.SlotsRequest;
import org.example.apigateway.responses.DraftViewResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/slots")
public class SlotController {

    private final SlotsClient slotsClient;

    public SlotController(SlotsClient slotsClient) {
        this.slotsClient = slotsClient;
    }
    @PostMapping("/addToSlot")
    public DraftViewResponse addToActiveSlot(@RequestBody SlotsRequest request) {
        Long userId = Long.parseLong(SecurityConfig.getCurrentUserId());

        var response = slotsClient.addToActiveSlot(userId, request.getDisciplineId(), request.getItemType(), request.getItemId());

        DraftViewResponse draftViewResponse = DraftViewMapper.toResponse(response);

        return draftViewResponse;

    }

    @DeleteMapping("/removeFromSlots")
    public DraftViewResponse removeFromActiveSlot(@RequestBody SlotsRequest request) {
        Long userId = Long.parseLong(SecurityConfig.getCurrentUserId());

        var response = slotsClient.removeFromActiveSlot(userId, request.getDisciplineId(), request.getItemType(), request.getItemId());

        DraftViewResponse draftViewResponse = DraftViewMapper.toResponse(response);

        return draftViewResponse;
    }

    @PostMapping("/getSlots")
    public DraftViewResponse getSlots(@RequestBody GetDraftRequest request) {
        Long userId = Long.parseLong(SecurityConfig.getCurrentUserId());

        var response = slotsClient.getDraft(userId, request.getDisciplineId(), request.getEvalCycle(), request.getEvalYear());

        DraftViewResponse draftViewResponse = DraftViewMapper.toResponse(response);

        return draftViewResponse;
    }


}
