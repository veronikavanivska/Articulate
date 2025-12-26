package org.example.apigateway.controllers;

import com.example.generated.ChapterView;
import com.example.generated.MonographView;
import org.checkerframework.common.initializedfields.qual.EnsuresInitializedFields;
import org.example.apigateway.clients.AdminMonoClient;
import org.example.apigateway.mappers.ChapterViewMapper;
import org.example.apigateway.mappers.MonographViewMapper;
import org.example.apigateway.mappers.PageMetaMapper;
import org.example.apigateway.requests.articles.AdminGetRequest;
import org.example.apigateway.requests.articles.AdminListRequest;
import org.example.apigateway.responses.articles.PageMeta;
import org.example.apigateway.responses.mono.ChapterViewResponse;
import org.example.apigateway.responses.mono.ListChaptersResponse;
import org.example.apigateway.responses.mono.ListMonographsResponse;
import org.example.apigateway.responses.mono.MonographViewResponse;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("/monograph/admin")
@RestController
public class AdminMonoController {

    @PostMapping("/listMonographs")
    public ListMonographsResponse listMonographs(@RequestBody AdminListRequest request) {
        var response = AdminMonoClient.listMonographs(request.getId(), request.getTypeId(), request.getDisciplineId(), request.getCycleId(), request.getPage(), request.getSize(), request.getSortBy(), request.getSortDir());

        List<MonographViewResponse> monographView = new ArrayList<>();
        for(MonographView view: response.getMonoghraficViewList()){
            MonographViewResponse result = MonographViewMapper.map(view);
            monographView.add(result);
        }

        PageMeta page = PageMetaMapper.toPageMeta(response.getPageMeta());

        ListMonographsResponse res = new ListMonographsResponse();
        res.setPageMeta(page);
        res.setMonograph(monographView);
        return res;
    }

    @PostMapping("/listChapters")
    public ListChaptersResponse listChapters(@RequestBody AdminListRequest request) {
        var response = AdminMonoClient.listChapters(request.getId(), request.getTypeId(), request.getDisciplineId(), request.getCycleId(), request.getPage(), request.getSize(), request.getSortBy(), request.getSortDir());
        List<ChapterViewResponse> chapterView = new ArrayList<>();
        for(ChapterView view: response.getChapterViewList()){
            ChapterViewResponse result = ChapterViewMapper.map(view);
            chapterView.add(result);
        }
        PageMeta page = PageMetaMapper.toPageMeta(response.getPageMeta());

        ListChaptersResponse res = new ListChaptersResponse();
        res.setPageMeta(page);
        res.setChapters(chapterView);
        return res;
    }


    @PostMapping("/getMonographs")
    public MonographViewResponse getMonograph(@RequestBody AdminGetRequest request) {
        var response = AdminMonoClient.getMonograph(request.getId(), request.getOwnerId());

        MonographViewResponse result = MonographViewMapper.map(response);
        return result;
    }

    @PostMapping("/getChapters")
    public ChapterViewResponse getChapter(@RequestBody AdminGetRequest request) {
        var response = AdminMonoClient.getChapter(request.getId(), request.getOwnerId());
        ChapterViewResponse result = ChapterViewMapper.map(response);
        return result;

    }

}
