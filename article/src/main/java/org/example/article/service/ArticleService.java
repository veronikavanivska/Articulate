package org.example.article.service;

import com.example.generated.*;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.example.article.entities.*;
import org.example.article.repositories.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ArticleService extends ArticleServiceGrpc.ArticleServiceImplBase {


    private final PublicationTypeRepository publicationTypeRepository;
    private final DisciplineRepository disciplineRepository;
    private final EvalCycleRepository evalCycleRepository;

    public ArticleService( PublicationTypeRepository publicationTypeRepository, DisciplineRepository disciplineRepository, EvalCycleRepository evalCycleRepository) {
        this.publicationTypeRepository = publicationTypeRepository;
        this.disciplineRepository = disciplineRepository;
        this.evalCycleRepository = evalCycleRepository;
    }

    //TODO: think about etl functions i also have to implement
//    /**
//     * ETL import
//     */
//    @Override
//    public void importFile(ImportMEiNRequest request, StreamObserver<ImportMEiNReply> responseObserver) {
//
//        try {
//            byte[] bytes = request.getFile().toByteArray();
//            String filename = request.getFilename();
//
//            Long versionId = ETLService.importExcel(
//                    bytes,
//                    filename,
//                    request.getLabel(),
//                    request.getImportedBy(),
//                    request.getActivateAfter()
//            );
//
//            boolean already = (versionId == null);
//            long id = already ? -1L : versionId;
//            ImportMEiNReply resp = ImportMEiNReply.newBuilder()
//                    .setVersionId(id)
//                    .setAlreadyImported(already)
//                    .build();
//
//            responseObserver.onNext(resp);
//            responseObserver.onCompleted();
//
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public void adminListMeinVersions(AdminListMeinVersionsRequest request, StreamObserver<AdminListMeinVersionsResponse> responseObserver) {
//        int pg = Math.max(0, request.getPage());
//        int sz = request.getSize() > 0 ? Math.min(request.getSize(), 100) : 20;
//
//        boolean desc = !"ASC".equalsIgnoreCase(request.getSortDir());
//        Sort sort = Sort.by(desc ? Sort.Direction.DESC : Sort.Direction.ASC, "importedAt");
//        Pageable pageable = PageRequest.of(pg, sz, sort);
//
//        Page<MeinVersion> page = meinVersionRepository.findAll(pageable);
//
//
//        PageMeta pageMeta = PageMeta.newBuilder()
//                .setPage(page.getNumber())
//                .setTotalPages(page.getTotalPages())
//                .setSize(page.getSize())
//                .setTotalItems(page.getTotalElements())
//                .build();
//
//        AdminListMeinVersionsResponse.Builder response = AdminListMeinVersionsResponse.newBuilder()
//                .setPage(pageMeta);
//
//
//
//        for(MeinVersion meinVersion : page.getContent()) {
//
//            long journalsCount = meinJournalRepository.countJournals(meinVersion.getId());
//            long codesCount = meinJournalCodeRepository.countDistinctCodesInVersion(meinVersion.getId());
//            Instant instant = meinVersion.getImportedAt();
//            Timestamp time = Timestamp.newBuilder()
//                    .setSeconds(instant.getEpochSecond())
//                    .setNanos(instant.getNano())
//                    .build();
//
//            response.addItems(MeinVersionItem.newBuilder()
//                    .setId(meinVersion.getId())
//                    .setIsActive(meinVersion.isActive())
//                    .setImportedAt(time)
//                    .setJournals(journalsCount)
//                    .setJournalCodes(codesCount)
//                    .setLabel(meinVersion.getLabel())
//                    .setSourceFilename(meinVersion.getSourceFilename())
//                    .setImportedBy(meinVersion.getImportedBy())
//                    .build());
//        }
//
//        responseObserver.onNext(response.build());
//        responseObserver.onCompleted();
//
//    }
//
//    @Override
//    public void adminGetActiveMeinVersion(Empty request, StreamObserver<AdminGetActiveMeinVersionResponse> responseObserver) {
//        MeinVersion meinVersion = meinVersionRepository.findByActiveTrue().orElseThrow(() -> new RuntimeException("There is not active mein version"));
//
//        long journalsCount = meinJournalRepository.countJournals(meinVersion.getId());
//        long codesCount = meinJournalCodeRepository.countDistinctCodesInVersion(meinVersion.getId());
//
//        Instant instant = meinVersion.getImportedAt();
//        Timestamp time = Timestamp.newBuilder()
//                .setSeconds(instant.getEpochSecond())
//                .setNanos(instant.getNano())
//                .build();
//
//        MeinVersionItem meinVersionItem = MeinVersionItem.newBuilder()
//                .setId(meinVersion.getId())
//                .setIsActive(meinVersion.isActive())
//                .setImportedAt(time)
//                .setJournals(journalsCount)
//                .setJournalCodes(codesCount)
//                .setLabel(meinVersion.getLabel())
//                .setSourceFilename(meinVersion.getSourceFilename())
//                .setImportedBy(meinVersion.getImportedBy())
//                .build();
//        AdminGetActiveMeinVersionResponse response = AdminGetActiveMeinVersionResponse.newBuilder()
//                .setVersion(meinVersionItem)
//                .build();
//
//        responseObserver.onNext(response);
//        responseObserver.onCompleted();
//    }
//
//    @Override
//    public void adminActivateMeinVersion(ActivateMeinVersionRequest request, StreamObserver<ApiResponse> responseObserver) {
//        Long versionId = request.getVersionId();
//
//        MeinVersion meinVersion = meinVersionRepository.findById(versionId).orElseThrow(() -> new RuntimeException("There is no this mein version"));
//
//        if(meinVersionRepository.isActive(versionId).orElse(false)){
//           responseObserver.onError(Status.INVALID_ARGUMENT
//                   .withDescription("This version is already active").asRuntimeException());
//           return;
//        }
//
//        meinVersion.setActive(true);
//        meinVersionRepository.deactivateAllExcept(versionId);
//        meinVersionRepository.save(meinVersion);
//
//        ApiResponse apiResponse = ApiResponse.newBuilder()
//                .setCode(200)
//                .setMessage("This mein is now active").build();
//
//        responseObserver.onNext(apiResponse);
//        responseObserver.onCompleted();
//    }
//
//
//    @Override
//    public void adminDeactivateMeinVersion(DeactivateMeinVersionRequest request, StreamObserver<ApiResponse> responseObserver) {
//        Long versionId = request.getVersionId();
//
//        MeinVersion meinVersion = meinVersionRepository.findById(versionId).orElseThrow(() -> new RuntimeException("There is no this mein version"));
//
//        if(!meinVersionRepository.isActive(versionId).orElse(false)){
//            responseObserver.onError(Status.INVALID_ARGUMENT
//                    .withDescription("This version is already deactivate").asRuntimeException());
//            return;
//        }
//
//        meinVersion.setActive(false);
//        meinVersionRepository.save(meinVersion);
//
//        ApiResponse apiResponse = ApiResponse.newBuilder()
//                .setCode(200)
//                .setMessage("This mein is now deactivate").build();
//
//        responseObserver.onNext(apiResponse);
//        responseObserver.onCompleted();
//
//    }
//
//    @Override
//    public void adminGetMeinVersion(AdminGetMeinVersionRequest request, StreamObserver<AdminGetMeinVersionResponse> responseObserver) {
//        Long versionId = request.getVersionId();
//
//
//        MeinVersion meinVersion = meinVersionRepository.findById(versionId)
//                .orElseThrow(() -> new RuntimeException("There is no this mein version"));
//
//        long journalsCount = meinJournalRepository.countJournals(meinVersion.getId());
//        long codesCount = meinJournalCodeRepository.countDistinctCodesInVersion(meinVersion.getId());
//
//        Instant instant = meinVersion.getImportedAt();
//        Timestamp time = Timestamp.newBuilder()
//                .setSeconds(instant.getEpochSecond())
//                .setNanos(instant.getNano())
//                .build();
//
//        MeinVersionItem meinVersionItem = MeinVersionItem.newBuilder()
//                .setId(meinVersion.getId())
//                .setIsActive(meinVersion.isActive())
//                .setImportedAt(time)
//                .setJournals(journalsCount)
//                .setJournalCodes(codesCount)
//                .setLabel(meinVersion.getLabel())
//                .setSourceFilename(meinVersion.getSourceFilename())
//                .setImportedBy(meinVersion.getImportedBy())
//                .build();
//        AdminGetMeinVersionResponse response = AdminGetMeinVersionResponse.newBuilder()
//                .setVersion(meinVersionItem)
//                .build();
//
//        responseObserver.onNext(response);
//        responseObserver.onCompleted();
//    }
//
//
//    @Override
//    public void adminDeleteMeinVersion(DeleteMeinVersionRequest request, StreamObserver<ApiResponse> responseObserver) {
//        Long versionId = request.getVersionId();
//
//        if(!meinVersionRepository.existsById(versionId)){
//            responseObserver.onError(Status.NOT_FOUND
//                    .withDescription("No mein version with this id").asRuntimeException());
//
//            return;
//        }
//
//        meinVersionRepository.deleteById(versionId);
//
//        ApiResponse response = ApiResponse.newBuilder()
//                .setCode(200)
//                .setMessage("Successfully deleted ")
//                .build();
//
//        responseObserver.onNext(response);
//        responseObserver.onCompleted();
//    }
//
//    @Override
//    public void adminGetMeinJournal(AdminGetMeinJournalRequest request, StreamObserver<AdminGetMeinJournalResponse> responseObserver) {
//        Long versionId = request.getVersionId();
//        Long journalId = request.getJournalId();
//
//        if(!meinVersionRepository.existsById(versionId)){
//            responseObserver.onError(Status.NOT_FOUND.withDescription("Not found the mein version").asRuntimeException());
//            return;
//        }
//
//        MeinJournal meinJournal = meinJournalRepository.findByIdAndVersion_Id(journalId, versionId)
//                .orElseThrow(() -> new RuntimeException("There is no mein journal with this id"));
//
//
//        OneMeinJournalItem.Builder item = OneMeinJournalItem.newBuilder()
//                .setId(meinJournal.getId())
//                .setUid(meinJournal.getUid())
//                .setTitle1(meinJournal.getTitle1() == null ? "-" : meinJournal.getTitle1())
//                .setTitle2(meinJournal.getTitle2() == null ? "-" : meinJournal.getTitle2())
//                .setIssn(meinJournal.getIssn() == null ? "-" : meinJournal.getIssn())
//                .setIssn2(meinJournal.getIssn2() == null ? "-" : meinJournal.getIssn2())
//                .setEissn(meinJournal.getEissn() == null ? "-" : meinJournal.getEissn())
//                .setEissn2(meinJournal.getEissn2() == null ? "-" : meinJournal.getEissn2())
//                .setPoints(meinJournal.getPoints() == null ? 0 : meinJournal.getPoints());
//
//        List<MeinJournalCode> codes = meinJournalCodeRepository.findAllByVersionIdAndJournalId(versionId, journalId);
//
//        for (MeinJournalCode jc : codes) {
//            MeinCode c = jc.getCodeRef();
//            item.addCodes(CodeRef.newBuilder()
//                            .setCode(c.getCode() == null ?  " - " : c.getCode() )
//                            .setName(c.getName() == null ? " - " : c.getName())
//                            .build()
//            );
//        }
//
//        AdminGetMeinJournalResponse response = AdminGetMeinJournalResponse.newBuilder()
//                .setItem(item.build())
//                .build();
//
//        responseObserver.onNext(response);
//        responseObserver.onCompleted();
//    }
//
//    private static String nvl(String s) {
//        return s == null ? "" : s;
//    }
//
//
//    //TODO: think about sorting
//    @Override
//    public void adminListMeinJournals(AdminListMeinJournalsRequest request, StreamObserver<AdminListMeinJournalsResponse> responseObserver) {
//        Long versionId = request.getVersionId();
//
//        if(!meinVersionRepository.existsById(versionId)){
//            responseObserver.onError(Status.NOT_FOUND.withDescription("Not found the mein version").asRuntimeException());
//            return;
//        }
//
//        int pg = Math.max(0, request.getPage());
//        int sz = request.getSize() > 0 ? Math.min(request.getSize(), 100) : 20;
//
//
//        Pageable pageable = PageRequest.of(pg, sz);
//
//        Page<MeinJournal> page = meinJournalRepository.findByVersion_Id(versionId, pageable);
//
//
//        PageMeta pageMeta = PageMeta.newBuilder()
//                .setSize(page.getSize())
//                .setPage(page.getNumber())
//                .setTotalItems(page.getTotalElements())
//                .setTotalPages(page.getTotalPages())
//                .build();
//
//        AdminListMeinJournalsResponse.Builder response = AdminListMeinJournalsResponse.newBuilder()
//                .setPage(pageMeta);
//
//
//        for (MeinJournal mj : page.getContent()) {
//            String title = mj.getTitle1() != null ? mj.getTitle1() : nvl(mj.getTitle2());
//            String issn  = mj.getIssn()   != null ? mj.getIssn()   : nvl(mj.getIssn2());
//            String eissn = mj.getEissn()  != null ? mj.getEissn()  : nvl(mj.getEissn2());
//            int points   = mj.getPoints() != null ? mj.getPoints() : 0;
//
//            response.addItems(MeinJournalItem.newBuilder()
//                    .setId(mj.getId())
//                    .setUid((mj.getUid()))
//                    .setTitle(title)
//                    .setIssn(issn)
//                    .setEissn(eissn)
//                    .setPoints(points)
//                    .build());
//        }
//
//        responseObserver.onNext(response.build());
//        responseObserver.onCompleted();
//
//
//    }
//
//    @Override
//    public void adminRecalculateCycleScores(AdminRecalcCycleScoresRequest request, StreamObserver<AdminRecalcCycleScoresResponse> responseObserver) {
//        Long cycleId = request.getCycleId();
//
//        if(!evalCycleRepository.existsById(cycleId)){
//            responseObserver.onError(Status.NOT_FOUND.withDescription("Not found the cycle").asRuntimeException());
//            return;
//        }
//
//        EvalCycle evalCycle = evalCycleRepository.findById(cycleId).orElseThrow(()-> new RuntimeException("Not found the cycle"));
//
//        if(evalCycle.getMeinVersion() == null){
//            responseObserver.onError(Status.NOT_FOUND.withDescription("Not found the mein version").asRuntimeException());
//        }
//
//        List<Publication> publications = publicationRepository.findAllByCycle(evalCycle);
//
//        int updated = 0;
//        int unmatched = 0;
//
//        for (Publication pub : publications) {
//            CommuteResult result = commutePoints.commute(
//                    pub.getJournalTitle(),
//                    pub.getType().getId(),
//                    pub.getDiscipline().getId(),
//                    pub.getIssn(),
//                    pub.getEissn(),
//                    pub.getPublicationYear()
//            );
//
//            if (result == null
//                    || result.meinJournal() == null
//                    || result.meinVersion() == null) {
//                unmatched++;
//                continue;
//            }
//
//            pub.setCycle(result.cycle());
//            pub.setMeinPoints(result.points());
//            pub.setMeinVersionId(result.meinVersion().getId());
//            pub.setMeinJournalId(result.meinJournal().getId());
//            pub.setUpdatedAt(Instant.now());
//
//            updated++;
//        }
//
//        if (!publications.isEmpty()) {
//            publicationRepository.saveAll(publications);
//        }
//
//        AdminRecalcCycleScoresResponse response = AdminRecalcCycleScoresResponse.newBuilder()
//                .setUpdatedPublications(updated)
//                .setUnmatchedPublications(unmatched)
//                .build();
//
//        responseObserver.onNext(response);
//        responseObserver.onCompleted();
//    }
//
//    /**
//     * Worker functions
//     * <p>
//     * 1) createPublication
//     * </p><p>
//     * 2) getPublication
//     * </p>
//     * <p>
//     * 3) updatePublication
//     * </p> <p>
//     * 4) DeletePublication
//     * </p> <p>
//     * 5) ListMyPublications(with filters and sorting)
//     * </p>
//    */
//    @Override
//    public void createPublication(CreatePublicationRequest request, StreamObserver<PublicationView> responseObserver) {
//        if(publicationRepository.existsByAuthorId(request.getUserId())&&
//                publicationRepository.existsByTitle(request.getTitle())){
//            responseObserver.onError(Status.INVALID_ARGUMENT
//                    .withDescription("You already added this article").asRuntimeException());
//            return;
//
//        }
//        if (request.getCoauthorsCount() == 0) {
//            throw Status.INVALID_ARGUMENT.withDescription("At least one author (including owner) must be provided").asRuntimeException();
//        }
//        if (request.getTitle().isBlank()) {
//            responseObserver.onError(Status.INVALID_ARGUMENT
//                    .withDescription("Title cannot be empty").asRuntimeException());
//            return;
//        }
//        if (request.getTypeId() <= 0 || request.getDisciplineId() <= 0) {
//            responseObserver.onError(Status.INVALID_ARGUMENT
//                    .withDescription("Type and Discipline must be provided").asRuntimeException());
//            return;
//        }
//        if (request.getPublicationYear() < 1900 || request.getPublicationYear() > 2100) {
//            responseObserver.onError(Status.INVALID_ARGUMENT
//                    .withDescription("Publication year is not valid").asRuntimeException());
//            return;
//        }
//
//        if (backfromnorm(request.getIssn()).isBlank() && backfromnorm(request.getEissn()).isBlank()){
//            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Either ISSN or eISSN must be provided").asRuntimeException());
//            return;
//        }
//
//        CommuteResult result = commutePoints.commute(request.getJournalTitle(),request.getTypeId(),request.getDisciplineId(),request.getIssn(),request.getEissn(),request.getPublicationYear());
//
//        Publication publication = Publication.builder()
//                .authorId(request.getUserId())
//                .type(publicationTypeRepository.findById(request.getTypeId()).orElseThrow())
//                .title(request.getTitle())
//                .doi(normalize(request.getDoi()))
//                .issn(normalize(request.getIssn()))
//                .eissn(normalize(request.getEissn()))
//                .journalTitle(request.getJournalTitle())
//                .publicationYear(request.getPublicationYear())
//                .cycle(result.cycle())
//                .discipline(disciplineRepository.findById(request.getDisciplineId()).orElseThrow())
//                .meinPoints(result.points())
//                .meinVersionId(result.meinVersion().getId())
//                .meinJournalId(result.meinJournal().getId())
//                .build();
//
//        if (request.getCoauthorsCount() > 0) {
//            List<PublicationCoauthor> authors = new ArrayList<>(request.getCoauthorsCount());
//            for (int i = 0; i < request.getCoauthorsCount(); i++) {
//                authors.add(PublicationCoauthor.builder()
//                        .publication(publication)
//                        .position(i + 1)
//                        .fullName(request.getCoauthors(i))
//                        .build());
//            }
//            publication.getCoauthors().clear();
//            publication.getCoauthors().addAll(authors);
//        }
//
//        publicationRepository.save(publication);
//
//        PublicationView publicationView = entityToProto(publication);
//        responseObserver.onNext(publicationView);
//        responseObserver.onCompleted();
//
//    }
//
//    @Override
//    @Transactional
//    public void getPublication(GetPublicationRequest request, StreamObserver<PublicationView> responseObserver) {
//
//        Publication publication = publicationRepository.findWithAllRelations(request.getId()).orElseThrow();
//
//        if(!publication.getAuthorId().equals(request.getUserId())){
//            responseObserver.onError(Status.INVALID_ARGUMENT
//                    .withDescription("This is not yours publication, you cannot see it, ha-ha-ha").asRuntimeException());
//            return;
//        }
//
//        PublicationView publicationView = entityToProto(publication);
//        responseObserver.onNext(publicationView);
//        responseObserver.onCompleted();
//    }
//
//    @Override
//    @Transactional
//    public void updatePublication(UpdatePublicationRequest request, StreamObserver<PublicationView> responseObserver) {
//
//        boolean changeForCommute = false;
//
//        Publication publication = publicationRepository.findWithAllRelations(request.getId()).orElseThrow();
//        if(!publication.getAuthorId().equals(request.getUserId())){
//            responseObserver.onError(Status.INVALID_ARGUMENT
//                    .withDescription("This is not yours publication, you cannot see it, ha-ha-ha").asRuntimeException());
//            return;
//        }
//
//        Set<String> paths = new HashSet<>(request.getUpdateMask().getPathsList());
//
//        if (paths.contains("typeId"))
//        {
//            publication.setType(publicationTypeRepository.findById(request.getTypeId()).orElseThrow());
//            changeForCommute = true;
//        }
//        if (paths.contains("disciplineId")){
//            publication.setDiscipline(disciplineRepository.findById(request.getDisciplineId()).orElseThrow());
//            changeForCommute = true;
//        }
//        if (paths.contains("title")) {
//            String v = (request.getTitle());
//            if(v == null || v.isEmpty()) {
//            responseObserver.onError(Status.INVALID_ARGUMENT
//                    .withDescription("Type and Discipline must be provided").asRuntimeException());
//            return;}
//            publication.setTitle(v);
//        }
//
//        if (paths.contains("doi"))           publication.setDoi(normalize(request.getDoi()));
//        if (paths.contains("issn")) {
//            publication.setIssn(normalize(request.getIssn()));
//            changeForCommute = true;
//        }
//        if (paths.contains("eissn")) {
//            publication.setEissn(normalize(request.getEissn()));
//            changeForCommute = true;
//        }
//        if (paths.contains("journalTitle"))  {
//            publication.setJournalTitle(request.getJournalTitle());
//            changeForCommute = true;
//        }
//        if (paths.contains("publicationYear")){
//            publication.setPublicationYear(request.getPublicationYear());
//            changeForCommute = true;
//        }
//
//        if (paths.contains("coauthors")) {
//                List<PublicationCoauthor> authors = new ArrayList<>(request.getReplaceCoauthorsCount());
//                for (int i = 0; i < request.getReplaceCoauthorsCount(); i++) {
//                    authors.add(PublicationCoauthor.builder()
//                            .publication(publication)
//                            .position(i + 1)
//                            .fullName(request.getReplaceCoauthors(i))
//                            .build());
//                }
//                publication.getCoauthors().clear();
//                publication.getCoauthors().addAll(authors);
//        }
//
//
//        if(changeForCommute){
//            CommuteResult result = commutePoints.commute(publication.getJournalTitle(),publication.getType().getId(),publication.getDiscipline().getId(),publication.getIssn(),publication.getEissn(),publication.getPublicationYear());
//
//            if (result.meinJournal() == null || result.meinVersion() == null) {
//                responseObserver.onError(Status.INVALID_ARGUMENT
//                        .withDescription("ISSN/eISSN/title/year/discipline do not match an active MEiN journal.")
//                        .asRuntimeException());
//                return;
//            }
//
//            publication.setMeinPoints(result.points());
//            publication.setCycle(result.cycle());
//            publication.setMeinJournalId(result.meinJournal().getId() );
//            publication.setMeinVersionId(result.meinVersion().getId());
//        }
//
//         publicationRepository.save(publication);
//
//
//        PublicationView publicationView = entityToProto(publication);
//        responseObserver.onNext(publicationView);
//        responseObserver.onCompleted();
//
//    }
//
//    @Override
//    public void deletePublication(DeletePublicationRequest request, StreamObserver<ApiResponse> responseObserver) {
//
//
//        Publication publication = publicationRepository.findById(request.getId()).orElseThrow(() -> new IllegalArgumentException("Publication not found"));
//
//        if(!publication.getAuthorId().equals(request.getUserId())){
//            responseObserver.onError(Status.INVALID_ARGUMENT
//                    .withDescription("This is not yours publication, you cannot delete it, ha-ha-ha").asRuntimeException());
//            return;
//        }
//
//        publicationRepository.delete(publication);
//
//        ApiResponse response = ApiResponse.newBuilder().setCode(200).setMessage("Deleted").build();
//
//        responseObserver.onNext(response);
//        responseObserver.onCompleted();
//
//    }
//
//    @Override
//    public void listMyPublications(ListPublicationsRequest request, StreamObserver<ListPublicationsResponse> responseObserver) {
//        doList(responseObserver , request.getUserId() , request.getTypeId(), request.getDisciplineId(), request.getCycleId(),
//                request.getPage(), request.getSize(), request.getSortBy() , request.getSortDir());
//    }
//
//    /**
//     * Admin functions
//     */
//    @Override
//    public void adminListPublications(ListAdminPublicationRequest request, StreamObserver<ListPublicationsResponse> responseObserver) {
//        Long authorId = request.getOwnerId() > 0 ? request.getOwnerId() : null;
//        doList(responseObserver , authorId , request.getTypeId(), request.getDisciplineId(), request.getCycleId(),
//                request.getPage(), request.getSize(), request.getSortBy() , request.getSortDir());
//    }
//
//    @Override
//    public void adminGetPublication(GetPublicationRequest request, StreamObserver<PublicationView> responseObserver) {
//        Publication publication = publicationRepository.findWithAllRelations(request.getId()).orElseThrow();
//
//        PublicationView publicationView = entityToProto(publication);
//        responseObserver.onNext(publicationView);
//        responseObserver.onCompleted();
//    }
//
//
//
//
//    @Override
//    public void adminListDisciplines(AdminListDisciplinesRequest request, StreamObserver<AdminListDisciplinesResponse> responseObserver) {
//        int pg = Math.max(0, request.getPage());
//        int sz = request.getSize() > 0 ? Math.min(request.getSize(), 100) : 20;
//
//        boolean desc = !"ASC".equalsIgnoreCase(request.getSortDir());
//        Pageable pageable = PageRequest.of(pg, sz, desc ? Sort.by("name").descending() : Sort.by("name").ascending());
//
//        Page<Discipline> page = disciplineRepository.findAll(pageable);
//
//        PageMeta pageMeta = PageMeta.newBuilder()
//                .setSize(page.getSize())
//                .setPage(page.getNumber())
//                .setTotalItems(page.getTotalElements())
//                .setTotalPages(page.getTotalPages())
//                .build();
//
//        AdminListDisciplinesResponse.Builder response = AdminListDisciplinesResponse.newBuilder()
//                .setPage(pageMeta);
//
//        for(Discipline discipline : page.getContent()){
//            response.addItems(RefItem.newBuilder()
//                    .setId(discipline.getId())
//                    .setName(discipline.getName())
//                    .build());
//        }
//
//        responseObserver.onNext(response.build());
//        responseObserver.onCompleted();
//    }
//
//
//    @Override
//    public void adminListEvalCycles(AdminListCyclesRequest request, StreamObserver<AdminListCyclesResponse> responseObserver) {
//        int pg = Math.max(0, request.getPage());
//        int sz = request.getSize() > 0 ? Math.min(request.getSize(), 100) : 20;
//
//        boolean desc = !"ASC".equalsIgnoreCase(request.getSortDir());
//        Pageable pageable = PageRequest.of(pg, sz, desc ? Sort.by("name").descending() : Sort.by("name").ascending());
//
//        Page<EvalCycle> page = evalCycleRepository.findAll(pageable);
//
//        PageMeta pageMeta = PageMeta.newBuilder()
//                .setSize(page.getSize())
//                .setPage(page.getNumber())
//                .setTotalItems(page.getTotalElements())
//                .setTotalPages(page.getTotalPages())
//                .build();
//
//        AdminListCyclesResponse.Builder response = AdminListCyclesResponse.newBuilder()
//                .setPage(pageMeta);
//
//        for(EvalCycle evalCycle : page.getContent()){
//            long mvId = 0L;
//
//            if (evalCycle.getMeinVersion() != null && evalCycle.getMeinVersion().getId() != null) {
//                mvId = evalCycle.getMeinVersion().getId();
//            }
//            response.addItems(CycleItem.newBuilder()
//                    .setId(evalCycle.getId())
//                    .setName(evalCycle.getName())
//                    .setYearFrom(evalCycle.getYearFrom())
//                    .setYearTo(evalCycle.getYearTo())
//                    .setIsActive(evalCycle.isActive())
//                    .setMeinVersionId(mvId)
//                    .build());
//        }
//
//        responseObserver.onNext(response.build());
//        responseObserver.onCompleted();
//    }
//
//    @Override
//    public void adminListPublicationTypes(AdminListTypesRequest request, StreamObserver<AdminListTypesResponse> responseObserver) {
//        int pg = Math.max(0, request.getPage());
//        int sz = request.getSize() > 0 ? Math.min(request.getSize(), 100) : 20;
//
//        boolean desc = !"ASC".equalsIgnoreCase(request.getSortDir());
//        Pageable pageable = PageRequest.of(pg, sz, desc ? Sort.by("name").descending() : Sort.by("name").ascending());
//
//        Page<PublicationType> page = publicationTypeRepository.findAll(pageable);
//        PageMeta pageMeta = PageMeta.newBuilder()
//                .setSize(page.getSize())
//                .setPage(page.getNumber())
//                .setTotalItems(page.getTotalElements())
//                .setTotalPages(page.getTotalPages())
//                .build();
//
//        AdminListTypesResponse.Builder response = AdminListTypesResponse.newBuilder().setPage(pageMeta);
//
//        for(PublicationType publicationType : page.getContent()){
//            response.addItems(RefItem.newBuilder()
//                    .setId(publicationType.getId())
//                    .setName(publicationType.getName())
//                    .build());
//        }
//        responseObserver.onNext(response.build());
//        responseObserver.onCompleted();
//    }
//
//
//    @Override
//    public void adminCreateDiscipline(CreateDisciplineRequest request, StreamObserver<RefItem> responseObserver) {
//        String disciplineName = request.getDisciplineName();
//
//        if(disciplineName.isEmpty()){
//            responseObserver.onError(Status.INVALID_ARGUMENT
//                    .withDescription("disciplineName is required.").asRuntimeException());
//            return;
//        }
//
//        if(disciplineRepository.existsByName(disciplineName)){
//            responseObserver.onError(Status.ALREADY_EXISTS
//                    .withDescription("Discipline \"" + disciplineName + "\" already exists.").asRuntimeException());
//            return;
//        }
//
//        Discipline d = new Discipline();
//        d.setName(disciplineName);
//
//        Discipline saved = disciplineRepository.save(d);
//
//
//        RefItem response = RefItem.newBuilder()
//                .setId(saved.getId())
//                .setName(saved.getName())
//                .build();
//
//        responseObserver.onNext(response);
//        responseObserver.onCompleted();
//    }
//
//
//    @Transactional
//    @Override
//    public void adminCreateEvalCycle(CreateCycleRequest request, StreamObserver<CycleItem> responseObserver) {
//
//        String evalName = request.getName();
//        int yearFrom = request.getYearFrom();
//        int yearTo = request.getYearTo();
//        boolean active = request.getIsActive();
//
//        if (evalName.isEmpty()) {
//            responseObserver.onError(Status.INVALID_ARGUMENT
//                    .withDescription("name is required.").asRuntimeException());
//            return;
//        }
//        if (yearFrom <= 0 || yearTo <= 0) {
//            responseObserver.onError(Status.INVALID_ARGUMENT
//                    .withDescription("yearFrom and yearTo must be positive.").asRuntimeException());
//            return;
//        }
//        if (yearFrom > yearTo) {
//            responseObserver.onError(Status.INVALID_ARGUMENT
//                    .withDescription("yearFrom cannot be greater than yearTo.").asRuntimeException());
//            return;
//        }
//
//        if(evalCycleRepository.existsByName(evalName)){
//            responseObserver.onError(Status.ALREADY_EXISTS
//                    .withDescription("Evaluation cycle \"" + evalName + "\" already exists.").asRuntimeException());
//            return;
//        }
//
//        if(evalCycleRepository.existsOverlapping(yearFrom, yearTo)){
//            responseObserver.onError(Status.FAILED_PRECONDITION
//                    .withDescription("The provided year range overlaps an existing evaluation cycle.").asRuntimeException());
//            return;
//        }
//        EvalCycle cycle = new EvalCycle();
//
//        cycle.setName(evalName);
//        cycle.setYearFrom(yearFrom);
//        cycle.setYearTo(yearTo);
//        cycle.setActive(active);
//
//        if (active) {
//            evalCycleRepository.deactivateAll();
//        }
//
//
//        EvalCycle saved = evalCycleRepository.save(cycle);
//
//        long mvId = 0;
//
//        if (saved.getMeinVersion() != null && saved.getMeinVersion().getId() != null) {
//            mvId = saved.getMeinVersion().getId();
//        }
//        CycleItem resp = CycleItem.newBuilder()
//                .setId(saved.getId())
//                .setName(saved.getName() == null ? "" : saved.getName())
//                .setYearFrom(saved.getYearFrom())
//                .setYearTo(saved.getYearTo())
//                .setIsActive(saved.isActive())
//                .setMeinVersionId(mvId)
//                .build();
//
//        responseObserver.onNext(resp);
//        responseObserver.onCompleted();
//    }
//
//    @Override
//    public void adminCreatePublicationType(CreateTypeRequest request, StreamObserver<RefItem> responseObserver) {
//        String publicationTypeName = request.getName();
//
//        if(publicationTypeName.isEmpty()){
//            responseObserver.onError(Status.INVALID_ARGUMENT
//                    .withDescription("publicationType name is required.").asRuntimeException());
//            return;
//        }
//
//        if(publicationTypeRepository.existsByName(publicationTypeName)){
//            responseObserver.onError(Status.ALREADY_EXISTS
//                    .withDescription("PublicationType \"" + publicationTypeName + "\" already exists.").asRuntimeException());
//            return;
//        }
//
//        PublicationType publicationType = new PublicationType();
//        publicationType.setName(publicationTypeName);
//
//        PublicationType saved = publicationTypeRepository.save(publicationType);
//
//
//        RefItem response = RefItem.newBuilder()
//                .setId(saved.getId())
//                .setName(saved.getName())
//                .build();
//
//        responseObserver.onNext(response);
//        responseObserver.onCompleted();
//    }
//
//
//    @Override
//    public void adminUpdateDiscipline(UpdateDisciplineRequest request, StreamObserver<RefItem> responseObserver) {
//
//        Long disciplineId = request.getId();
//        String disciplineName = request.getDisciplineName();
//
//        Discipline discipline = disciplineRepository.findById(disciplineId).orElseThrow(() -> new StatusRuntimeException(
//                Status.NOT_FOUND.withDescription("Discipline not found" + disciplineId)
//        ));
//
//        if(discipline.getName().equals(disciplineName)){
//            responseObserver.onError(Status.ALREADY_EXISTS
//                    .withDescription("Discipline with name " + disciplineName + "already exists").asRuntimeException());
//            return;
//        }
//
//        discipline.setName(disciplineName);
//        disciplineRepository.save(discipline);
//
//        RefItem refItem = RefItem.newBuilder().setId(discipline.getId()).setName(discipline.getName()).build();
//        responseObserver.onNext(refItem);
//        responseObserver.onCompleted();
//    }
//
//    @Override
//    @Transactional
//    public void adminUpdateEvalCycle(UpdateCycleRequest request, StreamObserver<CycleItem> responseObserver) {
//        Long evalId = request.getId();
//
//        EvalCycle cycle = evalCycleRepository.findById(evalId).orElseThrow(() -> new StatusRuntimeException(
//                Status.NOT_FOUND.withDescription("EvalCycle not found" + evalId)
//        ));
//
//        Set<String> paths = new HashSet<>(request.getUpdateMask().getPathsList());
//
//        String evalName = cycle.getName();
//        int yearFrom = cycle.getYearFrom();
//        int yearTo = cycle.getYearTo();
//        boolean isActive   = cycle.isActive();
//        Long meinVersionId = (cycle.getMeinVersion() != null ? cycle.getMeinVersion().getId() : null);
//
//        if (paths.contains("name"))     evalName = request.getName();
//        if (paths.contains("yearFrom"))  yearFrom = request.getYearFrom();
//        if (paths.contains("yearTo"))    yearTo = request.getYearTo();
//        if (paths.contains("isActive"))  isActive   = request.getIsActive();
//        if (paths.contains("meinVersionId")) {
//            long raw = request.getMeinVersionId();
//            meinVersionId = (raw > 0 ? raw : null);
//        }
//
//        if (paths.contains("name")) {
//            if(evalName == null || evalName.isEmpty()){
//                responseObserver.onError(Status.INVALID_ARGUMENT
//                        .withDescription("name must not be blank.").asRuntimeException());
//                return;
//            }
//            if (evalCycleRepository.existsByName(evalName)) {
//                responseObserver.onError(Status.ALREADY_EXISTS
//                        .withDescription("Evaluation cycle \"" + evalName + "\" already exists.").asRuntimeException());
//                return;
//            }
//        }
//
//        if (paths.contains("yearFrom") || paths.contains("yearTo")) {
//
//            if(evalCycleRepository.existsOverlappingExcludeId(evalId,yearFrom, yearTo)){
//                responseObserver.onError(Status.FAILED_PRECONDITION
//                        .withDescription("The provided year range overlaps an existing evaluation cycle.").asRuntimeException());
//                return;
//            }
//            if (yearFrom <= 0 || yearTo <= 0) {
//                responseObserver.onError(Status.INVALID_ARGUMENT
//                        .withDescription("yearFrom and yearTo must be positive.").asRuntimeException());
//                return;
//            }
//            if (yearFrom > yearTo) {
//                responseObserver.onError(Status.INVALID_ARGUMENT
//                        .withDescription("yearFrom cannot be greater than yearTo.").asRuntimeException());
//                return;
//            }
//
//        }
//
//        if(paths.contains("meinVersionId" )&& meinVersionId != null){
//            if (!meinVersionRepository.existsById(meinVersionId)) {
//                responseObserver.onError(Status.INVALID_ARGUMENT
//                        .withDescription("meinVersionId not found: " + meinVersionId).asRuntimeException());
//                return;
//            }
//        }
//
//        if(paths.contains("name"))      cycle.setName(evalName);
//        if (paths.contains("yearFrom")) cycle.setYearFrom(yearFrom);
//        if (paths.contains("yearTo"))   cycle.setYearTo(yearTo);
//        if (paths.contains("isActive")) cycle.setActive(isActive);
//        if (paths.contains("meinVersionId")) {
//            if (meinVersionId == null) {
//                cycle.setMeinVersion(null);
//            } else {
//                MeinVersion mv = meinVersionRepository.findById(meinVersionId).orElseThrow(() -> new RuntimeException("Mein Version not found"));
//                cycle.setMeinVersion(mv);
//            }
//        }
//
//        if (paths.contains("isActive") && isActive) {
//            evalCycleRepository.deactivateAllExcept(evalId);
//        }
//
//        evalCycleRepository.save(cycle);
//
//        CycleItem response = CycleItem.newBuilder()
//                .setId(cycle.getId())
//                .setName(cycle.getName())
//                .setYearFrom(cycle.getYearFrom())
//                .setYearTo(cycle.getYearTo())
//                .setIsActive(cycle.isActive())
//                .setMeinVersionId((cycle.getMeinVersion().getId() == null ? 0 : cycle.getMeinVersion().getId()))
//                .build();
//
//        responseObserver.onNext(response);
//        responseObserver.onCompleted();
//    }
//
//    @Override
//    public void adminUpdatePublicationType(UpdateTypeRequest request, StreamObserver<RefItem> responseObserver) {
//        Long publicationTypeId = request.getId();
//        String publicationTypeName = request.getName();
//
//        PublicationType publicationType = publicationTypeRepository.findById(publicationTypeId).orElseThrow(
//                () -> new StatusRuntimeException(Status.NOT_FOUND.withDescription("Publication type not found" + publicationTypeId)
//                ));
//
//        if(publicationType.getName().equals(publicationTypeName)){
//            responseObserver.onError(Status.ALREADY_EXISTS.withDescription("Publication Type with this name already exists").asRuntimeException());
//            return;
//        }
//
//        if(publicationTypeName == null || publicationTypeName.isEmpty()){
//            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Publication Type name cabbot be empty").asRuntimeException());
//            return;
//        }
//
//        publicationType.setName(publicationTypeName);
//        publicationTypeRepository.save(publicationType);
//
//        RefItem response = RefItem.newBuilder()
//                .setId(publicationType.getId())
//                .setName(publicationType.getName())
//                .build();
//
//        responseObserver.onNext(response);
//        responseObserver.onCompleted();
//    }
//
//
//    @Override
//    public void adminDeleteDiscipline(DeleteDisciplineRequest request, StreamObserver<ApiResponse> responseObserver) {
//        Long disciplineId = request.getId();
//
//        Discipline discipline = disciplineRepository.findById(disciplineId).orElseThrow(
//                () -> new StatusRuntimeException(Status.NOT_FOUND.withDescription("Discipline not found" + disciplineId)));
//
//        disciplineRepository.delete(discipline);
//
//        ApiResponse response = ApiResponse.newBuilder()
//                .setMessage("Discipline is deleted")
//                .setCode(200)
//                .build();
//
//        responseObserver.onNext(response);
//        responseObserver.onCompleted();
//    }
//
//    @Override
//    public void adminDeleteEvalCycle(DeleteCycleRequest request, StreamObserver<ApiResponse> responseObserver) {
//        Long evalCycleId = request.getId();
//
//        EvalCycle evalCycle = evalCycleRepository.findById(evalCycleId).orElseThrow(
//                () -> new StatusRuntimeException(Status.NOT_FOUND.withDescription("Evaluation cycle not found" + evalCycleId)));
//
//        evalCycleRepository.delete(evalCycle);
//
//        ApiResponse response = ApiResponse.newBuilder()
//                .setMessage("Evaluation Cycle is deleted")
//                .setCode(200)
//                .build();
//
//        responseObserver.onNext(response);
//        responseObserver.onCompleted();
//    }
//
//    @Override
//    public void adminDeletePublicationType(DeleteTypeRequest request, StreamObserver<ApiResponse> responseObserver) {
//        Long publicationTypeId = request.getId();
//
//        PublicationType publicationType = publicationTypeRepository.findById(publicationTypeId).orElseThrow(
//                () -> new StatusRuntimeException(Status.NOT_FOUND.withDescription("Publication type not found" + publicationTypeId)));
//
//        publicationTypeRepository.delete(publicationType);
//
//        ApiResponse response = ApiResponse.newBuilder()
//                .setMessage("Publication Type is deleted")
//                .setCode(200)
//                .build();
//
//        responseObserver.onNext(response);
//        responseObserver.onCompleted();
//    }

    /**
     * Functions for dropdowns
     */
    @Override
    public void listDisciplines(Empty request, StreamObserver<ListDisciplinesResponse> responseObserver) {
        List<Discipline> allDisciplines = disciplineRepository.findAll();

        ListDisciplinesResponse.Builder response = ListDisciplinesResponse.newBuilder();

        for(Discipline discipline : allDisciplines){
            response.addItems(RefItem.newBuilder()
                    .setId(discipline.getId())
                    .setName(discipline.getName())
                    .build());
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void listPublicationTypes(Empty request, StreamObserver<ListTypesResponse> responseObserver) {
        List<PublicationType> allPublicationTypes = publicationTypeRepository.findAll();

        ListTypesResponse.Builder response = ListTypesResponse.newBuilder();

        for(PublicationType publicationType : allPublicationTypes){
            response.addItems(RefItem.newBuilder()
                    .setId(publicationType.getId())
                    .setName(publicationType.getName())
                    .build());
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void listEvalCycles(Empty request, StreamObserver<ListCyclesResponse> responseObserver) {
        List<EvalCycle> allCycles = evalCycleRepository.findAll();

        ListCyclesResponse.Builder response = ListCyclesResponse.newBuilder();
        for(EvalCycle evalCycle : allCycles){
            response.addItems(CycleItem.newBuilder()
                    .setId(evalCycle.getId())
                    .setName(evalCycle.getName())
                    .setYearFrom(evalCycle.getYearFrom())
                    .setYearTo(evalCycle.getYearTo())
                    .setIsActive(evalCycle.isActive())
                    .build());

        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

//    /**
//     *  Private function for ListPublication
//     */
//    private void doList(StreamObserver<ListPublicationsResponse> responseObserver, Long authorId, long typeId, long disciplineId, long cycleId,
//                        int page, int size, String sortBy, String sortDir){
//
//        int pg = Math.max(0, page);
//        int sz = size > 0 ? Math.min(size, 100) : 20;
//
//        String sortProposition = switch(sortBy){
//            case "publicationYear" -> "publicationYear";
//            case "meinPoints"      -> "meinPoints";
//            case "createdAt"       -> "crqeatedAt";
//            default                -> "createdAt";
//        };
//
//        boolean desc = !"ASC".equalsIgnoreCase(sortDir);
//        Pageable pageable = PageRequest.of(pg, sz, desc ? Sort.by(sortProposition).descending() : Sort.by(sortProposition).ascending());
//
//        Specification<Publication> spec = PublicationSpecification.list(
//                authorId ,
//                typeId       > 0 ? typeId       : null,
//                disciplineId > 0 ? disciplineId : null,
//                cycleId      > 0 ? cycleId      : null
//        );
//
//        Page<Publication> pages = publicationRepository.findAll(spec, pageable);
//
//
//        PageMeta meta = PageMeta.newBuilder()
//                .setPage(pages.getNumber())
//                .setSize(pages.getSize())
//                .setTotalItems(pages.getTotalElements())
//                .setTotalPages(pages.getTotalPages())
//                .build();
//
//        ListPublicationsResponse.Builder resp = ListPublicationsResponse.newBuilder()
//                .setPage(meta);
//
//        for (Publication p : pages.getContent()) {
//            resp.addItems(entityToProto(p));
//        }
//
//        responseObserver.onNext(resp.build());
//        responseObserver.onCompleted();
//
//    }


}
