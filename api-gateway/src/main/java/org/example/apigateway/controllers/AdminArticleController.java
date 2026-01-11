package org.example.apigateway.controllers;

import com.example.generated.*;
import org.example.apigateway.clients.AdminArticleClient;
import org.example.apigateway.mappers.PageMetaMapper;
import org.example.apigateway.mappers.PublicationViewMapper;
import org.example.apigateway.requests.ListSmthRequest;
import org.example.apigateway.requests.articles.*;
import org.example.apigateway.requests.articles.CreateCycleRequest;
import org.example.apigateway.requests.articles.UpdateCycleRequest;
import org.example.apigateway.responses.ApiResponse;
import org.example.apigateway.responses.ListSmthResponse;
import org.example.apigateway.responses.articles.*;
import org.example.apigateway.responses.articles.CycleItem;
import org.example.apigateway.responses.articles.PageMeta;
import org.example.apigateway.responses.articles.RefItem;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/article/admin")
public class AdminArticleController {

    private final AdminArticleClient adminArticleClient;

    public AdminArticleController(AdminArticleClient adminArticleClient) {
        this.adminArticleClient = adminArticleClient;
    }

    @PostMapping("/listPublication")
    public ListPublicationResponse listPublication(@RequestBody AdminListRequest request) {

        var response = adminArticleClient.adminListPublications(request.getId(),request.getTypeId(),request.getDisciplineId(),request.getCycleId(),request.getPage(),request.getSize(),request.getSortBy(),request.getSortDir(),request.getTitle());

        List<PublicationViewResponse> publicationView = new ArrayList<>();
        for(PublicationView view : response.getItemsList())
        {
            PublicationViewResponse viewResponse = PublicationViewMapper.map(view);
            publicationView.add(viewResponse);
        }

        PageMeta page = PageMetaMapper.toPageMeta(response.getPage());

        ListPublicationResponse result = new ListPublicationResponse();
        result.setPublications(publicationView);
        result.setPageMeta(page);

        return result;

    }

    @PostMapping("/getPublication")
    public  PublicationViewResponse getPublication(@RequestBody AdminGetRequest request) {
        var response = adminArticleClient.adminGetPublication(request.getId(), request.getOwnerId());

        PublicationViewResponse viewResponse = PublicationViewMapper.map(response);

        return viewResponse;
    }

    @PostMapping("/listDisciplines")
    public  ListSmthResponse<RefItem> listDisciplines(@RequestBody ListSmthRequest request) {
        var response = adminArticleClient.adminListDisciplines(request.getPage(), request.getSize(), request.getSortDir());

        ListSmthResponse<RefItem> result = new ListSmthResponse<>();

        List<RefItem> refItem = new ArrayList<>();

        for (com.example.generated.RefItem ref : response.getItemsList()) {
            RefItem item = new RefItem();
            item.setId(ref.getId());
            item.setName(ref.getName());

            refItem.add(item);
        }

        PageMeta page = PageMetaMapper.toPageMeta(response.getPage());

        result.setPageMeta(page);
        result.setItem(refItem);

        return result;
    }

    @PostMapping("/createDiscipline")
    public RefItem createDiscipline(@RequestParam("name") String name) {
        var response = adminArticleClient.adminCreateDiscipline(name);

        RefItem refItem = new RefItem();
        refItem.setId(response.getId());
        refItem.setName(name);

        return refItem;
    }


    @PostMapping("/updateDiscipline")
    public RefItem updateDiscipline(@RequestParam("name") String name, @RequestParam("id") Long id) {
        var response = adminArticleClient.adminUpdateDiscipline(id, name);

        RefItem refItem = new RefItem();
        refItem.setId(response.getId());
        refItem.setName(name);

        return refItem;
    }

    @DeleteMapping("/deleteDiscipline")
    public ApiResponse deleteDiscipline(@RequestParam("id") Long id) {
        var response = adminArticleClient.adminDeleteDiscipline(id);

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(response.getCode());
        apiResponse.setMessage(response.getMessage());

        return apiResponse;
    }

    @PostMapping("/listTypes")
    public ListSmthResponse<RefItem> listTypes(@RequestBody ListSmthRequest request) {
        var response = adminArticleClient.adminListPublicationTypes(request.getPage(), request.getSize(), request.getSortDir());

        ListSmthResponse<RefItem> result = new ListSmthResponse<>();
        List<RefItem> refItem = new ArrayList<>();

        for (com.example.generated.RefItem ref : response.getItemsList()) {
            RefItem item = new RefItem();
            item.setId(ref.getId());
            item.setName(ref.getName());
            refItem.add(item);
        }

        PageMeta page = PageMetaMapper.toPageMeta(response.getPage());
        result.setPageMeta(page);
        result.setItem(refItem);

        return result;
    }

    @PostMapping("/createType")
    public RefItem createType(@RequestParam("name") String name) {
        var response = adminArticleClient.adminCreatePublicationType(name);

        RefItem refItem = new RefItem();
        refItem.setId(response.getId());
        refItem.setName(name);

        return refItem;
    }

    @PostMapping("/updateType")
    public RefItem updateType(@RequestParam("name") String name, @RequestParam("id") Long id) {
        var response = adminArticleClient.adminUpdatePublicationType(id, name);

        RefItem refItem = new RefItem();
        refItem.setId(response.getId());
        refItem.setName(name);

        return refItem;
    }

    @DeleteMapping("/deleteType")
    public ApiResponse deleteType(@RequestParam("id") Long id) {
        var response = adminArticleClient.adminDeletePublicationType(id);

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(response.getCode());
        apiResponse.setMessage(response.getMessage());

        return apiResponse;
    }

    @PostMapping("/listEvalCycles")
    public ListSmthResponse<CycleItem> listEvalCycles(@RequestBody ListSmthRequest request) {
        var response = adminArticleClient.adminListEvalCycles(request.getPage(), request.getSize(), request.getSortDir());

        ListSmthResponse<CycleItem> result = new ListSmthResponse<>();
        List<CycleItem> cycleItem = new ArrayList<>();
        for (com.example.generated.CycleItem cycle : response.getItemsList()) {
            CycleItem item = new CycleItem();
            item.setId(cycle.getId());
            item.setName(cycle.getName());
            item.setYearFrom(cycle.getYearFrom());
            item.setYearTo(cycle.getYearTo());
            item.setActive(cycle.getIsActive());
            item.setMeinVersionId(cycle.getMeinVersionId());
            item.setMeinMonoVersionId(cycle.getMonoVersionId());
            item.setActiveYear(cycle.getActiveYear());
            cycleItem.add(item);
        }

        PageMeta page = PageMetaMapper.toPageMeta(response.getPage());
        result.setPageMeta(page);
        result.setItem(cycleItem);

        return result;
    }

    @PostMapping("/createEvalCycle")
    public CycleItem createEvalCycle(@RequestBody CreateCycleRequest request) {
        var response = adminArticleClient.adminCreateCycle(request.getName(), request.getYearFrom(), request.getYearTo(), request.isActive(), request.getActiveYear());

        CycleItem cycleItem = new CycleItem();
        cycleItem.setId(response.getId());
        cycleItem.setName(response.getName());
        cycleItem.setYearFrom(request.getYearFrom());
        cycleItem.setYearTo(request.getYearTo());
        cycleItem.setActive(request.isActive());
        cycleItem.setMeinMonoVersionId(response.getMonoVersionId());
        cycleItem.setMeinVersionId(response.getMeinVersionId());
        cycleItem.setActiveYear(response.getActiveYear());


        return cycleItem;
    }

    @PatchMapping("/updateEvalCycle")
    public CycleItem updateEvalCycle(@RequestBody UpdateCycleRequest request) {
        var response = adminArticleClient.adminUpdateCycle(request.getId(), request.getName(), request.getYearFrom(), request.getYearTo(), request.getActive(), request.getMeinVersionId(), request.getMeinMonoVersionId(),request.getActiveYear());

        CycleItem cycleItem = new CycleItem();
        cycleItem.setId(response.getId());
        cycleItem.setName(response.getName());
        cycleItem.setYearFrom(response.getYearFrom());
        cycleItem.setYearTo(response.getYearTo());
        cycleItem.setActive(response.getIsActive());
        cycleItem.setMeinMonoVersionId(response.getMonoVersionId());
        cycleItem.setMeinVersionId(response.getMeinVersionId());
        cycleItem.setActiveYear(response.getActiveYear());

        return cycleItem;
    }

    @DeleteMapping("/deleteEvalCycle")
    public ApiResponse deleteEvalCycle(@RequestParam("id") Long id) {
        var response = adminArticleClient.adminDeleteCycle(id);

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(response.getCode());
        apiResponse.setMessage(response.getMessage());

        return apiResponse;
    }


}
