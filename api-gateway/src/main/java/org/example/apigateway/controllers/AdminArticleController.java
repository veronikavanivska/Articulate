package org.example.apigateway.controllers;

import com.example.generated.GetPublicationRequest;
import com.example.generated.PublicationView;
import org.example.apigateway.clients.AdminArticleClient;
import org.example.apigateway.mappers.PageMetaMapper;
import org.example.apigateway.mappers.PublicationViewMapper;
import org.example.apigateway.requests.articles.AdminGetPublicationRequest;
import org.example.apigateway.requests.articles.AdminListPublicationRequest;
import org.example.apigateway.requests.articles.ListPublicationRequest;
import org.example.apigateway.requests.articles.Page;
import org.example.apigateway.responses.articles.ListPublicationResponse;
import org.example.apigateway.responses.articles.PageMeta;
import org.example.apigateway.responses.articles.PublicationViewResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/article/admin")
public class AdminArticleController {

    @GetMapping("/listPublication")
    public ListPublicationResponse listPublication(@RequestBody AdminListPublicationRequest request) {

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
    public PublicationViewResponse getPublication(@RequestBody AdminGetPublicationRequest request) {
        var response = AdminArticleClient.adminGetPublication(request.getId(), request.getOwnerId());

        PublicationViewResponse viewResponse = PublicationViewMapper.map(response);

        return viewResponse;
    }

    @
}
