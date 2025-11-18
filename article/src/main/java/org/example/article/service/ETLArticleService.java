package org.example.article.service;

import com.example.generated.*;
import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.persistence.EntityManager;
import org.example.article.ETL.ETLService;
import org.example.article.entities.CommuteResult;
import org.example.article.entities.EvalCycle;
import org.example.article.entities.MEiN.MeinCode;
import org.example.article.entities.MEiN.MeinJournal;
import org.example.article.entities.MEiN.MeinJournalCode;
import org.example.article.entities.MEiN.MeinVersion;
import org.example.article.entities.Publication;
import org.example.article.helpers.CommutePoints;
import org.example.article.repositories.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;

@Service
public class ETLArticleService extends ETLArticleServiceGrpc.ETLArticleServiceImplBase {

    final private org.example.article.ETL.ETLService ETLService;

    private final CommutePoints commutePoints;

    private final PublicationRepository publicationRepository;
    private final EvalCycleRepository evalCycleRepository;
    private final MeinVersionRepository meinVersionRepository;
    private final MeinJournalRepository meinJournalRepository;
    private final MeinJournalCodeRepository meinJournalCodeRepository;
    private final EntityManager entityManager;
    private final TransactionTemplate tx;

    public ETLArticleService(ETLService ETLService, PlatformTransactionManager txManager, EntityManager entityManager , CommutePoints commutePoints, PublicationRepository publicationRepository, RestClient.Builder builder, EvalCycleRepository evalCycleRepository, MeinVersionRepository meinVersionRepository, MeinJournalRepository meinJournalRepository, MeinJournalCodeRepository meinJournalCodeRepository) {
        this.ETLService = ETLService;
        this.tx = new TransactionTemplate(txManager);
        this.commutePoints = commutePoints;
        this.entityManager = entityManager;
        this.publicationRepository = publicationRepository;
        this.evalCycleRepository = evalCycleRepository;
        this.meinVersionRepository = meinVersionRepository;
        this.meinJournalRepository = meinJournalRepository;
        this.meinJournalCodeRepository = meinJournalCodeRepository;
    }

    //TODO: think about etl functions i also have to implement
    /**
     * ETL import
     */
    @Override
    public void importFile(ImportMEiNRequest request, StreamObserver<ImportMEiNReply> responseObserver) {

        try {
            byte[] bytes = request.getFile().toByteArray();
            String filename = request.getFilename();

            Long versionId = ETLService.importExcel(
                    bytes,
                    filename,
                    request.getLabel(),
                    request.getImportedBy(),
                    request.getActivateAfter()
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
    public void adminListMeinVersions(AdminListMeinVersionsRequest request, StreamObserver<AdminListMeinVersionsResponse> responseObserver) {
        int pg = Math.max(0, request.getPage());
        int sz = request.getSize() > 0 ? Math.min(request.getSize(), 100) : 20;

        boolean desc = !"ASC".equalsIgnoreCase(request.getSortDir());
        Sort sort = Sort.by(desc ? Sort.Direction.DESC : Sort.Direction.ASC, "importedAt");
        Pageable pageable = PageRequest.of(pg, sz, sort);

        Page<MeinVersion> page = meinVersionRepository.findAll(pageable);


        PageMeta pageMeta = PageMeta.newBuilder()
                .setPage(page.getNumber())
                .setTotalPages(page.getTotalPages())
                .setSize(page.getSize())
                .setTotalItems(page.getTotalElements())
                .build();

        AdminListMeinVersionsResponse.Builder response = AdminListMeinVersionsResponse.newBuilder()
                .setPage(pageMeta);



        for(MeinVersion meinVersion : page.getContent()) {

            long journalsCount = meinJournalRepository.countJournals(meinVersion.getId());
            long codesCount = meinJournalCodeRepository.countDistinctCodesInVersion(meinVersion.getId());
            Instant instant = meinVersion.getImportedAt();
            Timestamp time = Timestamp.newBuilder()
                    .setSeconds(instant.getEpochSecond())
                    .setNanos(instant.getNano())
                    .build();

            response.addItems(MeinVersionItem.newBuilder()
                    .setId(meinVersion.getId())
                    .setIsActive(meinVersion.isActive())
                    .setImportedAt(time)
                    .setJournals(journalsCount)
                    .setJournalCodes(codesCount)
                    .setLabel(meinVersion.getLabel())
                    .setSourceFilename(meinVersion.getSourceFilename())
                    .setImportedBy(meinVersion.getImportedBy())
                    .build());
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();

    }

    @Override
    public void adminGetActiveMeinVersion(Empty request, StreamObserver<AdminGetActiveMeinVersionResponse> responseObserver) {
        MeinVersion meinVersion = meinVersionRepository.findByActiveTrue().orElseThrow(() -> new RuntimeException("There is not active mein version"));

        long journalsCount = meinJournalRepository.countJournals(meinVersion.getId());
        long codesCount = meinJournalCodeRepository.countDistinctCodesInVersion(meinVersion.getId());

        Instant instant = meinVersion.getImportedAt();
        Timestamp time = Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();

        MeinVersionItem meinVersionItem = MeinVersionItem.newBuilder()
                .setId(meinVersion.getId())
                .setIsActive(meinVersion.isActive())
                .setImportedAt(time)
                .setJournals(journalsCount)
                .setJournalCodes(codesCount)
                .setLabel(meinVersion.getLabel())
                .setSourceFilename(meinVersion.getSourceFilename())
                .setImportedBy(meinVersion.getImportedBy())
                .build();
        AdminGetActiveMeinVersionResponse response = AdminGetActiveMeinVersionResponse.newBuilder()
                .setVersion(meinVersionItem)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void adminActivateMeinVersion(ActivateMeinVersionRequest request, StreamObserver<ApiResponse> responseObserver) {
        Long versionId = request.getVersionId();

        MeinVersion meinVersion = meinVersionRepository.findById(versionId).orElseThrow(() -> new RuntimeException("There is no this mein version"));

        if(meinVersionRepository.isActive(versionId).orElse(false)){
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("This version is already active").asRuntimeException());
            return;
        }

        meinVersion.setActive(true);
        meinVersionRepository.deactivateAllExcept(versionId);
        meinVersionRepository.save(meinVersion);

        ApiResponse apiResponse = ApiResponse.newBuilder()
                .setCode(200)
                .setMessage("This mein is now active").build();

        responseObserver.onNext(apiResponse);
        responseObserver.onCompleted();
    }


    @Override
    public void adminDeactivateMeinVersion(DeactivateMeinVersionRequest request, StreamObserver<ApiResponse> responseObserver) {
        Long versionId = request.getVersionId();

        MeinVersion meinVersion = meinVersionRepository.findById(versionId).orElseThrow(() -> new RuntimeException("There is no this mein version"));

        if(!meinVersionRepository.isActive(versionId).orElse(false)){
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("This version is already deactivate").asRuntimeException());
            return;
        }

        meinVersion.setActive(false);
        meinVersionRepository.save(meinVersion);

        ApiResponse apiResponse = ApiResponse.newBuilder()
                .setCode(200)
                .setMessage("This mein is now deactivate").build();

        responseObserver.onNext(apiResponse);
        responseObserver.onCompleted();

    }

    @Override
    public void adminGetMeinVersion(AdminGetMeinVersionRequest request, StreamObserver<AdminGetMeinVersionResponse> responseObserver) {
        Long versionId = request.getVersionId();


        MeinVersion meinVersion = meinVersionRepository.findById(versionId)
                .orElseThrow(() -> new RuntimeException("There is no this mein version"));

        long journalsCount = meinJournalRepository.countJournals(meinVersion.getId());
        long codesCount = meinJournalCodeRepository.countDistinctCodesInVersion(meinVersion.getId());

        Instant instant = meinVersion.getImportedAt();
        Timestamp time = Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();

        MeinVersionItem meinVersionItem = MeinVersionItem.newBuilder()
                .setId(meinVersion.getId())
                .setIsActive(meinVersion.isActive())
                .setImportedAt(time)
                .setJournals(journalsCount)
                .setJournalCodes(codesCount)
                .setLabel(meinVersion.getLabel())
                .setSourceFilename(meinVersion.getSourceFilename())
                .setImportedBy(meinVersion.getImportedBy())
                .build();
        AdminGetMeinVersionResponse response = AdminGetMeinVersionResponse.newBuilder()
                .setVersion(meinVersionItem)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    @Override
    @Transactional
    public void adminDeleteMeinVersion(DeleteMeinVersionRequest request, StreamObserver<ApiResponse> responseObserver) {
        Long versionId = request.getVersionId();

        if(!meinVersionRepository.existsById(versionId)){
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("No mein version with this id").asRuntimeException());

            return;
        }

        MeinVersion meinVersion = meinVersionRepository.findById(versionId).orElseThrow(() -> new RuntimeException("There is no this mein version"));
    
//        entityManager.createNativeQuery("SET LOCAL synchronous_commit = off").executeUpdate();
//        meinJournalCodeRepository.deleteCodesByVersion(versionId);
//        meinJournalRepository.deleteJournalsByVersion(versionId);
//
//        meinVersionRepository.deleteById(versionId);
//        meinVersionRepository.flush();
        tx.executeWithoutResult(status -> {
            entityManager.createNativeQuery("SET LOCAL synchronous_commit = off").executeUpdate();

            meinJournalCodeRepository.deleteCodesByVersion(versionId);   // @Modifying
            meinJournalRepository.deleteJournalsByVersion(versionId);    // @Modifying

            meinVersionRepository.deleteById(versionId);
            meinVersionRepository.flush();
        });
        ApiResponse response = ApiResponse.newBuilder()
                .setCode(200)
                .setMessage("Successfully deleted ")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void adminGetMeinJournal(AdminGetMeinJournalRequest request, StreamObserver<AdminGetMeinJournalResponse> responseObserver) {
        Long versionId = request.getVersionId();
        Long journalId = request.getJournalId();

        if(!meinVersionRepository.existsById(versionId)){
            responseObserver.onError(Status.NOT_FOUND.withDescription("Not found the mein version").asRuntimeException());
            return;
        }

        MeinJournal meinJournal = meinJournalRepository.findByIdAndVersion_Id(journalId, versionId)
                .orElseThrow(() -> new RuntimeException("There is no mein journal with this id"));


        OneMeinJournalItem.Builder item = OneMeinJournalItem.newBuilder()
                .setId(meinJournal.getId())
                .setUid(meinJournal.getUid())
                .setTitle1(meinJournal.getTitle1() == null ? "-" : meinJournal.getTitle1())
                .setTitle2(meinJournal.getTitle2() == null ? "-" : meinJournal.getTitle2())
                .setIssn(meinJournal.getIssn() == null ? "-" : meinJournal.getIssn())
                .setIssn2(meinJournal.getIssn2() == null ? "-" : meinJournal.getIssn2())
                .setEissn(meinJournal.getEissn() == null ? "-" : meinJournal.getEissn())
                .setEissn2(meinJournal.getEissn2() == null ? "-" : meinJournal.getEissn2())
                .setPoints(meinJournal.getPoints() == null ? 0 : meinJournal.getPoints());

        List<MeinJournalCode> codes = meinJournalCodeRepository.findAllByVersionIdAndJournalId(versionId, journalId);

        for (MeinJournalCode jc : codes) {
            MeinCode c = jc.getCodeRef();
            item.addCodes(CodeRef.newBuilder()
                    .setCode(c.getCode() == null ?  " - " : c.getCode() )
                    .setName(c.getName() == null ? " - " : c.getName())
                    .build()
            );
        }

        AdminGetMeinJournalResponse response = AdminGetMeinJournalResponse.newBuilder()
                .setItem(item.build())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }


    //TODO: think about sorting
    @Override
    public void adminListMeinJournals(AdminListMeinJournalsRequest request, StreamObserver<AdminListMeinJournalsResponse> responseObserver) {
        Long versionId = request.getVersionId();

        if (!meinVersionRepository.existsById(versionId)) {
            responseObserver.onError(Status.NOT_FOUND.withDescription("Not found the mein version").asRuntimeException());
            return;
        }

        int pg = Math.max(0, request.getPage());
        int sz = request.getSize() > 0 ? Math.min(request.getSize(), 100) : 20;


        Pageable pageable = PageRequest.of(pg, sz);

        Page<MeinJournal> page = meinJournalRepository.findByVersion_Id(versionId, pageable);


        PageMeta pageMeta = PageMeta.newBuilder()
                .setSize(page.getSize())
                .setPage(page.getNumber())
                .setTotalItems(page.getTotalElements())
                .setTotalPages(page.getTotalPages())
                .build();

        AdminListMeinJournalsResponse.Builder response = AdminListMeinJournalsResponse.newBuilder()
                .setPage(pageMeta);


        for (MeinJournal mj : page.getContent()) {
            String title = mj.getTitle1() != null ? mj.getTitle1() : nvl(mj.getTitle2());
            String issn = mj.getIssn() != null ? mj.getIssn() : nvl(mj.getIssn2());
            String eissn = mj.getEissn() != null ? mj.getEissn() : nvl(mj.getEissn2());
            int points = mj.getPoints() != null ? mj.getPoints() : 0;

            response.addItems(MeinJournalItem.newBuilder()
                    .setId(mj.getId())
                    .setUid((mj.getUid()))
                    .setTitle(title)
                    .setIssn(issn)
                    .setEissn(eissn)
                    .setPoints(points)
                    .build());
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();


    }
    @Override
    public void adminRecalculateCycleScores(AdminRecalcCycleScoresRequest request, StreamObserver<AdminRecalcCycleScoresResponse> responseObserver) {
        Long cycleId = request.getCycleId();

        if(!evalCycleRepository.existsById(cycleId)){
            responseObserver.onError(Status.NOT_FOUND.withDescription("Not found the cycle").asRuntimeException());
            return;
        }

        EvalCycle evalCycle = evalCycleRepository.findById(cycleId).orElseThrow(()-> new RuntimeException("Not found the cycle"));

        if(evalCycle.getMeinVersion() == null){
            responseObserver.onError(Status.NOT_FOUND.withDescription("Not found the mein version").asRuntimeException());
        }

        List<Publication> publications = publicationRepository.findAllByCycle(evalCycle);

        int updated = 0;
        int unmatched = 0;

        for (Publication pub : publications) {
            CommuteResult result = commutePoints.commute(
                    pub.getJournalTitle(),
                    pub.getType().getId(),
                    pub.getDiscipline().getId(),
                    pub.getIssn(),
                    pub.getEissn(),
                    pub.getPublicationYear()
            );

            if (result == null
                    || result.meinJournal() == null
                    || result.meinVersion() == null) {
                unmatched++;
                continue;
            }

            pub.setCycle(result.cycle());
            pub.setMeinPoints(result.points());
            pub.setMeinVersionId(result.meinVersion().getId());
            pub.setMeinJournalId(result.meinJournal().getId());
            pub.setUpdatedAt(Instant.now());

            updated++;
        }

        if (!publications.isEmpty()) {
            publicationRepository.saveAll(publications);
        }

        AdminRecalcCycleScoresResponse response = AdminRecalcCycleScoresResponse.newBuilder()
                .setUpdatedPublications(updated)
                .setUnmatchedPublications(unmatched)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
