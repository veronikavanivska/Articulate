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

    @GetMapping("/listPublication")
    public ListPublicationResponse listPublication(@RequestBody AdminListRequest request) {

        var response = AdminArticleClient.adminListPublications(request.getId(),request.getTypeId(),request.getDisciplineId(),request.getCycleId(),request.getPage(),request.getSize(),request.getSortBy(),request.getSortDir());

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

    @GetMapping("/getPublication")
    public  PublicationViewResponse getPublication(@RequestBody AdminGetRequest request) {
        var response = AdminArticleClient.adminGetPublication(request.getId(), request.getOwnerId());

        PublicationViewResponse viewResponse = PublicationViewMapper.map(response);

        return viewResponse;
    }

    @GetMapping("/listDisciplines")
    public  ListSmthResponse<RefItem> listDisciplines(@RequestBody ListSmthRequest request) {
        var response = AdminArticleClient.adminListDisciplines(request.getPage(), request.getSize(), request.getSortDir());

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
        var response = AdminArticleClient.adminCreateDiscipline(name);

        RefItem refItem = new RefItem();
        refItem.setId(response.getId());
        refItem.setName(name);

        return refItem;
    }


    @PostMapping("/updateDiscipline")
    public RefItem updateDiscipline(@RequestParam("name") String name, @RequestParam("id") Long id) {
        var response = AdminArticleClient.adminUpdateDiscipline(id, name);

        RefItem refItem = new RefItem();
        refItem.setId(response.getId());
        refItem.setName(name);

        return refItem;
    }

    @DeleteMapping("/deleteDiscipline")
    public ApiResponse deleteDiscipline(@RequestParam("id") Long id) {
        var response = AdminArticleClient.adminDeleteDiscipline(id);

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(response.getCode());
        apiResponse.setMessage(response.getMessage());

        return apiResponse;
    }

    @GetMapping("/listTypes")
    public ListSmthResponse<RefItem> listTypes(@RequestBody ListSmthRequest request) {
        var response = AdminArticleClient.adminListPublicationTypes(request.getPage(), request.getSize(), request.getSortDir());

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
        var response = AdminArticleClient.adminCreatePublicationType(name);

        RefItem refItem = new RefItem();
        refItem.setId(response.getId());
        refItem.setName(name);

        return refItem;
    }

    @PostMapping("/updateType")
    public RefItem updateType(@RequestParam("name") String name, @RequestParam("id") Long id) {
        var response = AdminArticleClient.adminUpdatePublicationType(id, name);

        RefItem refItem = new RefItem();
        refItem.setId(response.getId());
        refItem.setName(name);

        return refItem;
    }

    @DeleteMapping("/deleteType")
    public ApiResponse deleteType(@RequestParam("id") Long id) {
        var response = AdminArticleClient.adminDeletePublicationType(id);

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(response.getCode());
        apiResponse.setMessage(response.getMessage());

        return apiResponse;
    }

    @GetMapping("/listEvalCycles")
    public ListSmthResponse<CycleItem> listEvalCycles(@RequestBody ListSmthRequest request) {
        var response = AdminArticleClient.adminListEvalCycles(request.getPage(), request.getSize(), request.getSortDir());

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
            cycleItem.add(item);
        }

        PageMeta page = PageMetaMapper.toPageMeta(response.getPage());
        result.setPageMeta(page);
        result.setItem(cycleItem);

        return result;
    }

    @PostMapping("/createEvalCycle")
    public CycleItem createEvalCycle(@RequestBody CreateCycleRequest request) {
        var response = AdminArticleClient.adminCreateCycle(request.getName(), request.getYearFrom(), request.getYearTo(), request.isActive());

        CycleItem cycleItem = new CycleItem();
        cycleItem.setId(response.getId());
        cycleItem.setName(response.getName());
        cycleItem.setYearFrom(request.getYearFrom());
        cycleItem.setYearTo(request.getYearTo());
        cycleItem.setActive(request.isActive());
        cycleItem.setMeinMonoVersionId(response.getMonoVersionId());
        cycleItem.setMeinVersionId(response.getMeinVersionId());

        return cycleItem;
    }

    @PatchMapping("/updateEvalCycle")
    public CycleItem updateEvalCycle(@RequestBody UpdateCycleRequest request) {
        var response = AdminArticleClient.adminUpdateCycle(request.getId(), request.getName(), request.getYearFrom(), request.getYearTo(), request.getActive(), request.getMeinVersionId(), request.getMeinMonoVersionId());

        CycleItem cycleItem = new CycleItem();
        cycleItem.setId(response.getId());
        cycleItem.setName(response.getName());
        cycleItem.setYearFrom(response.getYearFrom());
        cycleItem.setYearTo(response.getYearTo());
        cycleItem.setActive(response.getIsActive());
        cycleItem.setMeinMonoVersionId(response.getMonoVersionId());
        cycleItem.setMeinVersionId(response.getMeinVersionId());

        return cycleItem;
    }

    @DeleteMapping("/deleteEvalCycle")
    public ApiResponse deleteEvalCycle(@RequestParam("id") Long id) {
        var response = AdminArticleClient.adminDeleteCycle(id);

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(response.getCode());
        apiResponse.setMessage(response.getMessage());

        return apiResponse;
    }


}
