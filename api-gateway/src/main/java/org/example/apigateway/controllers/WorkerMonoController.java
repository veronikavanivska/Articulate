package org.example.apigateway.controllers;

import com.example.generated.ChapterView;
import com.example.generated.GetMonographRequest;
import com.example.generated.ListChaptersResponse;
import com.example.generated.MonographView;
import org.example.apigateway.clients.WorkerMonoClient;
import org.example.apigateway.config.SecurityConfig;
import org.example.apigateway.mappers.ChapterViewMapper;
import org.example.apigateway.mappers.MonographViewMapper;
import org.example.apigateway.requests.articles.AdminListRequest;
import org.example.apigateway.requests.articles.ListRequest;
import org.example.apigateway.requests.mono.CreateChapterRequest;
import org.example.apigateway.requests.mono.CreateMonographRequest;
import org.example.apigateway.requests.mono.UpdateChapterRequest;
import org.example.apigateway.requests.mono.UpdateMonographRequest;
import org.example.apigateway.responses.ApiResponse;
import org.example.apigateway.responses.articles.PageMeta;
import org.example.apigateway.responses.mono.ChapterViewResponse;
import org.example.apigateway.responses.mono.ListMonographsResponse;
import org.example.apigateway.responses.mono.MonographViewResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/monograph/worker")
public class WorkerMonoController {


    @PostMapping("/createMonograph")
    public MonographViewResponse createMonograph(@RequestBody CreateMonographRequest request){
        Long userId =  Long.parseLong(SecurityConfig.getCurrentUserId());

        var response = WorkerMonoClient.createMonograph(userId, request.getTypeId(), request.getDisciplineId(), request.getTitle(), request.getDoi(), request.getIsbn(), request.getMonograficPublisherTitle(), request.getPublicationYear(), request.getCoauthors());

        MonographViewResponse monographViewResponse = MonographViewMapper.map(response);

        return monographViewResponse;
    }

    @PostMapping("/createChapter")
    public ChapterViewResponse createChapter(@RequestBody CreateChapterRequest request){
        Long userId =  Long.parseLong(SecurityConfig.getCurrentUserId());

        var response = WorkerMonoClient.createChapter(userId, request.getTypeId(), request.getDisciplineId(), request.getMonograficChapterTitle(), request.getMonograficTitle(), request.getMonographPublisher(), request.getDoi(), request.getIsbn(), request.getPublicationYear(), request.getCoauthor());

        ChapterViewResponse chapterViewResponse = ChapterViewMapper.map(response);
        return chapterViewResponse;
    }

    @GetMapping("/getMonograph")
    public MonographViewResponse getMonograph(@RequestParam("id") Long id){
        Long userId =  Long.parseLong(SecurityConfig.getCurrentUserId());

        var response = WorkerMonoClient.getMonograph(id, userId);

        MonographViewResponse monographViewResponse = MonographViewMapper.map(response);

        return monographViewResponse;
    }

    @GetMapping("/getChapter")
    public ChapterViewResponse getChapter(@RequestParam("id") Long id){
        Long userId =  Long.parseLong(SecurityConfig.getCurrentUserId());

        var response = WorkerMonoClient.getChapter(id, userId);

        ChapterViewResponse chapterViewResponse = ChapterViewMapper.map(response);

        return chapterViewResponse;
    }

    @PatchMapping("/updateMonograph")
    public MonographViewResponse updateMonograph(@RequestBody UpdateMonographRequest request){
        Long userId =  Long.parseLong(SecurityConfig.getCurrentUserId());

        var response = WorkerMonoClient.updateMonograph(request.getId(),userId, request.getTypeId(), request.getDisciplineId(), request.getTitle(), request.getDoi(), request.getIsbn(), request.getMonograficPublisherTitle(), request.getPublicationYear(), request.getCoauthors());

        MonographViewResponse monographViewResponse = MonographViewMapper.map(response);
        return monographViewResponse;
    }

    @PatchMapping("/updateChapter")
    public ChapterViewResponse updateChapter(@RequestBody UpdateChapterRequest request){
        Long userId =  Long.parseLong(SecurityConfig.getCurrentUserId());

        var response = WorkerMonoClient.updateChapter(request.getId(),userId , request.getTypeId(), request.getDisciplineId(), request.getMonograficChapterTitle(), request.getMonograficTitle(), request.getMonographPublisher(), request.getDoi(), request.getIsbn(), request.getPublicationYear(), request.getCoauthor());

        ChapterViewResponse chapterViewResponse = ChapterViewMapper.map(response);

        return chapterViewResponse;
    }

    @PostMapping("/listMyMonographs")
    public ListMonographsResponse listMyMonographs(@RequestBody ListRequest request){
        Long userId =  Long.parseLong(SecurityConfig.getCurrentUserId());

        var response = WorkerMonoClient.listMonographs(userId, request.getTypeId(), request.getDisciplineId(), request.getCycleId(), request.getPage(), request.getSize(), request.getSortBy(), request.getSortDir());

        ListMonographsResponse monographsResponse = new ListMonographsResponse();

        List<MonographViewResponse> monographView = new ArrayList<>();
        for(MonographView view : response.getMonoghraficViewList()){
            monographView.add(MonographViewMapper.map(view));
        }

        PageMeta pageMeta = new PageMeta();
        pageMeta.setPage(response.getPageMeta().getPage());
        pageMeta.setSize(response.getPageMeta().getSize());
        pageMeta.setTotalPages(response.getPageMeta().getTotalPages());
        pageMeta.setTotalItems(response.getPageMeta().getTotalItems());


        monographsResponse.setPageMeta(pageMeta);
        monographsResponse.setMonograph(monographView);

        return monographsResponse;
    }


    @PostMapping("/listMyChapters")
    public org.example.apigateway.responses.mono.ListChaptersResponse listMyChapters(@RequestBody ListRequest request){
        Long userId =  Long.parseLong(SecurityConfig.getCurrentUserId());

        var response = WorkerMonoClient.listChapters(userId, request.getTypeId(), request.getDisciplineId(), request.getCycleId(), request.getPage(), request.getSize(), request.getSortBy(), request.getSortDir());

        org.example.apigateway.responses.mono.ListChaptersResponse chapterViewResponse = new org.example.apigateway.responses.mono.ListChaptersResponse();

        List<ChapterViewResponse> chapterView = new ArrayList<>();
        for(ChapterView view : response.getChapterViewList()){
            chapterView.add(ChapterViewMapper.map(view));
        }

        PageMeta pageMeta = new PageMeta();
        pageMeta.setPage(response.getPageMeta().getPage());
        pageMeta.setSize(response.getPageMeta().getSize());
        pageMeta.setTotalPages(response.getPageMeta().getTotalPages());
        pageMeta.setTotalItems(response.getPageMeta().getTotalItems());


        chapterViewResponse.setPageMeta(pageMeta);
        chapterViewResponse.setChapters(chapterView);

        return chapterViewResponse;
    }

    @DeleteMapping("/deleteMonograph")
    public ApiResponse deleteMonograph(@RequestParam("id") Long id){
        Long userId =  Long.parseLong(SecurityConfig.getCurrentUserId());
        var response = WorkerMonoClient.deleteMonograph( id,userId);

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(response.getCode());
        apiResponse.setMessage(response.getMessage());
        return apiResponse;

    }

    @DeleteMapping("/deleteChapter")
    public ApiResponse deleteChapter(@RequestParam("id") Long id){
        Long userId =  Long.parseLong(SecurityConfig.getCurrentUserId());
        var response = WorkerMonoClient.deleteChapter( id,userId);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(response.getCode());
        apiResponse.setMessage(response.getMessage());
        return apiResponse;
    }

}
