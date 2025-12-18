package org.example.article.service;

import com.example.generated.*;
import io.grpc.stub.StreamObserver;
import org.example.article.entities.MEiN.monographs.MonographChapter;
import org.example.article.entities.MEiN.monographs.Monographic;
import org.example.article.helpers.ChapterSpecification;
import org.example.article.helpers.MonographSpecification;
import org.example.article.repositories.MonographChapterRepository;
import org.example.article.repositories.MonographicRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import static org.example.article.helpers.Mapper.*;

@Service
public class AdminMonographService extends AdminMonographServiceGrpc.AdminMonographServiceImplBase {

    private final MonographChapterRepository monographChapterRepository;
    private final MonographicRepository monographicRepository;

    public AdminMonographService(MonographChapterRepository monographChapterRepository, MonographicRepository monographicRepository) {
        super();
        this.monographChapterRepository = monographChapterRepository;
        this.monographicRepository = monographicRepository;
    }

    @Override
    public void adminListMonographs(ListAdminMonographsRequest request, StreamObserver<ListMonographsResponse> responseObserver) {
        Long authorId = request.getOwnerId() > 0 ? request.getOwnerId() : null;
        doList(responseObserver , authorId , request.getTypeId(), request.getDisciplineId(), request.getCycleId(),
                request.getPage(), request.getSize(), request.getSortBy() , request.getSortDir());

    }

    @Override
    public void adminListChapters(ListAdminChaptersRequest request, StreamObserver<ListChaptersResponse> responseObserver) {
        Long authorId = request.getOwnerId() > 0 ? request.getOwnerId() : null;
        doListChapter(responseObserver , authorId , request.getTypeId(), request.getDisciplineId(), request.getCycleId(),
                request.getPage(), request.getSize(), request.getSortBy() , request.getSortDir());

    }

    @Override
    public void adminGetMonograph(GetMonographRequest request, StreamObserver<MonographView> responseObserver) {
        Monographic monographic = monographicRepository.findWithAllRelations(request.getId()).orElseThrow();
        MonographView view = entityToProtoMonograph(monographic);
        responseObserver.onNext(view);
        responseObserver.onCompleted();

    }

    public void adminGetChapter(GetChapterRequest request, StreamObserver<ChapterView> responseObserver) {
        MonographChapter chapter = monographChapterRepository.findWithAllRelations(request.getId()).orElseThrow();
        ChapterView view = entityToProtoChapter(chapter);
        responseObserver.onNext(view);
        responseObserver.onCompleted();
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


}
