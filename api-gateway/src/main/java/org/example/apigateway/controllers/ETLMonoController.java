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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//@RestController
//@RequestMapping("/etl")
//public class ETLMonoController {
//
//    @PostMapping("/admin/importPDF")
//    public MEiNResponse importPDF(@RequestParam("file") MultipartFile file,
//                                   @RequestParam("label") String label){
//        Long userId =  Long.parseLong(SecurityConfig.getCurrentUserId());
//        var response = ETLMonoClient.importFile(file, file.getName(), label, userId);
//
//        MEiNResponse meinResponse = new MEiNResponse();
//        meinResponse.setVersion_id(response.getVersionId());
//        meinResponse.setAlreadyImported(response.getAlreadyImported());
//
//        return meinResponse;
//    }
//
//    @GetMapping("/admin/getMeinMonoVersion")
//    public MeinMonoVersionItem getMeinMonoVersionItem(@RequestParam("versionId") Long versionId){
//        var response = ETLMonoClient.adminGetMeinMonoVersion(versionId);
//
//        com.example.generated.MeinMonoVersionItem versionItem = response.getVersion();
//        MeinMonoVersionItem meinMonoVersionItem = MeinMonoVersionItemMapper.map(versionItem);
//
//        return meinMonoVersionItem;
//    }
//
//     @GetMapping("/admin/getMeinMonoPublishers")
//     public MeinMonoPublisherItem getMeinMonoPublishersItem(@RequestParam("publisherId") Long publisherId){
//        var response = ETLMonoClient.adminGetMeinMonoPublisher(publisherId);
//
//        com.example.generated.MeinMonoPublisherItem publisher = response.getPublisher();
//        MeinMonoPublisherItem item = MeinMonoPublisherMapper.map(publisher);
//
//        return item;
//     }
//
//    @PostMapping("/admin/listMeinMonoVerions")
//    public ListMonoVersionResponse listMeinMonoVerions(@RequestBody ListMeinMonoVersionsRequest request){
//        var response = ETLMonoClient.adminListMeinMonoVersions(request.getPage(),request.getSize(),request.getSortDir());
//
//        ListMonoVersionResponse listMonoVersionResponse = new ListMonoVersionResponse();
//
//        List<MeinMonoVersionItem> list = new ArrayList<>();
//        for(com.example.generated.MeinMonoVersionItem item : response.getItemsList()){
//            MeinMonoVersionItem meinItem = MeinMonoVersionItemMapper.map(item);
//            list.add(meinItem);
//        }
//
//        PageMeta page = PageMetaMapper.toPageMeta(response.getPage());
//
//        listMonoVersionResponse.setItems(list);
//        listMonoVersionResponse.setPageMeta(page);
//
//        return listMonoVersionResponse;
//    }
//
//    @PostMapping("/admin/listMeinMonoPublishers")
//    public ListMonoPublishersResponse listMeinMonoPublishers(@RequestBody ListMeinMonoPublishersRequest request){
//        var response = ETLMonoClient.adminListMeinMonoPublishers(request.getVersionId(), request.getPage(), request.getSize(), request.getSortDir());
//
//        ListMonoPublishersResponse listMonoPublishersResponse = new ListMonoPublishersResponse();
//
//        List<MeinMonoPublisherItem> list = new ArrayList<>();
//        for(com.example.generated.MeinMonoPublisherItem item : response.getItemsList()){
//            MeinMonoPublisherItem meinItem = MeinMonoPublisherMapper.map(item);
//            list.add(meinItem);
//        }
//
//        listMonoPublishersResponse.setItems(list);
//
//        PageMeta page = PageMetaMapper.toPageMeta(response.getPageMeta());
//        listMonoPublishersResponse.setPageMeta(page);
//
//        return listMonoPublishersResponse;
//    }
//
//    @DeleteMapping("/admin/deleteMeinMonoVersion")
//    public AsyncResponse deleteMeinMonoVersion(@RequestParam("versionId") long versionId){
//        var response = ETLMonoClient.deleteMeinMonoVersion(versionId);
//
//        AsyncResponse asyncResponse = new AsyncResponse();
//        asyncResponse.setJobId(response.getJobId());
//        asyncResponse.setMessage(response.getMessage());
//        return asyncResponse;
//    }
//
//    @GetMapping("/admin/recalculateMonoPoints")
//    public AsyncResponse recalculateMonoPoints(@RequestParam("cycleId") long cycleId){
//        var response = ETLMonoClient.adminRecalcMonoCycleScores(cycleId);
//
//        AsyncResponse asyncResponse = new AsyncResponse();
//        asyncResponse.setJobId(response.getJobId());
//        asyncResponse.setMessage(response.getMessage());
//        return asyncResponse;
//
//    }
//
//}
@RestController
@RequestMapping("/etl")
public class ETLMonoController {
    private static final Logger log = LoggerFactory.getLogger(ETLMonoController.class);

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
            var response = ETLMonoClient.importFile(file, filename, label, userId);

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
            var response = ETLMonoClient.adminGetMeinMonoVersion(versionId);
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
            var response = ETLMonoClient.adminGetMeinMonoPublisher(publisherId);
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
            var response = ETLMonoClient.adminListMeinMonoVersions(request.getPage(), request.getSize(), request.getSortDir());

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
            var response = ETLMonoClient.adminListMeinMonoPublishers(request.getVersionId(), request.getPage(), request.getSize(), request.getSortDir());

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
            var response = ETLMonoClient.deleteMeinMonoVersion(versionId);
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
            var response = ETLMonoClient.adminRecalcMonoCycleScores(cycleId);
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