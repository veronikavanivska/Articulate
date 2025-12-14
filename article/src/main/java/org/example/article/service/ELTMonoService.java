package org.example.article.service;

import com.example.generated.*;
import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.example.article.ETL.MonoETLService;
import org.example.article.entities.AsyncJob;
import org.example.article.entities.MEiN.monographs.MeinMonoPublisher;
import org.example.article.entities.MEiN.monographs.MeinMonoVersion;
import org.example.article.helpers.CommutePoints;
import org.example.article.repositories.*;
import org.example.article.service.Async.AsyncMeinService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ELTMonoService extends ETLMonoServiceGrpc.ETLMonoServiceImplBase {

    private final MonoETLService monoETLService;
    private final MeinMonoVersionRepository meinMonoVersionRepository;
    private final MeinMonoPublisherRepository meinMonoPublisherRepository;
    private final EvalCycleRepository evalCycleRepository;
    private final MonographicRepository monographicRepository;
    private final MonographChapterRepository monographChapterRepository;
    private final CommutePoints commutePoints;
    private final AsyncJobRepository asyncJobRepository;
    private final AsyncMeinService asyncMeinService;

    public ELTMonoService(MonoETLService monoETLService, MeinMonoVersionRepository meinMonoVersionRepository, MeinMonoPublisherRepository meinMonoPublisherRepository, EvalCycleRepository evalCycleRepository, MonographicRepository monographicRepository, MonographChapterRepository monographChapterRepository, CommutePoints commutePoints, AsyncJobRepository asyncJobRepository, AsyncMeinService asyncMeinService) {
        this.monoETLService = monoETLService;
        this.meinMonoVersionRepository = meinMonoVersionRepository;
        this.meinMonoPublisherRepository = meinMonoPublisherRepository;
        this.evalCycleRepository = evalCycleRepository;
        this.monographicRepository = monographicRepository;
        this.monographChapterRepository = monographChapterRepository;
        this.commutePoints = commutePoints;
        this.asyncJobRepository = asyncJobRepository;
        this.asyncMeinService = asyncMeinService;
    }

    @Override
    public void importFile(ImportMEiNRequest request, StreamObserver<ImportMEiNReply> responseObserver) {

        try {
            byte[] bytes = request.getFile().toByteArray();
            String filename = request.getFilename();

            Long versionId = monoETLService.importPDF(
                    bytes,
                    filename,
                    request.getLabel(),
                    request.getImportedBy()
            );

            boolean already = (versionId == null);
            long id = already ? -1L : versionId;
            ImportMEiNReply resp = ImportMEiNReply.newBuilder()
                    .setVersionId(id)
                    .setAlreadyImported(already)
                    .build();

            responseObserver.onNext(resp);
            responseObserver.onCompleted();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void adminGetMeinMonoVersion(AdminGetMeinMonoVersionRequest request, StreamObserver<AdminGetMeinMonoVersionResponse> responseObserver) {
        Long versionId = request.getId();

        MeinMonoVersion meinMonoVersion = meinMonoVersionRepository.findById(versionId)
                .orElseThrow(() -> new RuntimeException("Mein version mono not found"));

        long countPublishers = meinMonoPublisherRepository.countPublishers(meinMonoVersion.getId());

        Instant instant = meinMonoVersion.getImportedAt();
        Timestamp time = Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();

        MeinMonoVersionItem item = MeinMonoVersionItem.newBuilder()
                .setId(meinMonoVersion.getId())
                .setLabel(meinMonoVersion.getLabel())
                .setSourceFilename(meinMonoVersion.getSourceFilename())
                .setImportedBy(meinMonoVersion.getImportedBy())
                .setImportedAt(time)
                .setPublishers(countPublishers)
                .build();

        AdminGetMeinMonoVersionResponse response = AdminGetMeinMonoVersionResponse.newBuilder()
                .setVersion(item).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();


    }

    @Override
        public void adminGetMeinMonoPublisher(AdminGetMeinMonoPublisherRequest request, StreamObserver<AdminGetMeinMonoPublisherResponse> responseObserver) {
        Long publisherId = request.getId();

        MeinMonoPublisher meinMonoPublisher = meinMonoPublisherRepository.findById(publisherId).orElseThrow(() -> new RuntimeException("Mein publisher not found"));


        MeinMonoPublisherItem item = MeinMonoPublisherItem.newBuilder()
                .setId(meinMonoPublisher.getId())
                .setTitle(meinMonoPublisher.getName())
                .setPoints(meinMonoPublisher.getPoints())
                .setLevel(meinMonoPublisher.getLevel())
                .setVersionId(meinMonoPublisher.getVersion().getId())
                .build();

        AdminGetMeinMonoPublisherResponse response = AdminGetMeinMonoPublisherResponse.newBuilder()
                .setPublisher(item).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void adminListMeinMonoVersions(AdminListMeinMonoVersionsRequest request, StreamObserver<AdminListMeinMonoVersionsResponse> responseObserver) {
        int pg = Math.max(0, request.getPage());
        int sz = request.getSize() > 0 ? Math.min(request.getSize(), 100) : 20;

        boolean desc = !"ASC".equalsIgnoreCase(request.getSortDir());
        Sort sort = Sort.by(desc ? Sort.Direction.DESC : Sort.Direction.ASC, "importedAt");
        Pageable pageable = PageRequest.of(pg, sz, sort);

        Page<MeinMonoVersion> page = meinMonoVersionRepository.findAll(pageable);

        PageMeta pageMeta = PageMeta.newBuilder()
                .setPage(page.getNumber())
                .setTotalPages(page.getTotalPages())
                .setSize(page.getSize())
                .setTotalItems(page.getTotalElements())
                .build();

        AdminListMeinMonoVersionsResponse.Builder response = AdminListMeinMonoVersionsResponse.newBuilder()
                .setPage(pageMeta);

        for(MeinMonoVersion version : page.getContent()) {

            long publisers = meinMonoPublisherRepository.countPublishers(version.getId());

            Instant instant = version.getImportedAt();
            Timestamp time = Timestamp.newBuilder()
                    .setSeconds(instant.getEpochSecond())
                    .setNanos(instant.getNano())
                    .build();


            response.addItems(MeinMonoVersionItem.newBuilder()
                    .setId(version.getId())
                    .setLabel(version.getLabel())
                    .setSourceFilename(version.getSourceFilename())
                    .setImportedBy(version.getImportedBy())
                    .setImportedAt(time)
                    .setPublishers(publisers)
                    .build());

        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void adminListMeinMonoPublishers(AdminListMeinMonoPublishersRequest request, StreamObserver<AdminListMeinMonoPublishersResponse> responseObserver) {
        long versionId = request.getVersionId();

        if(!meinMonoVersionRepository.existsById(versionId)) {
            responseObserver.onError(Status.NOT_FOUND.withDescription("Not found the mein mono version").asRuntimeException());
            return;
        }

        int pg = Math.max(0, request.getPage());
        int sz = request.getSize() > 0 ? Math.min(request.getSize(), 100) : 20;

        boolean desc = !"ASC".equalsIgnoreCase(request.getSortDir());
        Sort sort = Sort.by(desc ? Sort.Direction.DESC : Sort.Direction.ASC, "level");
        Pageable pageable = PageRequest.of(pg, sz, sort);

        Page<MeinMonoPublisher> page = meinMonoPublisherRepository.findByVersion_Id(versionId, pageable);

        PageMeta pageMeta = PageMeta.newBuilder()
                .setSize(page.getSize())
                .setPage(page.getNumber())
                .setTotalItems(page.getTotalElements())
                .setTotalPages(page.getTotalPages())
                .build();

        AdminListMeinMonoPublishersResponse.Builder response = AdminListMeinMonoPublishersResponse.newBuilder()
                .setPageMeta(pageMeta);

        for(MeinMonoPublisher publisher : page.getContent()) {
            response.addItems(MeinMonoPublisherItem.newBuilder()
                    .setId(publisher.getId())
                    .setTitle(publisher.getName())
                    .setPoints(publisher.getPoints())
                    .setLevel(publisher.getLevel())
                    .setVersionId(publisher.getVersion().getId())
                    .build());

        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
        }

//    @Override
//    public void adminDeleteMeinMonoVersion(DeleteMeinMonoVersionRequest request, StreamObserver<ApiResponse> responseObserver) {
//        long versionId = request.getId();
//
//        if(!meinMonoVersionRepository.existsById(versionId)) {
//            responseObserver.onError(Status.NOT_FOUND
//                    .withDescription("No mein version with this id").asRuntimeException());
//            return;
//        }
//
//        List<EvalCycle> cycles = evalCycleRepository.findAllByMeinMonoVersion(versionId);
//        for (EvalCycle cycle : cycles) {
//            cycle.setMeinMonoVersion(null);
//        }
//        if (!cycles.isEmpty()) {
//            evalCycleRepository.saveAll(cycles);
//        }
//        // 2. Wyczyść publikacje powiązane z tą wersją MEiN
//        // (monografie i rozdziały)
//        List<Monographic> monographics = monographicRepository.findAllByMeinMonoId(versionId);
//        for (Monographic m : monographics) {
//            m.setMeinPoints(0);
//            m.setMeinMonoId(null);
//            m.setMeinMonoPublisherId(null);
//            m.setUpdatedAt(Instant.now());
//        }
//        if (!monographics.isEmpty()) {
//            monographicRepository.saveAll(monographics);
//        }
//
//        List<MonographChapter> chapters = monographChapterRepository.findAllByMeinMonoId(versionId);
//        for (MonographChapter mc : chapters) {
//            mc.setMeinPoints(0.0);
//            mc.setMeinMonoId(null);
//            mc.setMeinMonoPublisherId(null);
//            mc.setUpdatedAt(Instant.now());
//        }
//        if (!chapters.isEmpty()) {
//            monographChapterRepository.saveAll(chapters);
//        }
//
//
//        evalCycleRepository.saveAll(cycles);
//        meinMonoVersionRepository.deleteById(versionId);
//
//
//        ApiResponse response = ApiResponse.newBuilder()
//                .setCode(200)
//                .setMessage("Successfully deleted ")
//                .build();
//
//        responseObserver.onNext(response);
//        responseObserver.onCompleted();
//
//    }
@Override
public void adminDeleteMeinMonoVersion(DeleteMeinMonoVersionRequest request,
                                       StreamObserver<DeleteMeinMonoVersionResponse> responseObserver) {
    long versionId = request.getId();

    if (!meinMonoVersionRepository.existsById(versionId)) {
        responseObserver.onError(Status.NOT_FOUND
                .withDescription("No MeinMonoVersion with this id")
                .asRuntimeException());
        return;
    }

    String businessKey = "DELETE_MEIN_MONO_VERSION:" + versionId;

    Optional<AsyncJob> existingJobOpt =
            asyncJobRepository.findFirstByTypeAndBusinessKeyAndStatusIn(
                    "DELETE_MEIN_MONO_VERSION",
                    businessKey,
                    List.of(AsyncJob.Status.QUEUED, AsyncJob.Status.RUNNING)
            );

    AsyncJob job;
    if (existingJobOpt.isPresent()) {
        job = existingJobOpt.get();
    } else {
        job = new AsyncJob();
        job.setType("DELETE_MEIN_MONO_VERSION");
        job.setBusinessKey(businessKey);
        job.setStatus(AsyncJob.Status.QUEUED);
        job.setProgressPercent(0);
        job.setPhase("Queued");
        job.setMessage("Waiting to start MeinMonoVersion deletion");
        job.setRequestPayload("{\"versionId\": " + versionId + "}");
        job.setCreatedAt(Instant.now());
        asyncJobRepository.save(job);

        asyncMeinService.executeDeleteMeinMonoVersionJob(job.getId());
    }

   DeleteMeinMonoVersionResponse response = DeleteMeinMonoVersionResponse.newBuilder()
           .setJobId(job.getId())
           .setMessage("MEiN mono version deletion started or already in progress")
           .build();

   responseObserver.onNext(response);
   responseObserver.onCompleted();

}
//    @Override
//    public void adminRecalculateMonoCycleScores(AdminRecalcMonoCycleScoresRequest request, StreamObserver<AdminRecalcMonoCycleScoresResponse> responseObserver) {
//        Long cycleId = request.getCycleId();
//
//        if(!evalCycleRepository.existsById(cycleId)){
//            responseObserver.onError(Status.NOT_FOUND.withDescription("Not found the cycle").asRuntimeException());
//            return;
//        }
//
//        EvalCycle evalCycle = evalCycleRepository.findById(cycleId).orElseThrow(()-> new RuntimeException("Not found the cycle"));
//
//        Recalculation recalculation = recalcMonoCycleScoresInternal(evalCycle);
//
//        AdminRecalcMonoCycleScoresResponse response = AdminRecalcMonoCycleScoresResponse.newBuilder()
//                .setUpdatedMonographs(recalculation.getUpdatedMono())
//                .setUnmatchedMonographs(recalculation.getUnmatchdMono())
//                .setUpdatedChapters(recalculation.getUpdatedChapter())
//                .setUnmatchedChapters(recalculation.getUnmatchdChapter())
//                .build();
//
//        responseObserver.onNext(response);
//        responseObserver.onCompleted();
//    }
@Override
public void adminRecalculateMonoCycleScores(AdminRecalcMonoCycleScoresRequest request,
                                            StreamObserver<AdminRecalcMonoCycleScoresResponse> responseObserver) {
    Long cycleId = request.getCycleId();

    if (!evalCycleRepository.existsById(cycleId)) {
        responseObserver.onError(Status.NOT_FOUND
                .withDescription("Not found the cycle")
                .asRuntimeException());
        return;
    }

    String businessKey = "RECALC_MONO_CYCLE_SCORES:" + cycleId;

    Optional<AsyncJob> existingJobOpt =
            asyncJobRepository.findFirstByTypeAndBusinessKeyAndStatusIn(
                    "RECALC_MONO_CYCLE_SCORES",
                    businessKey,
                    List.of(AsyncJob.Status.QUEUED, AsyncJob.Status.RUNNING)
            );

    AsyncJob job;
    if (existingJobOpt.isPresent()) {
        job = existingJobOpt.get();
    } else {
        job = new AsyncJob();
        job.setType("RECALC_MONO_CYCLE_SCORES");
        job.setBusinessKey(businessKey);
        job.setStatus(AsyncJob.Status.QUEUED);
        job.setProgressPercent(0);
        job.setPhase("Queued");
        job.setMessage("Waiting to start monograph cycle recalculation");
        job.setRequestPayload("{\"cycleId\": " + cycleId + "}");
        job.setCreatedAt(Instant.now());
        asyncJobRepository.save(job);

        asyncMeinService.executeRecalcMonoCycleScoresJob(job.getId());
    }

    AdminRecalcMonoCycleScoresResponse response =
            AdminRecalcMonoCycleScoresResponse.newBuilder()
                    .setJobId(job.getId())
                    .setMessage("Monograph cycle recalculation started or already in progress")
                    .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
}
}
