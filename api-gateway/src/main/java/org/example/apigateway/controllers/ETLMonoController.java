package org.example.apigateway.controllers;

import org.example.apigateway.clients.ETLMonoClient;
import org.example.apigateway.config.SecurityConfig;
import org.example.apigateway.mappers.MeinMonoPublisherMapper;
import org.example.apigateway.mappers.MeinMonoVersionItemMapper;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/etl")
public class ETLMonoController {
    private static final Logger log = LoggerFactory.getLogger(ETLMonoController.class);
    private final ETLMonoClient etlClient;

    public ETLMonoController(ETLMonoClient etlClient) {
        this.etlClient = etlClient;
    }
    @PostMapping("/admin/importPDF")
    public ResponseEntity<MEiNResponse> importPDF(@RequestParam("file") MultipartFile file,
                                                  @RequestParam("label") String label) {
        try {
            String uid = SecurityConfig.getCurrentUserId();
            Long userId = null;
            if (uid != null) {
                userId = Long.parseLong(uid);
            }
            String filename = file.getOriginalFilename();
            var response = etlClient.importFile(file, filename, label, userId);

            MEiNResponse meinResponse = new MEiNResponse();
            meinResponse.setVersion_id(response.getVersionId());
            meinResponse.setAlreadyImported(response.getAlreadyImported());
            return ResponseEntity.ok(meinResponse);
        } catch (Exception e) {
            log.error("importPDF error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/admin/getMeinMonoVersion")
    public ResponseEntity<MeinMonoVersionItem> getMeinMonoVersionItem(@RequestParam("versionId") Long versionId) {
        try {
            var response = etlClient.adminGetMeinMonoVersion(versionId);
            MeinMonoVersionItem meinMonoVersionItem = MeinMonoVersionItemMapper.map(response.getVersion());
            return ResponseEntity.ok(meinMonoVersionItem);
        } catch (Exception e) {
            log.error("getMeinMonoVersion error for versionId=" + versionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/admin/getMeinMonoPublisher")
    public ResponseEntity<MeinMonoPublisherItem> getMeinMonoPublisherItem(@RequestParam("publisherId") Long publisherId) {
        try {
            var response = etlClient.adminGetMeinMonoPublisher(publisherId);
            MeinMonoPublisherItem item = MeinMonoPublisherMapper.map(response.getPublisher());
            return ResponseEntity.ok(item);
        } catch (Exception e) {
            log.error("getMeinMonoPublisher error for publisherId=" + publisherId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/admin/listMeinMonoVersions")
    public ResponseEntity<ListMonoVersionResponse> listMeinMonoVersions(@RequestBody ListMeinMonoVersionsRequest request) {
        try {
            var response = etlClient.adminListMeinMonoVersions(request.getPage(), request.getSize(), request.getSortDir());

            List<MeinMonoVersionItem> list = response.getItemsList().stream()
                    .map(MeinMonoVersionItemMapper::map)
                    .collect(Collectors.toList());

            PageMeta page = PageMetaMapper.toPageMeta(response.getPage());

            ListMonoVersionResponse listMonoVersionResponse = new ListMonoVersionResponse();
            listMonoVersionResponse.setItems(list);
            listMonoVersionResponse.setPageMeta(page);

            return ResponseEntity.ok(listMonoVersionResponse);
        } catch (Exception e) {
            log.error("listMeinMonoVersions error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/admin/listMeinMonoPublishers")
    public ResponseEntity<ListMonoPublishersResponse> listMeinMonoPublishers(@RequestBody ListMeinMonoPublishersRequest request) {
        try {
            Long versionId = request.getVersionId() == null ? 0 : request.getVersionId();
            String title = request.getTitle() == null ? "" : request.getTitle().trim();
            int page = request.getPage() == null ? 0 : Math.max(request.getPage(), 0);
            int size = request.getSize() == null ? 20 : Math.min(Math.max(request.getSize(), 1), 100);
            String sortDir = request.getSortDir() == null ? "asc" : request.getSortDir().trim();

            //var response = ProfilesClient.allProfiles(title, page, size, sortBy, sortDir);
            var response = etlClient.adminListMeinMonoPublishers(versionId, page, size, sortDir, title);

            List<MeinMonoPublisherItem> list = response.getItemsList().stream()
                    .map(MeinMonoPublisherMapper::map)
                    .collect(Collectors.toList());

            ListMonoPublishersResponse listMonoPublishersResponse = new ListMonoPublishersResponse();
            listMonoPublishersResponse.setItems(list);
            listMonoPublishersResponse.setPageMeta(PageMetaMapper.toPageMeta(response.getPageMeta()));

            return ResponseEntity.ok(listMonoPublishersResponse);
        } catch (Exception e) {
            log.error("listMeinMonoPublishers error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/admin/deleteMeinMonoVersion")
    public ResponseEntity<AsyncResponse> deleteMeinMonoVersion(@RequestParam("versionId") long versionId) {
        try {
            var response = etlClient.deleteMeinMonoVersion(versionId);
            AsyncResponse asyncResponse = new AsyncResponse();
            asyncResponse.setJobId(response.getJobId());
            asyncResponse.setMessage(response.getMessage());
            return ResponseEntity.ok(asyncResponse);
        } catch (Exception e) {
            log.error("deleteMeinMonoVersion error for versionId=" + versionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/admin/recalculateMonoPoints")
    public ResponseEntity<AsyncResponse> recalculateMonoPoints(@RequestParam("cycleId") long cycleId) {
        try {
            var response = etlClient.adminRecalcMonoCycleScores(cycleId);
            AsyncResponse asyncResponse = new AsyncResponse();
            asyncResponse.setJobId(response.getJobId());
            asyncResponse.setMessage(response.getMessage());
            return ResponseEntity.ok(asyncResponse);
        } catch (Exception e) {
            log.error("recalculateMonoPoints error for cycleId=" + cycleId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}