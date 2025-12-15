package org.example.apigateway.controllers;

import org.example.apigateway.clients.ETLArticleClient;
import org.example.apigateway.clients.ETLMonoClient;
import org.example.apigateway.config.SecurityConfig;
import org.example.apigateway.mappers.MeinMonoPublisherMapper;
import org.example.apigateway.mappers.MeinMonoVersionItemMapper;
import org.example.apigateway.mappers.MeinVersionItemMapper;
import org.example.apigateway.mappers.PageMetaMapper;
import org.example.apigateway.requests.articles.ListMeinMonoPublishersRequest;
import org.example.apigateway.requests.articles.ListMeinMonoVersionsRequest;
import org.example.apigateway.responses.AsyncResponse;
import org.example.apigateway.responses.MEiNResponse;
import org.example.apigateway.responses.articles.PageMeta;
import org.example.apigateway.responses.mono.ListMonoPublishersResponse;
import org.example.apigateway.responses.mono.ListMonoVersionResponse;
import org.example.apigateway.responses.mono.MeinMonoPublisherItem;
import org.example.apigateway.responses.mono.MeinMonoVersionItem;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/etl")
public class ETLMonoController {

    @PostMapping("/admin/importPDF")
    public MEiNResponse importPDF(@RequestParam("file") MultipartFile file,
                                   @RequestParam("label") String label){
        Long userId =  Long.parseLong(SecurityConfig.getCurrentUserId());
        var response = ETLMonoClient.importFile(file, file.getName(), label, userId);

        MEiNResponse meinResponse = new MEiNResponse();
        meinResponse.setVersion_id(response.getVersionId());
        meinResponse.setAlreadyImported(response.getAlreadyImported());

        return meinResponse;
    }

    @GetMapping("/admin/getMeinMonoVersion")
    public MeinMonoVersionItem getMeinMonoVersionItem(@RequestParam("versionId") Long versionId){
        var response = ETLMonoClient.adminGetMeinMonoVersion(versionId);

        com.example.generated.MeinMonoVersionItem versionItem = response.getVersion();
        MeinMonoVersionItem meinMonoVersionItem = MeinMonoVersionItemMapper.map(versionItem);

        return meinMonoVersionItem;
    }

     @GetMapping("/admin/getMeinMonoPublishers")
     public MeinMonoPublisherItem getMeinMonoPublishersItem(@RequestParam("publisherId") Long publisherId){
        var response = ETLMonoClient.adminGetMeinMonoPublisher(publisherId);

        com.example.generated.MeinMonoPublisherItem publisher = response.getPublisher();
        MeinMonoPublisherItem item = MeinMonoPublisherMapper.map(publisher);

        return item;
     }

    @GetMapping("/admin/listMeinMonoVerions")
    public ListMonoVersionResponse listMeinMonoVerions(@RequestBody ListMeinMonoVersionsRequest request){
        var response = ETLMonoClient.adminListMeinMonoVersions(request.getPage(),request.getSize(),request.getSortDir());

        ListMonoVersionResponse listMonoVersionResponse = new ListMonoVersionResponse();

        List<MeinMonoVersionItem> list = new ArrayList<>();
        for(com.example.generated.MeinMonoVersionItem item : response.getItemsList()){
            MeinMonoVersionItem meinItem = MeinMonoVersionItemMapper.map(item);
            list.add(meinItem);
        }

        PageMeta page = PageMetaMapper.toPageMeta(response.getPage());

        listMonoVersionResponse.setItems(list);
        listMonoVersionResponse.setPageMeta(page);

        return listMonoVersionResponse;
    }

    @GetMapping("/admin/listMeinMonoPublishers")
    public ListMonoPublishersResponse listMeinMonoPublishers(@RequestBody ListMeinMonoPublishersRequest request){
        var response = ETLMonoClient.adminListMeinMonoPublishers(request.getVersionId(), request.getPage(), request.getSize(), request.getSortDir());

        ListMonoPublishersResponse listMonoPublishersResponse = new ListMonoPublishersResponse();

        List<MeinMonoPublisherItem> list = new ArrayList<>();
        for(com.example.generated.MeinMonoPublisherItem item : response.getItemsList()){
            MeinMonoPublisherItem meinItem = MeinMonoPublisherMapper.map(item);
            list.add(meinItem);
        }

        listMonoPublishersResponse.setItems(list);

        PageMeta page = PageMetaMapper.toPageMeta(response.getPageMeta());
        listMonoPublishersResponse.setPageMeta(page);

        return listMonoPublishersResponse;
    }

    @DeleteMapping("/admin/deleteMeinMonoVersion")
    public AsyncResponse deleteMeinMonoVersion(@RequestParam("versionId") long versionId){
        var response = ETLMonoClient.deleteMeinMonoVersion(versionId);

        AsyncResponse asyncResponse = new AsyncResponse();
        asyncResponse.setJobId(response.getJobId());
        asyncResponse.setMessage(response.getMessage());
        return asyncResponse;
    }

    @GetMapping("/admin/recalculateMonoPoints")
    public AsyncResponse recalculateMonoPoints(@RequestParam("cycleId") long cycleId){
        var response = ETLMonoClient.adminRecalcMonoCycleScores(cycleId);

        AsyncResponse asyncResponse = new AsyncResponse();
        asyncResponse.setJobId(response.getJobId());
        asyncResponse.setMessage(response.getMessage());
        return asyncResponse;

    }

}
