package org.example.apigateway.controllers;


import com.example.generated.MeinJournalItem;
import org.example.apigateway.clients.ETLArticleClient;
import org.example.apigateway.config.SecurityConfig;
import org.example.apigateway.mappers.MeinJournalItemMapper;
import org.example.apigateway.mappers.MeinVersionItemMapper;
import org.example.apigateway.mappers.PageMetaMapper;
import org.example.apigateway.requests.ListSmthRequest;
import org.example.apigateway.requests.articles.GetMeinJournalRequest;
import org.example.apigateway.requests.articles.ListMeinJournalRequest;
import org.example.apigateway.responses.ApiResponse;
import org.example.apigateway.responses.AsyncResponse;
import org.example.apigateway.responses.MEiNResponse;
import org.example.apigateway.responses.articles.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.example.apigateway.mappers.MeinJournalItemMapper.toMeinJournalItem;
import static org.example.apigateway.mappers.MeinVersionItemMapper.toMeinVersionItem;
import static org.example.apigateway.mappers.PageMetaMapper.toPageMeta;

//@RequestMapping("/etl")
//@RestController
//public class ETLArticleController {
//
//    @PostMapping("/admin/import")
//    public MEiNResponse importMEiN(@RequestParam("file") MultipartFile file,
//                                   @RequestParam("label") String label,
//                                   @RequestParam(value = "activateAfter", defaultValue = "true") boolean activateAfter
//    ){
//        Long userId =  Long.parseLong(SecurityConfig.getCurrentUserId());
//        var response = ETLArticleClient.importFile(file, file.getName(), label, userId, activateAfter);
//
//        MEiNResponse meinResponse = new MEiNResponse();
//        meinResponse.setVersion_id(response.getVersionId());
//        meinResponse.setAlreadyImported(response.getAlreadyImported());
//
//        return meinResponse;
//    }
//
//    @GetMapping("/admin/activeMeinVersion")
//    public GetActiveMeinVersionResponse getActiveMeinVersion(){
//
//        var response = ETLArticleClient.adminGetActiveMeinVersion();
//        GetActiveMeinVersionResponse meinResponse = new GetActiveMeinVersionResponse();
//        meinResponse.setMeinVersion(toMeinVersionItem(response.getVersion()));
//        return meinResponse;
//    }
//
//    @GetMapping("/admin/meinVersion")
//    public GetMeinVersionItem getMeinVersion(@RequestParam long versionId){
//
//        var response = ETLArticleClient.adminGetMeinVersion(versionId);
//        GetMeinVersionItem meinResponse = new GetMeinVersionItem();
//        meinResponse.setMeinVersion(toMeinVersionItem(response.getVersion()));
//        meinResponse.setDistinctCodes(response.getDistinctCodes());
//        meinResponse.setDistinctIssn(response.getDistinctIssn());
//        meinResponse.setDistinctEissn(response.getDistinctEissn());
//
//        return meinResponse;
//    }
//
//    @PostMapping("/admin/listMeinJournals")
//    public ListMeinJournalResponse listMeinJournal(@RequestBody ListMeinJournalRequest request) {
//        var response = ETLArticleClient.adminListMeinJournals(request.getVersionId(), request.getPage(), request.getSize(), request.getSortDir());
//
//        ListMeinJournalResponse meinResponse = new ListMeinJournalResponse();
//
//        List<org.example.apigateway.responses.articles.MeinJournalItem>  listOfJournals = new ArrayList<>();
//
//        for(MeinJournalItem item : response.getItemsList()){
//            org.example.apigateway.responses.articles.MeinJournalItem d = toMeinJournalItem(item);
//            listOfJournals.add(d);
//        }
//
//
//        meinResponse.setMeinJournals(listOfJournals);
//        meinResponse.setPageMeta(toPageMeta(response.getPage()));
//
//        return meinResponse;
//    }
//
//
//    @PostMapping("/admin/getMeinJournal")
//    public GetMeinJournalResponse adminGetMeinJournal(@RequestBody GetMeinJournalRequest request) {
//
//        var response = ETLArticleClient.adminGetMeinJournal(request.getVersionId(), request.getJournalId());
//
//        GetMeinJournalResponse meinResponse = new GetMeinJournalResponse();
//
//        meinResponse.setId(response.getItem().getId());
//        meinResponse.setUid(response.getItem().getUid());
//        meinResponse.setTitle1(response.getItem().getTitle1());
//        meinResponse.setTitle2(response.getItem().getTitle2());
//        meinResponse.setIssn(response.getItem().getIssn());
//        meinResponse.setIssn2(response.getItem().getIssn2());
//        meinResponse.setEissn(response.getItem().getEissn());
//        meinResponse.setEissn2(response.getItem().getEissn2());
//        meinResponse.setPoints(response.getItem().getPoints());
//
//        List<GetMeinJournalResponse.CodeRef> codeRefs = response.getItem()
//                .getCodesList().stream()
//                .map(src -> {
//                    GetMeinJournalResponse.CodeRef ref  = new GetMeinJournalResponse.CodeRef();
//                    ref.setCode(src.getCode());
//                    ref.setName(src.getName());
//                    return ref;
//                }).collect(Collectors.toList());
//
//        meinResponse.setCodes(codeRefs);
//
//        return meinResponse;
//    }
//
//    @PostMapping("/admin/activateMeinVersion")
//    public ApiResponse adminActivateMeinVersion(@RequestParam long versionId){
//        var response = ETLArticleClient.adminActivateMeinVersion(versionId);
//
//
//        ApiResponse apiResponse = new ApiResponse();
//        apiResponse.setCode(response.getCode());
//        apiResponse.setMessage(response.getMessage());
//
//        return apiResponse;
//    }
//
//    @PostMapping("/admin/deactivateMeinVersion")
//    public ApiResponse adminDeactivateMeinVersion(@RequestParam long versionId){
//        var response = ETLArticleClient.adminDeactivateMeinVersion(versionId);
//
//        ApiResponse apiResponse = new ApiResponse();
//        apiResponse.setCode(response.getCode());
//        apiResponse.setMessage(response.getMessage());
//
//        return apiResponse;
//    }
//
//    @DeleteMapping("/admin/deleteMeinVersion")
//    public AsyncResponse adminDeleteMeinVersion(@RequestParam long versionId){
//        var response = ETLArticleClient.adminDeleteMeinVersion(versionId);
//
//       AsyncResponse deleteResponse = new AsyncResponse();
//       deleteResponse.setJobId(response.getJobId());
//       deleteResponse.setMessage(response.getMessage());
//       return deleteResponse;
//    }
//
//    @PostMapping("/admin/recalcCycleScores")
//    public AsyncResponse recalcCycleScores(@RequestParam long cycleId){
//
//        var response = ETLArticleClient.adminRecalcCycleScores(cycleId);
//
//        AsyncResponse recalcResponse = new AsyncResponse();
//        recalcResponse.setJobId(response.getJobId());
//        recalcResponse.setMessage(response.getMessage());
//        return recalcResponse;
//    }
//
//    @GetMapping("/admin/getJobStatus")
//    public JobStatusResponse getJobStatus(@RequestParam long jobId){
//        var response = ETLArticleClient.getJobStatus(jobId);
//
//        JobStatusResponse jobStatusResponse = new JobStatusResponse();
//        jobStatusResponse.setJobId(response.getJobId());
//        jobStatusResponse.setStatus(response.getStatus());
//        jobStatusResponse.setType(response.getType());
//        jobStatusResponse.setError(response.getError());
//
//        return jobStatusResponse;
//    }
//
//
//
//
//
//}
@RequestMapping("/etl")
@RestController
public class ETLArticleController {
    private static final Logger log = LoggerFactory.getLogger(ETLArticleController.class);

    @PostMapping("/admin/import")
    public ResponseEntity<MEiNResponse> importMEiN(@RequestParam("file") MultipartFile file,
                                                   @RequestParam("label") String label,
                                                   @RequestParam(value = "activateAfter", defaultValue = "true") boolean activateAfter) {
        try {
            String uid = SecurityConfig.getCurrentUserId();
            Long userId = null;
            if (uid != null) userId = Long.parseLong(uid);

            String filename = file.getOriginalFilename();
            var response = ETLArticleClient.importFile(file, filename, label, userId, activateAfter);

            MEiNResponse meinResponse = new MEiNResponse();
            meinResponse.setVersion_id(response.getVersionId());
            meinResponse.setAlreadyImported(response.getAlreadyImported());
            return ResponseEntity.ok(meinResponse);
        } catch (Exception e) {
            log.error("importMEiN error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/admin/activeMeinVersion")
    public ResponseEntity<GetActiveMeinVersionResponse> getActiveMeinVersion() {
        try {
            var response = ETLArticleClient.adminGetActiveMeinVersion();
            GetActiveMeinVersionResponse meinResponse = new GetActiveMeinVersionResponse();
            meinResponse.setMeinVersion(MeinVersionItemMapper.toMeinVersionItem(response.getVersion()));
            return ResponseEntity.ok(meinResponse);
        } catch (Exception e) {
            log.error("getActiveMeinVersion error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/admin/meinVersion")
    public ResponseEntity<GetMeinVersionItem> getMeinVersion(@RequestParam long versionId) {
        try {
            var response = ETLArticleClient.adminGetMeinVersion(versionId);
            GetMeinVersionItem meinResponse = new GetMeinVersionItem();
            meinResponse.setMeinVersion(MeinVersionItemMapper.toMeinVersionItem(response.getVersion()));
            meinResponse.setDistinctCodes(response.getDistinctCodes());
            meinResponse.setDistinctIssn(response.getDistinctIssn());
            meinResponse.setDistinctEissn(response.getDistinctEissn());
            return ResponseEntity.ok(meinResponse);
        } catch (Exception e) {
            log.error("getMeinVersion error for versionId=" + versionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/admin/listMeinJournals")
    public ResponseEntity<ListMeinJournalResponse> listMeinJournal(@RequestBody ListMeinJournalRequest request, Sort sort) {
        try {
            Long versionId = request.getVersionId() == null ? 0 : request.getVersionId();
            String title = request.getTitle() == null ? "" : request.getTitle().trim();
            int page = request.getPage() == null ? 0 : Math.max(request.getPage(), 0);
            int size = request.getSize() == null ? 20 : Math.min(Math.max(request.getSize(), 1), 100);
            String sortDir = request.getSortDir() == null ? "asc" : request.getSortDir().trim();

            var response = ETLArticleClient.adminListMeinJournals(versionId, page, size, sortDir ,title);

            List<org.example.apigateway.responses.articles.MeinJournalItem> listOfJournals = response.getItemsList().stream()
                    .map(MeinJournalItemMapper::toMeinJournalItem)
                    .collect(Collectors.toList());

            ListMeinJournalResponse meinResponse = new ListMeinJournalResponse();
            meinResponse.setMeinJournals(listOfJournals);
            meinResponse.setPageMeta(   PageMetaMapper.toPageMeta(response.getPage()));
            return ResponseEntity.ok(meinResponse);
        } catch (Exception e) {
            log.error("listMeinJournal error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/admin/getMeinJournal")
    public ResponseEntity<GetMeinJournalResponse> adminGetMeinJournal(@RequestBody GetMeinJournalRequest request) {
        try {
            var response = ETLArticleClient.adminGetMeinJournal(request.getVersionId(), request.getJournalId());
            var item = response.getItem();

            GetMeinJournalResponse meinResponse = new GetMeinJournalResponse();
            meinResponse.setId(item.getId());
            meinResponse.setUid(item.getUid());
            meinResponse.setTitle1(item.getTitle1());
            meinResponse.setTitle2(item.getTitle2());
            meinResponse.setIssn(item.getIssn());
            meinResponse.setIssn2(item.getIssn2());
            meinResponse.setEissn(item.getEissn());
            meinResponse.setEissn2(item.getEissn2());
            meinResponse.setPoints(item.getPoints());

            List<GetMeinJournalResponse.CodeRef> codeRefs = item.getCodesList().stream()
                    .map(src -> {
                        GetMeinJournalResponse.CodeRef ref = new GetMeinJournalResponse.CodeRef();
                        ref.setCode(src.getCode());
                        ref.setName(src.getName());
                        return ref;
                    }).collect(Collectors.toList());

            meinResponse.setCodes(codeRefs);
            return ResponseEntity.ok(meinResponse);
        } catch (Exception e) {
            log.error("adminGetMeinJournal error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/admin/activateMeinVersion")
    public ResponseEntity<ApiResponse> adminActivateMeinVersion(@RequestParam long versionId) {
        try {
            var response = ETLArticleClient.adminActivateMeinVersion(versionId);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setCode(response.getCode());
            apiResponse.setMessage(response.getMessage());
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("adminActivateMeinVersion error for versionId=" + versionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/admin/deactivateMeinVersion")
    public ResponseEntity<ApiResponse> adminDeactivateMeinVersion(@RequestParam long versionId) {
        try {
            var response = ETLArticleClient.adminDeactivateMeinVersion(versionId);
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setCode(response.getCode());
            apiResponse.setMessage(response.getMessage());
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("adminDeactivateMeinVersion error for versionId=" + versionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/admin/deleteMeinVersion")
    public ResponseEntity<AsyncResponse> adminDeleteMeinVersion(@RequestParam long versionId) {
        try {
            var response = ETLArticleClient.adminDeleteMeinVersion(versionId);
            AsyncResponse deleteResponse = new AsyncResponse();
            deleteResponse.setJobId(response.getJobId());
            deleteResponse.setMessage(response.getMessage());
            return ResponseEntity.ok(deleteResponse);
        } catch (Exception e) {
            log.error("adminDeleteMeinVersion error for versionId=" + versionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/admin/recalcCycleScores")
    public ResponseEntity<AsyncResponse> recalcCycleScores(@RequestParam long cycleId) {
        try {
            var response = ETLArticleClient.adminRecalcCycleScores(cycleId);
            AsyncResponse recalcResponse = new AsyncResponse();
            recalcResponse.setJobId(response.getJobId());
            recalcResponse.setMessage(response.getMessage());
            return ResponseEntity.ok(recalcResponse);
        } catch (Exception e) {
            log.error("recalcCycleScores error for cycleId=" + cycleId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/admin/getJobStatus")
    public ResponseEntity<JobStatusResponse> getJobStatus(@RequestParam long jobId) {
        try {
            var response = ETLArticleClient.getJobStatus(jobId);
            JobStatusResponse jobStatusResponse = new JobStatusResponse();
            jobStatusResponse.setJobId(response.getJobId());
            jobStatusResponse.setStatus(response.getStatus());
            jobStatusResponse.setType(response.getType());
            jobStatusResponse.setError(response.getError());
            return ResponseEntity.ok(jobStatusResponse);
        } catch (Exception e) {
            log.error("getJobStatus error for jobId=" + jobId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/admin/listMeinVersions")
    public ResponseEntity<ListMeinVersionResponse> adminListMeinVersions(@RequestBody ListSmthRequest request) {
        var response = ETLArticleClient.adminListMeinVersions(request.getPage(), request.getSize(), request.getSortDir());

        ListMeinVersionResponse listMeinVersionResponse = new ListMeinVersionResponse();


        List<MeinVersionItem> items = new ArrayList<>();

        for(com.example.generated.MeinVersionItem i : response.getItemsList()){
            MeinVersionItem item = MeinVersionItemMapper.toMeinVersionItem(i);
            items.add(item);
        }

        PageMeta pageMeta = PageMetaMapper.toPageMeta(response.getPage());
        listMeinVersionResponse.setPageMeta(pageMeta);
        listMeinVersionResponse.setItem(items);

        return ResponseEntity.ok(listMeinVersionResponse);
    }


}