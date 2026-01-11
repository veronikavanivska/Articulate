package org.example.apigateway.controllers;

import org.example.apigateway.clients.ArticleClient;
import org.example.apigateway.clients.ETLArticleClient;
import org.example.apigateway.config.SecurityConfig;
import org.example.apigateway.responses.MEiNResponse;
import org.example.apigateway.responses.articles.CycleItem;
import org.example.apigateway.responses.articles.RefItem;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Ref;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/article")
public class ArticleController {

    private final ArticleClient articleClient;

    public ArticleController(ArticleClient articleClient) {
        this.articleClient = articleClient;
    }

    @GetMapping("listPublicationType")
    public List<RefItem> listPublicationType() {
        var response = articleClient.listPublicationTypes();

        List<RefItem> refItems = new ArrayList<>();
        for(com.example.generated.RefItem refItem : response.getItemsList())
        {
            RefItem item = new RefItem();
            item.setId(refItem.getId());
            item.setName(refItem.getName());

            refItems.add(item);
        }

        return refItems;
    }


    @GetMapping("/listDisciplines")
    public List<RefItem> listDisciplines(){
        var response = articleClient.listDisciplines();

        List<RefItem> refItems = new ArrayList<>();
        for(com.example.generated.RefItem refItem : response.getItemsList())
        {
            RefItem item = new RefItem();
            item.setId(refItem.getId());
            item.setName(refItem.getName());

            refItems.add(item);
        }

        return refItems;
    }

    @GetMapping("/listCycles")
    public List<CycleItem> listCycles(){
        var response = articleClient.listCycles();
        List<CycleItem> cycleItems = new ArrayList<>();

        for(com.example.generated.CycleItem cycleItem : response.getItemsList())
        {
            CycleItem item = new CycleItem();
            item.setId(cycleItem.getId());
            item.setName(cycleItem.getName());
            item.setYearFrom(cycleItem.getYearFrom());
            item.setYearTo(cycleItem.getYearTo());
            item.setActive(cycleItem.getIsActive());
            item.setMeinVersionId(cycleItem.getMeinVersionId());
            item.setMeinMonoVersionId(cycleItem.getMonoVersionId());
            cycleItems.add(item);
        }

        return cycleItems;
    }

}
