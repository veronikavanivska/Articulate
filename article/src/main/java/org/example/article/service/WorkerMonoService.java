package org.example.article.service;

import com.example.generated.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.example.article.entities.*;
import org.example.article.entities.MEiN.monographs.MonographAuthor;
import org.example.article.entities.MEiN.monographs.MonographChapter;
import org.example.article.entities.MEiN.monographs.MonographChapterAuthor;
import org.example.article.entities.MEiN.monographs.Monographic;
import org.example.article.helpers.ChapterSpecification;
import org.example.article.helpers.CommutePoints;
import org.example.article.helpers.MonographSpecification;
import org.example.article.repositories.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.example.article.ETL.IsbnUtil.normalizeIsbn13Strict;
import static org.example.article.helpers.Mapper.*;

@Service
public class WorkerMonoService extends WorkerMonographServiceGrpc.WorkerMonographServiceImplBase {


    private final MonographicRepository monographicRepository;
    private final CommutePoints commutePoints;
    private final PublicationTypeRepository publicationTypeRepository;
    private final DisciplineRepository disciplineRepository;
    private final MonographAuthorRepository monographAuthorRepository;
    private final OpenLibraryIsbnService openLibraryIsbnService;
    private final MonographChapterRepository monographChapterRepository;
    private final MonographChapterAuthorRepository monographChapterAuthorRepository;

    public WorkerMonoService(MonographicRepository monographicRepository, OpenLibraryIsbnService openLibraryIsbnService, CommutePoints commutePoints, PublicationTypeRepository publicationTypeRepository, DisciplineRepository disciplineRepository, MonographAuthorRepository monographAuthorRepository, MonographChapterRepository monographChapterRepository, MonographChapterAuthorRepository monographChapterAuthorRepository) {
        this.monographicRepository = monographicRepository;
        this.commutePoints = commutePoints;
        this.publicationTypeRepository = publicationTypeRepository;
        this.disciplineRepository = disciplineRepository;
        this.monographAuthorRepository = monographAuthorRepository;
        this.openLibraryIsbnService = openLibraryIsbnService;
        this.monographChapterRepository = monographChapterRepository;
        this.monographChapterAuthorRepository = monographChapterAuthorRepository;
    }

    @Override
    public void createMonograph(CreateMonographRequest request, StreamObserver<MonographView> responseObserver) {
        if (monographicRepository.existsByAuthorId(request.getUserId()) && monographicRepository.existsByTitle(request.getTitle())) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("You already added this monograph").asRuntimeException());
            return;
        }

        if (request.getInputCount() == 0) {
            throw Status.INVALID_ARGUMENT.withDescription("At least one author (including owner) must be provided").asRuntimeException();
        }

        if (request.getTitle().isBlank()) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Title must be provided").asRuntimeException());
            return;
        }
        if (request.getTypeId() <= 0 || request.getDisciplineId() <= 0) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Type and Discipline must be provided").asRuntimeException());
            return;
        }

        if (request.getPublicationYear() < 1900 || request.getPublicationYear() > 2100) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Publication year is not valid").asRuntimeException());
            return;
        }
        Optional<CoauthorInput> ownerEntryOpt = request.getInputList().stream()
                .filter(c -> c.getUserId() == request.getUserId())
                .findFirst();

        if (ownerEntryOpt.isEmpty()) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("You must include yourself in the coauthors list")
                    .asRuntimeException());
            return;
        }

//        if (request.getIsbn() != null && !request.getIsbn().isBlank()) {
//            boolean ok = openLibraryIsbnService.publisherMatches(
//                    request.getIsbn(),
//                    request.getMonograficPublisherTitle()
//            );
//
//            if (!ok) {
//                responseObserver.onError(
//                        Status.INVALID_ARGUMENT
//                                .withDescription("ISBN publisher does not match provided publisher title")
//                                .asRuntimeException()
//                );
//                return;
//            }
//        }

        CommuteResultMono result = commutePoints.commuteMono(request.getMonograficPublisherTitle(), request.getTypeId(), request.getPublicationYear());


        Monographic.MonographicBuilder builder = Monographic.builder()
                .authorId(request.getUserId())
                .type(publicationTypeRepository.findById(request.getTypeId()).orElseThrow(() -> new RuntimeException("Publication type not found")))
                .discipline(disciplineRepository.findById(request.getDisciplineId()).orElseThrow(() -> new RuntimeException("Discipline not found")))
                .cycle(result.cycle())
                .title(request.getTitle())
                .doi(request.getDoi())
                .isbn(normalizeIsbn13Strict(request.getIsbn()))
                .publicationYear(request.getPublicationYear())
                .monograficTitle(request.getMonograficPublisherTitle())
                .meinPoints(result.points());

        if (!result.offList()
                && result.meinMonoPublisher() != null
                && result.meinMonoVersion() != null) {
            builder.meinMonoPublisherId(result.meinMonoPublisher().getId());
            builder.meinMonoId(result.meinMonoVersion().getId());
        } else {
            builder.meinMonoPublisherId(null);
            builder.meinMonoId(null);
        }

        Monographic monograph = builder.build();

        monographicRepository.save(monograph);


        CoauthorInput ownerEntry = ownerEntryOpt.get();

        int pos = 1;

        MonographAuthor main = new MonographAuthor();
        main.setMonograph(monograph);
        main.setPosition(pos++);
        main.setUserId(request.getUserId());
        main.setInternal(true);
        main.setFullName(ownerEntry.getFullName());
        monographAuthorRepository.save(main);


        for (CoauthorInput c : request.getInputList()) {
            if (c.getUserId() == request.getUserId()
                    && Objects.equals(c.getFullName(), ownerEntry.getFullName())) {
                continue;
            }

            MonographAuthor co = new MonographAuthor();
            co.setMonograph(monograph);
            co.setPosition(pos++);
            co.setFullName(c.getFullName());

            if (c.getUserId() > 0) {
                co.setUserId(c.getUserId());
                co.setInternal(true);
            } else {
                co.setInternal(false);
            }

            monographAuthorRepository.save(co);
        }

        List<MonographAuthor> coauthors =
                monographAuthorRepository.findByMonographIdOrderByPosition(monograph.getId());
        monograph.setCoauthors(coauthors);


        MonographView monographView = entityToProtoMonograph(monograph);
        responseObserver.onNext(monographView);
        responseObserver.onCompleted();

    }
//
//    int64 userId = 1;
//
//    int64 typeId = 2;
//    int64 disciplineId = 3;
//    string monographChapterTitle = 4;
//    string monographTitle = 13;
//    string monographPublisherTitle = 15;
//    string doi = 5;
//    string isbn = 6;
//    int32 publicationYear = 7;
//
//    repeated CoauthorInput input = 9;
    @Override
    public void createChapter(CreateChapterRequest request, StreamObserver<ChapterView> responseObserver) {
        if (monographChapterRepository.existsByAuthorId(request.getUserId()) && monographChapterRepository.existsByMonograficChapterTitle(request.getMonographChapterTitle())) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("You already added this monograph").asRuntimeException());
            return;
        }

        if (request.getInputCount() == 0) {
            throw Status.INVALID_ARGUMENT.withDescription("At least one author (including owner) must be provided").asRuntimeException();
        }

        if (request.getMonographChapterTitle().isBlank()) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Chapter title must be provided").asRuntimeException());
            return;
        }
        if (request.getMonographTitle().isBlank()) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Title of monograph must be provided").asRuntimeException());
            return;
        }
        if (request.getMonographPublisherTitle().isBlank()) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Publisher of monograph must be provided").asRuntimeException());
            return;
        }

        if (request.getTypeId() <= 0 || request.getDisciplineId() <= 0) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Type and Discipline must be provided").asRuntimeException());
            return;
        }

        if (request.getPublicationYear() < 1900 || request.getPublicationYear() > 2100) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Publication year is not valid").asRuntimeException());
            return;
        }

        Optional<CoauthorInput> ownerEntryOpt = request.getInputList().stream()
                .filter(c -> c.getUserId() == request.getUserId())
                .findFirst();

        if (ownerEntryOpt.isEmpty()) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("You must include yourself in the coauthors list")
                    .asRuntimeException());
            return;
        }

        CommuteResultMonoChapter result = commutePoints.commuteChapter(request.getMonographPublisherTitle(), request.getTypeId(), request.getPublicationYear());

        MonographChapter.MonographChapterBuilder builder = MonographChapter.builder()
                .authorId(request.getUserId())
                .type(publicationTypeRepository.findById(request.getTypeId()).orElseThrow(() -> new RuntimeException("Publication type not found")))
                .discipline(disciplineRepository.findById(request.getDisciplineId()).orElseThrow(() -> new RuntimeException("Discipline not found")))
                .cycle(result.cycle())
                .monograficChapterTitle(request.getMonographChapterTitle())
                .doi(request.getDoi())
                .isbn(normalizeIsbn13Strict(request.getIsbn()))
                .publicationYear(request.getPublicationYear())
                .monograficTitle(request.getMonographTitle())
                .monographChapterPublisher(request.getMonographPublisherTitle())
                .meinPoints(result.points());

        if (!result.offList()
                && result.meinMonoPublisher() != null
                && result.meinMonoVersion() != null) {
            builder.meinMonoPublisherId(result.meinMonoPublisher().getId());
            builder.meinMonoId(result.meinMonoVersion().getId());
        } else {
            builder.meinMonoPublisherId(null);
            builder.meinMonoId(null);
        }

        MonographChapter monographChapter = builder.build();

        monographChapterRepository.save(monographChapter);


        CoauthorInput ownerEntry = ownerEntryOpt.get();

        int pos = 1;

        MonographChapterAuthor main = new MonographChapterAuthor();
        main.setMonographChapter(monographChapter);
        main.setPosition(pos++);
        main.setUserId(request.getUserId());
        main.setInternal(true);
        main.setFullName(ownerEntry.getFullName());
        monographChapterAuthorRepository.save(main);


        for (CoauthorInput c : request.getInputList()) {
            if (c.getUserId() == request.getUserId()
                    && Objects.equals(c.getFullName(), ownerEntry.getFullName())) {
                continue;
            }

            MonographChapterAuthor co = new MonographChapterAuthor();
            co.setMonographChapter(monographChapter);
            co.setPosition(pos++);
            co.setFullName(c.getFullName());

            if (c.getUserId() > 0) {
                co.setUserId(c.getUserId());
                co.setInternal(true);
            } else {
                co.setInternal(false);
            }

            monographChapterAuthorRepository.save(co);
        }

        List<MonographChapterAuthor> coauthors =
                monographChapterAuthorRepository.findByMonographChapterIdOrderByPosition(monographChapter.getId());
        monographChapter.setCoauthors(coauthors);


        ChapterView chapterView = entityToProtoChapter(monographChapter);
        responseObserver.onNext(chapterView);
        responseObserver.onCompleted();
    }

    @Override
    @Transactional
    public void getMonograph(GetMonographRequest request, StreamObserver<MonographView> responseObserver) {
        Monographic monograph = monographicRepository.findWithAllRelations(request.getId()).orElseThrow();

        if (!monograph.getAuthorId().equals(request.getUserId())) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("This is not yours monograph, you cannot see it, ha-ha-ha").asRuntimeException());
            return;
        }

        MonographView monographView = entityToProtoMonograph(monograph);
        responseObserver.onNext(monographView);
        responseObserver.onCompleted();
    }

    @Override
    public void getChapter(GetChapterRequest request, StreamObserver<ChapterView> responseObserver) {
        MonographChapter chapter = monographChapterRepository.findWithAllRelations(request.getId()).orElseThrow();

        if (!chapter.getAuthorId().equals(request.getUserId())) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("This is not yours chapter, you cannot see it, ha-ha-ha").asRuntimeException());
            return;
        }

        ChapterView chapterView = entityToProtoChapter(chapter);
        responseObserver.onNext(chapterView);
        responseObserver.onCompleted();
    }

    @Override
    @Transactional
    public void updateMonograph(UpdateMonographRequest request, StreamObserver<MonographView> responseObserver) {
        boolean changeForCommute = false;

        Monographic monographic = monographicRepository.findWithAllRelations(request.getId()).orElseThrow(() -> new RuntimeException("Publication not found"));
        if (!monographic.getAuthorId().equals(request.getUserId())) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("This is not yours monographic, you cannot see it, ha-ha-ha").asRuntimeException());
            return;
        }

        Set<String> paths = new HashSet<>(request.getUpdateMask().getPathsList());

        if (paths.contains("typeId")) {
            monographic.setType(publicationTypeRepository.findById(request.getTypeId()).orElseThrow(() -> new RuntimeException("Type id not found")));
            changeForCommute = true;
        }
        if (paths.contains("disciplineId")) {
            monographic.setDiscipline(disciplineRepository.findById(request.getDisciplineId()).orElseThrow());
        }
        if (paths.contains("title")) {
            String v = (request.getTitle());
            if (v == null || v.isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Title be provided").asRuntimeException());
                return;
            }
            monographic.setTitle(v);
        }

        if (paths.contains("doi")) monographic.setDoi(normalize(request.getDoi()));
        if (paths.contains("isbn")) {
            monographic.setIsbn(normalizeIsbn13Strict(request.getIsbn()));
        }

        if (paths.contains("monographPublisherTitle")) {
            monographic.setMonograficTitle(request.getMonographPublisherTitle());
            changeForCommute = true;
        }
        if (paths.contains("publicationYear")) {
            monographic.setPublicationYear(request.getPublicationYear());
            changeForCommute = true;
        }

        if (paths.contains("replaceCoauthors")) {
            long ownerId = monographic.getAuthorId();

            List<CoauthorInput> inputs = request.getReplaceCoauthorsList();

            Optional<CoauthorInput> ownerEntryOpt = inputs.stream()
                    .filter(c -> c.getUserId() == ownerId)
                    .findFirst();

            if (ownerEntryOpt.isEmpty()) {
                responseObserver.onError(
                        Status.INVALID_ARGUMENT
                                .withDescription("You must include yourself in coauthors when updating")
                                .asRuntimeException()
                );
                return;
            }

            CoauthorInput ownerEntry = ownerEntryOpt.get();

            monographAuthorRepository.deleteByMonographId(monographic.getId());

            List<MonographAuthor> newCoauthors = new ArrayList<>();
            int pos = 1;

            MonographAuthor main = new MonographAuthor();
            main.setMonograph(monographic);
            main.setPosition(pos++);
            main.setUserId(ownerId);
            main.setInternal(true);
            main.setFullName(ownerEntry.getFullName());
            newCoauthors.add(main);

            for (CoauthorInput c : inputs) {
                if (c.getUserId() == ownerId &&
                        Objects.equals(c.getFullName(), ownerEntry.getFullName())) {
                    continue;
                }

                MonographAuthor co = new MonographAuthor();
                co.setMonograph(monographic);
                co.setPosition(pos++);
                co.setFullName(c.getFullName());

                if (c.getUserId() > 0) {
                    co.setUserId(c.getUserId());
                    co.setInternal(true);
                } else {
                    co.setInternal(false);
                }

                newCoauthors.add(co);
            }

            monographAuthorRepository.saveAll(newCoauthors);
            monographic.setCoauthors(newCoauthors);
        }

        if (changeForCommute) {
            CommuteResultMono result = commutePoints.commuteMono(
                    monographic.getMonograficTitle(),
                    monographic.getType().getId(),
                    monographic.getPublicationYear()
            );

            monographic.setMeinPoints(result.points());
            monographic.setCycle(result.cycle());

            if (!result.offList()
                    && result.meinMonoPublisher() != null
                    && result.meinMonoVersion() != null) {
                monographic.setMeinMonoPublisherId(result.meinMonoPublisher().getId());
                monographic.setMeinMonoId(result.meinMonoVersion().getId());
            } else {
                monographic.setMeinMonoPublisherId(null);
                monographic.setMeinMonoId(null);
            }
        }

        monographicRepository.save(monographic);

        List<MonographAuthor> updatedCoauthors =
                monographAuthorRepository.findByMonographIdOrderByPosition(monographic.getId());
        monographic.setCoauthors(updatedCoauthors);

        MonographView monographView = entityToProtoMonograph(monographic);
        responseObserver.onNext(monographView);
        responseObserver.onCompleted();

    }

    @Override
    public void updateChapter(UpdateChapterRequest request, StreamObserver<ChapterView> responseObserver) {
        boolean changeForCommute = false;

        MonographChapter chapter = monographChapterRepository.findWithAllRelations(request.getId()).orElseThrow(() -> new RuntimeException("Chapter not found"));
        if (!chapter.getAuthorId().equals(request.getUserId())) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("This is not yours chapter, you cannot see it, ha-ha-ha").asRuntimeException());
            return;
        }

        Set<String> paths = new HashSet<>(request.getUpdateMask().getPathsList());

        if (paths.contains("typeId")) {
            chapter.setType(publicationTypeRepository.findById(request.getTypeId()).orElseThrow(() -> new RuntimeException("Type id not found")));
            changeForCommute = true;
        }
        if (paths.contains("disciplineId")) {
            chapter.setDiscipline(disciplineRepository.findById(request.getDisciplineId()).orElseThrow());
        }
        if (paths.contains("monograficChapterTitle")) {
            String v = (request.getMonograficChapterTitle());
            if (v == null || v.isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Title be provided").asRuntimeException());
                return;
            }
            chapter.setMonograficChapterTitle(v);
        }

        if (paths.contains("doi")) chapter.setDoi(normalize(request.getDoi()));
        if (paths.contains("isbn")) {
            chapter.setIsbn(normalizeIsbn13Strict(request.getIsbn()));
        }

        if (paths.contains("monograficTitle")) {
            chapter.setMonograficTitle(request.getMonograficTitle());
        }

        if (paths.contains("monographPublisher")) {
            chapter.setMonographChapterPublisher(request.getMonographPublisher());
            changeForCommute = true;
        }

        if (paths.contains("publicationYear")) {
            chapter.setPublicationYear(request.getPublicationYear());
            changeForCommute = true;
        }

        if (paths.contains("replaceCoauthors")) {
            long ownerId = chapter.getAuthorId();

            List<CoauthorInput> inputs = request.getReplaceCoauthorsList();

            Optional<CoauthorInput> ownerEntryOpt = inputs.stream()
                    .filter(c -> c.getUserId() == ownerId)
                    .findFirst();

            if (ownerEntryOpt.isEmpty()) {
                responseObserver.onError(
                        Status.INVALID_ARGUMENT
                                .withDescription("You must include yourself in coauthors when updating")
                                .asRuntimeException()
                );
                return;
            }

            CoauthorInput ownerEntry = ownerEntryOpt.get();

            monographChapterAuthorRepository.deleteByMonographChapterId(chapter.getId());

            List<MonographChapterAuthor> newCoauthors = new ArrayList<>();
            int pos = 1;

            MonographChapterAuthor main = new MonographChapterAuthor();
            main.setMonographChapter(chapter);
            main.setPosition(pos++);
            main.setUserId(ownerId);
            main.setInternal(true);
            main.setFullName(ownerEntry.getFullName());
            newCoauthors.add(main);

            for (CoauthorInput c : inputs) {
                if (c.getUserId() == ownerId &&
                        Objects.equals(c.getFullName(), ownerEntry.getFullName())) {
                    continue;
                }

                MonographChapterAuthor co = new MonographChapterAuthor();
                co.setMonographChapter(chapter);
                co.setPosition(pos++);
                co.setFullName(c.getFullName());

                if (c.getUserId() > 0) {
                    co.setUserId(c.getUserId());
                    co.setInternal(true);
                } else {
                    co.setInternal(false);
                }

                newCoauthors.add(co);
            }

            monographChapterAuthorRepository.saveAll(newCoauthors);
            chapter.setCoauthors(newCoauthors);
        }

        if (changeForCommute) {
            CommuteResultMonoChapter result = commutePoints.commuteChapter(
                    chapter.getMonographChapterPublisher(),
                    chapter.getType().getId(),
                    chapter.getPublicationYear()
            );

            chapter.setMeinPoints(result.points());
            chapter.setCycle(result.cycle());

            if (!result.offList()
                    && result.meinMonoPublisher() != null
                    && result.meinMonoVersion() != null) {
                chapter.setMeinMonoPublisherId(result.meinMonoPublisher().getId());
                chapter.setMeinMonoId(result.meinMonoVersion().getId());
            } else {
                chapter.setMeinMonoPublisherId(null);
                chapter.setMeinMonoId(null);
            }
        }

        monographChapterRepository.save(chapter);

        List<MonographChapterAuthor> updatedCoauthors =
                monographChapterAuthorRepository.findByMonographChapterIdOrderByPosition(chapter.getId());
        chapter.setCoauthors(updatedCoauthors);

        ChapterView chapterView = entityToProtoChapter(chapter);
        responseObserver.onNext(chapterView);
        responseObserver.onCompleted();
    }

    @Override
    public void listMyMonographs(ListMonographsRequest request, StreamObserver<ListMonographsResponse> responseObserver) {
        doList(responseObserver, request.getUserId(), request.getTypeId(), request.getDisciplineId(), request.getCycleId(),
                request.getPage(), request.getSize(), request.getSortBy(), request.getSortDir());
    }

    @Override
    public void listMyChapters(ListChaptersRequest request, StreamObserver<ListChaptersResponse> responseObserver) {
        doListChapter(responseObserver, request.getUserId(), request.getTypeId(), request.getDisciplineId(), request.getCycleId(),
                request.getPage(), request.getSize(), request.getSortBy(), request.getSortDir());
    }

    private void doList(StreamObserver<ListMonographsResponse> responseObserver, Long authorId, long typeId, long disciplineId, long cycleId,
                        int page, int size, String sortBy, String sortDir) {

        int pg = Math.max(0, page);
        int sz = size > 0 ? Math.min(size, 100) : 20;

        String sortProposition = switch (sortBy) {
            case "publicationYear" -> "publicationYear";
            case "meinPoints" -> "meinPoints";
            case "createdAt" -> "createdAt";
            default -> "createdAt";
        };

        boolean desc = !"ASC".equalsIgnoreCase(sortDir);
        Pageable pageable = PageRequest.of(pg, sz, desc ? Sort.by(sortProposition).descending() : Sort.by(sortProposition).ascending());

        Specification<Monographic> spec = MonographSpecification.list(
                authorId,
                typeId > 0 ? typeId : null,
                disciplineId > 0 ? disciplineId : null,
                cycleId > 0 ? cycleId : null
        );

        Page<Monographic> pages = monographicRepository.findAll(spec, pageable);

        PageMeta meta = PageMeta.newBuilder()
                .setPage(pages.getNumber())
                .setSize(pages.getSize())
                .setTotalItems(pages.getTotalElements())
                .setTotalPages(pages.getTotalPages())
                .build();

        ListMonographsResponse.Builder resp = ListMonographsResponse.newBuilder()
                .setPageMeta(meta);

        for (Monographic m : pages.getContent()) {
            resp.addMonoghraficView(entityToProtoMonograph(m));
        }

        responseObserver.onNext(resp.build());
        responseObserver.onCompleted();
    }

    private void doListChapter(StreamObserver<ListChaptersResponse> responseObserver, Long authorId, long typeId, long disciplineId, long cycleId,
                        int page, int size, String sortBy, String sortDir) {

        int pg = Math.max(0, page);
        int sz = size > 0 ? Math.min(size, 100) : 20;

        String sortProposition = switch (sortBy) {
            case "publicationYear" -> "publicationYear";
            case "meinPoints" -> "meinPoints";
            case "createdAt" -> "createdAt";
            default -> "createdAt";
        };

        boolean desc = !"ASC".equalsIgnoreCase(sortDir);
        Pageable pageable = PageRequest.of(pg, sz, desc ? Sort.by(sortProposition).descending() : Sort.by(sortProposition).ascending());

        Specification<MonographChapter> spec = ChapterSpecification.list(
                authorId,
                typeId > 0 ? typeId : null,
                disciplineId > 0 ? disciplineId : null,
                cycleId > 0 ? cycleId : null
        );

        Page<MonographChapter> pages = monographChapterRepository.findAll(spec, pageable);

        PageMeta meta = PageMeta.newBuilder()
                .setPage(pages.getNumber())
                .setSize(pages.getSize())
                .setTotalItems(pages.getTotalElements())
                .setTotalPages(pages.getTotalPages())
                .build();

        ListChaptersResponse.Builder resp = ListChaptersResponse.newBuilder()
                .setPageMeta(meta);

        for (MonographChapter m : pages.getContent()) {
            resp.addChapterView(entityToProtoChapter(m));
        }

        responseObserver.onNext(resp.build());
        responseObserver.onCompleted();
    }


    @Override
    public void deleteMonograph(DeleteMonographRequest request, StreamObserver<ApiResponse> responseObserver) {
        Monographic monographic = monographicRepository.findById(request.getId())
                .orElseThrow(() -> new IllegalArgumentException("Monograph not found"));

        if (!monographic.getAuthorId().equals(request.getUserId())) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("This is not yours monograph, you cannot delete it, ha-ha-ha").asRuntimeException());
            return;
        }

        monographicRepository.deleteById(monographic.getId());

        ApiResponse response = ApiResponse.newBuilder().setCode(200).setMessage("Deleted").build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteChapter(DeleteChapterRequest request, StreamObserver<ApiResponse> responseObserver) {
        MonographChapter chapter = monographChapterRepository.findById(request.getId())
                .orElseThrow(() -> new IllegalArgumentException("Chapter not found"));

        if (!chapter.getAuthorId().equals(request.getUserId())) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("This is not yours chapter, you cannot delete it, ha-ha-ha").asRuntimeException());
            return;
        }

        monographChapterRepository.deleteById(chapter.getId());

        ApiResponse response = ApiResponse.newBuilder().setCode(200).setMessage("Deleted").build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}

