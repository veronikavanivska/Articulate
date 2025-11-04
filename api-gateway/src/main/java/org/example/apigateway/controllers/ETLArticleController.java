package org.example.apigateway.controllers;


import com.example.generated.MeinJournalItem;
import org.example.apigateway.clients.ETLArticleClient;
import org.example.apigateway.config.SecurityConfig;
import org.example.apigateway.requests.articles.ListMeinJournalRequest;
import org.example.apigateway.responses.MEiNResponse;
import org.example.apigateway.responses.articles.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.example.apigateway.config.MeinJournalItemMapper.toMeinJournalItem;
import static org.example.apigateway.config.MeinVersionItemMapper.toMeinVersionItem;
import static org.example.apigateway.config.PageMetaMapper.toPageMeta;

@RequestMapping("/etl")
@RestController
public class ETLArticleController {

    @PostMapping("/admin/import")
    public MEiNResponse importMEiN(@RequestParam("file") MultipartFile file,
                                   @RequestParam("label") String label,
                                   @RequestParam(value = "activateAfter", defaultValue = "true") boolean activateAfter
    ){
        Long userId =  Long.parseLong(SecurityConfig.getCurrentUserId());
        var response = ETLArticleClient.importFile(file, file.getName(), label, userId, activateAfter);

        MEiNResponse meinResponse = new MEiNResponse();
        meinResponse.setVersion_id(response.getVersionId());
        meinResponse.setAlreadyImported(response.getAlreadyImported());

        return meinResponse;
    }

    @GetMapping("/admin/activeMeinVersion")
    public GetActiveMeinVersionResponse getActiveMeinVersion(){

        var response = ETLArticleClient.adminGetActiveMeinVersion();
        GetActiveMeinVersionResponse meinResponse = new GetActiveMeinVersionResponse();
        meinResponse.setMeinVersion(toMeinVersionItem(response.getVersion()));
        return meinResponse;
    }

    @GetMapping("/admin/meinVersion")
    public GetMeinVersionItem getMeinVersion(@RequestParam long versionId){

        var response = ETLArticleClient.adminGetMeinVersion(versionId);
        GetMeinVersionItem meinResponse = new GetMeinVersionItem();
        meinResponse.setMeinVersion(toMeinVersionItem(response.getVersion()));
        meinResponse.setDistinctCodes(response.getDistinctCodes());
        meinResponse.setDistinctIssn(response.getDistinctIssn());
        meinResponse.setDistinctIssn(response.getDistinctIssn());

        return meinResponse;
    }

    @GetMapping("/admin/listMeinJournals")
    public ListMeinJournalResponse listMeinJournal(@RequestBody ListMeinJournalRequest request) {
        var response = ETLArticleClient.adminListMeinJournals(request.getVersionId(), request.getPage(), request.getSize(), request.getSortDir());

        ListMeinJournalResponse meinResponse = new ListMeinJournalResponse();

        List<org.example.apigateway.responses.articles.MeinJournalItem>  listOfJournals = new ArrayList<>();

        for(MeinJournalItem item : response.getItemsList()){
            org.example.apigateway.responses.articles.MeinJournalItem d = toMeinJournalItem(item);
            listOfJournals.add(d);
        }


        meinResponse.setMeinJournals(listOfJournals);
        meinResponse.setPageMeta(toPageMeta(response.getPage()));

        return meinResponse;
    }





}
