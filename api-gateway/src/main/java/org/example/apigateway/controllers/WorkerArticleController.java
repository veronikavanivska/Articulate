package org.example.apigateway.controllers;

import com.example.generated.PublicationView;
import org.example.apigateway.clients.WorkerArticleClient;
import org.example.apigateway.mappers.PublicationViewMapper;
import org.example.apigateway.config.SecurityConfig;
import org.example.apigateway.requests.articles.ListRequest;
import org.example.apigateway.requests.articles.UpdatePublicationRequest;
import org.example.apigateway.responses.ApiResponse;
import org.example.apigateway.responses.articles.*;
import org.springframework.web.bind.annotation.*;
import org.example.apigateway.requests.articles.CreatePublicationRequest;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/article")
public class WorkerArticleController {

    @PostMapping("/worker/createPublication")
    public PublicationViewResponse createPublication(@RequestBody CreatePublicationRequest request) {
        Long userId =  Long.parseLong(SecurityConfig.getCurrentUserId());

        var response = WorkerArticleClient.createPublication(userId, request.getTypeId(), request.getDisciplineId(), request.getTitle(), request.getDoi(), request.getIssn(), request.getEissn(), request.getJournalTitle(), request.getPublicationYear(), request.getCoauthors() );

        return PublicationViewMapper.map(response);
    }

    @GetMapping("/worker/getPublication")
    public PublicationViewResponse getPublication(@RequestParam  long publicationId) {
        Long userId =  Long.parseLong(SecurityConfig.getCurrentUserId());

        var response = WorkerArticleClient.getPublication(userId, publicationId);

        return PublicationViewMapper.map(response);

    }

    @PostMapping("/worker/listMyPublication")
    public ListPublicationResponse listMyPublications(@RequestBody ListRequest request){
        Long userId =  Long.parseLong(SecurityConfig.getCurrentUserId());

        var response = WorkerArticleClient.listMyPublications(userId, request.getTypeId(), request.getDisciplineId(),request.getCycleId(), request.getPage(), request.getSize(), request.getSortBy(), request.getSortDir());

        ListPublicationResponse publicationResponse = new ListPublicationResponse();

        List<PublicationViewResponse> publicationView = new ArrayList<>();
        for(PublicationView view : response.getItemsList()){
           PublicationViewResponse publicationViewResponse =  PublicationViewMapper.map(view);
           publicationView.add(publicationViewResponse);
        }

        PageMeta pageMeta = new PageMeta();
        pageMeta.setPage(response.getPage().getPage());
        pageMeta.setSize(response.getPage().getSize());
        pageMeta.setTotalPages(response.getPage().getTotalPages());
        pageMeta.setTotalItems(response.getPage().getTotalItems());

        publicationResponse.setPublications(publicationView);
        publicationResponse.setPageMeta(pageMeta);

        return publicationResponse;
    }

    @PatchMapping("/worker/updatePublication")
    public PublicationViewResponse updatePublication(@RequestBody UpdatePublicationRequest request){
        Long userId =  Long.parseLong(SecurityConfig.getCurrentUserId());
        var response = WorkerArticleClient.updatePublication(request.getId(), userId , request.getTypeId(), request.getDisciplineId(), request.getTitle(), request.getDoi(), request.getIssn(), request.getEissn(), request.getJournalTitle(), request.getPublicationYear(),request.getReplaceCoauthors());

        return PublicationViewMapper.map(response);
    }

    @DeleteMapping("/worker/deletePublication")
    public ApiResponse deletePublication(@RequestParam long publicationId){

        Long userId =  Long.parseLong(SecurityConfig.getCurrentUserId());

        var response = WorkerArticleClient.deletePublication( publicationId, userId);

        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(response.getCode());
        apiResponse.setMessage(response.getMessage());

        return apiResponse;
    }



}
