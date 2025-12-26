package org.example.apigateway.controllers;

import com.example.generated.*;
import org.example.apigateway.clients.ProfilesClient;
import org.example.apigateway.mappers.ProfileMapper;
import org.example.apigateway.config.SecurityConfig;
import org.example.apigateway.requests.GetOrCreateStatementRequest;
import org.example.apigateway.requests.ListProfilesRequest;
import org.example.apigateway.requests.profiles.UpdateProfileRequest;
import org.example.apigateway.responses.*;

import org.example.apigateway.responses.AdminInitStatementsForYearResponse;
import org.example.apigateway.responses.ApiResponse;
import org.example.apigateway.responses.GetOrCreateStatementResponse;
import org.example.apigateway.responses.GetProfileResponse;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/profile")
public class ProfilesController {

    @GetMapping("/me")
    public GetProfileResponse getProfile() {
        Long userId = Long.parseLong(SecurityConfig.getCurrentUserId());

        var response = ProfilesClient.getProfile(userId);

        return ProfileMapper.toResponse(response);
    }

    @PostMapping("/update")
    public Response<Void> updateProfile(@RequestBody UpdateProfileRequest request) {
        Long userId = Long.parseLong(SecurityConfig.getCurrentUserId());

        var response = ProfilesClient.updateMyProfile(request, userId);

        var api = response.getResponse();

        return new Response<>(
                api.getCode(),
                api.getMessage()
        );
    }


    @GetMapping("/someone")
    public GetProfileResponse getSomeOneProfile(@RequestParam Long userId) {
        var response = ProfilesClient.seeSomeoneProfile(userId);

        return ProfileMapper.toResponse(response);
    }

    @GetMapping("/me/disciplines")
    public ListWorkerDisciplineResponse getWorkerDisciplines() {
        Long userId = Long.parseLong(SecurityConfig.getCurrentUserId());

        var response = ProfilesClient.listWorkerDisciplines(userId);

        ListWorkerDisciplineResponse result = new ListWorkerDisciplineResponse();

        List<DisciplineResponse> disciplineResponseList = new ArrayList<>();
        for (DisciplineRef ref : response.getDisciplinesList()) {
            DisciplineResponse disciplineResponse = new DisciplineResponse();
            disciplineResponse.setId(ref.getId());
            disciplineResponse.setName(ref.getName());
            disciplineResponseList.add(disciplineResponse);
        }

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(response.getResponse().getCode());
        apiResponse.setMessage(response.getResponse().getMessage());

        result.setDiscipline(disciplineResponseList);
        result.setApiResponse(apiResponse);

        return result;
    }

    @PostMapping("/addDiscipline")
    public ListWorkerDisciplineResponse addWorkerDiscipline(@RequestParam Long userId, @RequestParam Long disciplineId) {
        var response = ProfilesClient.addWorkerDiscipline(userId, disciplineId);

        ListWorkerDisciplineResponse result = new ListWorkerDisciplineResponse();

        List<DisciplineResponse> disciplineResponseList = new ArrayList<>();
        for (DisciplineRef ref : response.getDisciplinesList()) {
            DisciplineResponse disciplineResponse = new DisciplineResponse();
            disciplineResponse.setId(ref.getId());
            disciplineResponse.setName(ref.getName());
            disciplineResponseList.add(disciplineResponse);
        }

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(response.getResponse().getCode());
        apiResponse.setMessage(response.getResponse().getMessage());

        result.setDiscipline(disciplineResponseList);
        result.setApiResponse(apiResponse);

        return result;
    }

    @DeleteMapping("deleteDiscipline")
    public ListWorkerDisciplineResponse removeWorkerDiscipline(@RequestParam Long userId, @RequestParam Long disciplineId){
        var response = ProfilesClient.removeWorkerDiscipline(userId, disciplineId);

        ListWorkerDisciplineResponse result = new ListWorkerDisciplineResponse();

        List<DisciplineResponse> disciplineResponseList = new ArrayList<>();
        for (DisciplineRef ref : response.getDisciplinesList()) {
            DisciplineResponse disciplineResponse = new DisciplineResponse();
            disciplineResponse.setId(ref.getId());
            disciplineResponse.setName(ref.getName());
            disciplineResponseList.add(disciplineResponse);
        }

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(response.getResponse().getCode());
        apiResponse.setMessage(response.getResponse().getMessage());

        result.setDiscipline(disciplineResponseList);
        result.setApiResponse(apiResponse);

        return result;
    }

    @PostMapping("/createStatement")
    public GetOrCreateStatementResponse createStatement(@RequestBody GetOrCreateStatementRequest request) {
        var response = ProfilesClient.getOrCreateStatement(request.getUserId(), request.getDisciplineId(), request.getEvalYear());
        WorkerStatement st = response.getStatement();

        GetOrCreateStatementResponse result = new GetOrCreateStatementResponse();

        WorkerStatementResponse dto = new WorkerStatementResponse();
        dto.setUserId(st.getUserId());
        dto.setDisciplineId(st.getDisciplineId());
        dto.setEvalYear(st.getEvalYear());

        dto.setFte(st.getFte());
        dto.setSharePercent(st.getSharePercent());
        dto.setSlotInDiscipline(st.getSlotInDiscipline());

        dto.setMaxSlots(st.getMaxSlots());
        dto.setMaxMonoSlots(st.getMaxMonoSlots());

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(response.getResponse().getCode());
        apiResponse.setMessage(response.getResponse().getMessage());

        result.setStatement(dto);
        result.setApiResponse(apiResponse);

        return result;
    }

    @PostMapping("/createStatements")
    public AdminInitStatementsForYearResponse createStatements(@RequestParam int year) {
        var response = ProfilesClient.adminInitStatementsForYear(year);

        AdminInitStatementsForYearResponse result = new AdminInitStatementsForYearResponse();

        result.setYear(response.getEvalYear());
        result.setCount(response.getCreatedCount());
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(response.getResponse().getCode());
        apiResponse.setMessage(response.getResponse().getMessage());
        result.setApiResponse(apiResponse);
        return result;
    }

    @PostMapping("/listProfiles")
    public ListProfileResponse listProfiles(@RequestBody ListProfilesRequest request) {
        String fullName = request.getFullName() == null ? "" : request.getFullName().trim();
        int page = request.getPage() == null ? 0 : Math.max(request.getPage(), 0);
        int size = request.getSize() == null ? 20 : Math.min(Math.max(request.getSize(), 1), 100);
        String sortBy = request.getSortBy() == null ? "fullname" : request.getSortBy().trim();
        String sortDir = request.getSortDir() == null ? "asc" : request.getSortDir().trim();

        var response = ProfilesClient.allProfiles(fullName, page, size, sortBy, sortDir);

        ListProfileResponse result = new ListProfileResponse();

        List<ListProfileItem> item = new ArrayList<>();
        for(AdminProfileListItem listItem : response.getItemsList()){
            ListProfileItem profileItem = new ListProfileItem();
            profileItem.setId(listItem.getUserId());
            profileItem.setFullname(listItem.getFullname());
            profileItem.setHasWorker(listItem.getHasWorker());
            profileItem.setHasAdmin(listItem.getHasAdmin());
            profileItem.setDegreeTitle(listItem.getWorkerDegreeTitle());
            profileItem.setWorkerUnitName(listItem.getWorkerUnitName());
            profileItem.setAdminUnitName(listItem.getAdminUnitName());
            item.add(profileItem);
        }

        result.setItems(item);
        result.setTotal(response.getTotalElements());
        result.setTotalPages(response.getTotalPages());
        return result;

    }

    @GetMapping("/listDisciplines")
    public ListWorkerDisciplineResponse listDisciplines() {
        var response = ProfilesClient.listDiscipline();

        ListWorkerDisciplineResponse result = new ListWorkerDisciplineResponse();

        List<DisciplineResponse> disciplineResponseList = new ArrayList<>();
        for (DisciplineRef ref : response.getDisciplinesList()) {
            DisciplineResponse disciplineResponse = new DisciplineResponse();
            disciplineResponse.setId(ref.getId());
            disciplineResponse.setName(ref.getName());
            disciplineResponseList.add(disciplineResponse);
        }

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(response.getResponse().getCode());
        apiResponse.setMessage(response.getResponse().getMessage());

        result.setDiscipline(disciplineResponseList);
        result.setApiResponse(apiResponse);

        return result;
    }
}
