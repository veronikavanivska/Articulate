package org.example.apigateway.controllers;


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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/etl")
@RestController
public class ETLArticleController {
    private static final Logger log = LoggerFactory.getLogger(ETLArticleController.class);
    private final ETLArticleClient etlArticleClient;

    public ETLArticleController(ETLArticleClient etlArticleClient) {
        this.etlArticleClient = etlArticleClient;
    }
    @PostMapping("/admin/import")
    public ResponseEntity<MEiNResponse> importMEiN(@RequestParam("file") MultipartFile file,
                                                   @RequestParam("label") String label,
                                                   @RequestParam(value = "activateAfter", defaultValue = "true") boolean activateAfter) {
        try {
            String uid = SecurityConfig.getCurrentUserId();
            Long userId = null;
            if (uid != null) userId = Long.parseLong(uid);

            String filename = file.getOriginalFilename();
            var response = etlArticleClient.importFile(file, filename, label, userId, activateAfter);

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
            var response = etlArticleClient.adminGetActiveMeinVersion();
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
            var response = etlArticleClient.adminGetMeinVersion(versionId);
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
    public ResponseEntity<ListMeinJournalResponse> listMeinJournal(@RequestBody ListMeinJournalRequest request) {
        try {
            Long versionId = request.getVersionId() == null ? 0 : request.getVersionId();
            String title = request.getTitle() == null ? "" : request.getTitle().trim();
            int page = request.getPage() == null ? 0 : Math.max(request.getPage(), 0);
            int size = request.getSize() == null ? 20 : Math.min(Math.max(request.getSize(), 1), 100);
            String sortDir = request.getSortDir() == null ? "asc" : request.getSortDir().trim();

            var response = etlArticleClient.adminListMeinJournals(versionId, page, size, sortDir ,title);

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
            var response = etlArticleClient.adminGetMeinJournal(request.getVersionId(), request.getJournalId());
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
            var response = etlArticleClient.adminActivateMeinVersion(versionId);
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
            var response = etlArticleClient.adminDeactivateMeinVersion(versionId);
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
            var response = etlArticleClient.adminDeleteMeinVersion(versionId);
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
            var response = etlArticleClient.adminRecalcCycleScores(cycleId);
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
            var response = etlArticleClient.getJobStatus(jobId);
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
        var response = etlArticleClient.adminListMeinVersions(request.getPage(), request.getSize(), request.getSortDir());

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