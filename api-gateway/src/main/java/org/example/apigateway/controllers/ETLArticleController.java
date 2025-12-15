package org.example.apigateway.controllers;


import com.example.generated.MeinJournalItem;
import org.example.apigateway.clients.ETLArticleClient;
import org.example.apigateway.config.SecurityConfig;
import org.example.apigateway.requests.articles.GetMeinJournalRequest;
import org.example.apigateway.requests.articles.ListMeinJournalRequest;
import org.example.apigateway.responses.ApiResponse;
import org.example.apigateway.responses.AsyncResponse;
import org.example.apigateway.responses.MEiNResponse;
import org.example.apigateway.responses.articles.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.example.apigateway.mappers.MeinJournalItemMapper.toMeinJournalItem;
import static org.example.apigateway.mappers.MeinVersionItemMapper.toMeinVersionItem;
import static org.example.apigateway.mappers.PageMetaMapper.toPageMeta;

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


    @GetMapping("/admin/getMeinJournal")
    public GetMeinJournalResponse adminGetMeinJournal(@RequestBody GetMeinJournalRequest request) {

        var response = ETLArticleClient.adminGetMeinJournal(request.getVersionId(), request.getJournalId());

        GetMeinJournalResponse meinResponse = new GetMeinJournalResponse();

        meinResponse.setId(response.getItem().getId());
        meinResponse.setUid(response.getItem().getUid());
        meinResponse.setTitle1(response.getItem().getTitle1());
        meinResponse.setTitle2(response.getItem().getTitle2());
        meinResponse.setIssn(response.getItem().getIssn());
        meinResponse.setIssn2(response.getItem().getIssn2());
        meinResponse.setEissn(response.getItem().getEissn());
        meinResponse.setEissn2(response.getItem().getEissn2());
        meinResponse.setPoints(response.getItem().getPoints());

        List<GetMeinJournalResponse.CodeRef> codeRefs = response.getItem()
                .getCodesList().stream()
                .map(src -> {
                    GetMeinJournalResponse.CodeRef ref  = new GetMeinJournalResponse.CodeRef();
                    ref.setCode(src.getCode());
                    ref.setName(src.getName());
                    return ref;
                }).collect(Collectors.toList());

        meinResponse.setCodes(codeRefs);

        return meinResponse;
    }

    @PostMapping("/admin/activateMeinVersion")
    public ApiResponse adminActivateMeinVersion(@RequestParam long versionId){
        var response = ETLArticleClient.adminActivateMeinVersion(versionId);


        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(response.getCode());
        apiResponse.setMessage(response.getMessage());

        return apiResponse;
    }

    @PostMapping("/admin/deactivateMeinVersion")
    public ApiResponse adminDeactivateMeinVersion(@RequestParam long versionId){
        var response = ETLArticleClient.adminDeactivateMeinVersion(versionId);

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(response.getCode());
        apiResponse.setMessage(response.getMessage());

        return apiResponse;
    }

    @DeleteMapping("/admin/deleteMeinVersion")
    public AsyncResponse adminDeleteMeinVersion(@RequestParam long versionId){
        var response = ETLArticleClient.adminDeleteMeinVersion(versionId);

       AsyncResponse deleteResponse = new AsyncResponse();
       deleteResponse.setJobId(response.getJobId());
       deleteResponse.setMessage(response.getMessage());
       return deleteResponse;
    }

    @PostMapping("/admin/recalcCycleScores")
    public AsyncResponse recalcCycleScores(@RequestParam long cycleId){

        var response = ETLArticleClient.adminRecalcCycleScores(cycleId);

        AsyncResponse recalcResponse = new AsyncResponse();
        recalcResponse.setJobId(response.getJobId());
        recalcResponse.setMessage(response.getMessage());
        return recalcResponse;
    }

    @GetMapping("/admin/getJobStatus")
    public JobStatusResponse getJobStatus(@RequestParam long jobId){
        var response = ETLArticleClient.getJobStatus(jobId);

        JobStatusResponse jobStatusResponse = new JobStatusResponse();
        jobStatusResponse.setJobId(response.getJobId());
        jobStatusResponse.setStatus(response.getStatus());
        jobStatusResponse.setType(response.getType());
        jobStatusResponse.setError(response.getError());

        return jobStatusResponse;
    }





}
