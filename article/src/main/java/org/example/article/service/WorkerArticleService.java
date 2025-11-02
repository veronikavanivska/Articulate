package org.example.article.service;

import com.example.generated.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.example.article.ETL.ETLService;
import org.example.article.entities.CommuteResult;
import org.example.article.entities.Publication;
import org.example.article.entities.PublicationCoauthor;
import org.example.article.helpers.CommutePoints;
import org.example.article.helpers.PublicationSpecification;
import org.example.article.repositories.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.example.article.helpers.Mapper.*;

@Service
public class WorkerArticleService extends WorkerArticleServiceGrpc.WorkerArticleServiceImplBase {


    private final PublicationTypeRepository publicationTypeRepository;
    private final CommutePoints commutePoints;
    private final DisciplineRepository disciplineRepository;
    private final PublicationRepository publicationRepository;


    public WorkerArticleService( PublicationTypeRepository publicationTypeRepository, CommutePoints commutePoints, DisciplineRepository disciplineRepository, PublicationRepository publicationRepository ) {
        this.publicationTypeRepository = publicationTypeRepository;
        this.commutePoints = commutePoints;
        this.disciplineRepository = disciplineRepository;
        this.publicationRepository = publicationRepository;
    }


    /**
     * Worker functions
     * <p>
     * 1) createPublication
     * </p><p>
     * 2) getPublication
     * </p>
     * <p>
     * 3) updatePublication
     * </p> <p>
     * 4) DeletePublication
     * </p> <p>
     * 5) ListMyPublications(with filters and sorting)
     * </p>
     */
    @Override
    public void createPublication(CreatePublicationRequest request, StreamObserver<PublicationView> responseObserver) {
        if(publicationRepository.existsByAuthorId(request.getUserId())&&
                publicationRepository.existsByTitle(request.getTitle())){
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("You already added this article").asRuntimeException());
            return;

        }
        if (request.getCoauthorsCount() == 0) {
            throw Status.INVALID_ARGUMENT.withDescription("At least one author (including owner) must be provided").asRuntimeException();
        }
        if (request.getTitle().isBlank()) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Title cannot be empty").asRuntimeException());
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

        if (backfromnorm(request.getIssn()).isBlank() && backfromnorm(request.getEissn()).isBlank()){
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Either ISSN or eISSN must be provided").asRuntimeException());
            return;
        }

        CommuteResult result = commutePoints.commute(request.getJournalTitle(),request.getTypeId(),request.getDisciplineId(),request.getIssn(),request.getEissn(),request.getPublicationYear());

        Publication publication = Publication.builder()
                .authorId(request.getUserId())
                .type(publicationTypeRepository.findById(request.getTypeId()).orElseThrow())
                .title(request.getTitle())
                .doi(normalize(request.getDoi()))
                .issn(normalize(request.getIssn()))
                .eissn(normalize(request.getEissn()))
                .journalTitle(request.getJournalTitle())
                .publicationYear(request.getPublicationYear())
                .cycle(result.cycle())
                .discipline(disciplineRepository.findById(request.getDisciplineId()).orElseThrow())
                .meinPoints(result.points())
                .meinVersionId(result.meinVersion().getId())
                .meinJournalId(result.meinJournal().getId())
                .build();

        if (request.getCoauthorsCount() > 0) {
            List<PublicationCoauthor> authors = new ArrayList<>(request.getCoauthorsCount());
            for (int i = 0; i < request.getCoauthorsCount(); i++) {
                authors.add(PublicationCoauthor.builder()
                        .publication(publication)
                        .position(i + 1)
                        .fullName(request.getCoauthors(i))
                        .build());
            }
            publication.getCoauthors().clear();
            publication.getCoauthors().addAll(authors);
        }

        publicationRepository.save(publication);

        PublicationView publicationView = entityToProto(publication);
        responseObserver.onNext(publicationView);
        responseObserver.onCompleted();

    }

    @Override
    @Transactional
    public void getPublication(GetPublicationRequest request, StreamObserver<PublicationView> responseObserver) {

        Publication publication = publicationRepository.findWithAllRelations(request.getId()).orElseThrow();

        if(!publication.getAuthorId().equals(request.getUserId())){
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("This is not yours publication, you cannot see it, ha-ha-ha").asRuntimeException());
            return;
        }

        PublicationView publicationView = entityToProto(publication);
        responseObserver.onNext(publicationView);
        responseObserver.onCompleted();
    }

    @Override
    @Transactional
    public void updatePublication(UpdatePublicationRequest request, StreamObserver<PublicationView> responseObserver) {

        boolean changeForCommute = false;

        Publication publication = publicationRepository.findWithAllRelations(request.getId()).orElseThrow();
        if(!publication.getAuthorId().equals(request.getUserId())){
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("This is not yours publication, you cannot see it, ha-ha-ha").asRuntimeException());
            return;
        }

        Set<String> paths = new HashSet<>(request.getUpdateMask().getPathsList());

        if (paths.contains("typeId"))
        {
            publication.setType(publicationTypeRepository.findById(request.getTypeId()).orElseThrow());
            changeForCommute = true;
        }
        if (paths.contains("disciplineId")){
            publication.setDiscipline(disciplineRepository.findById(request.getDisciplineId()).orElseThrow());
            changeForCommute = true;
        }
        if (paths.contains("title")) {
            String v = (request.getTitle());
            if(v == null || v.isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Type and Discipline must be provided").asRuntimeException());
                return;}
            publication.setTitle(v);
        }

        if (paths.contains("doi"))           publication.setDoi(normalize(request.getDoi()));
        if (paths.contains("issn")) {
            publication.setIssn(normalize(request.getIssn()));
            changeForCommute = true;
        }
        if (paths.contains("eissn")) {
            publication.setEissn(normalize(request.getEissn()));
            changeForCommute = true;
        }
        if (paths.contains("journalTitle"))  {
            publication.setJournalTitle(request.getJournalTitle());
            changeForCommute = true;
        }
        if (paths.contains("publicationYear")){
            publication.setPublicationYear(request.getPublicationYear());
            changeForCommute = true;
        }

        if (paths.contains("coauthors")) {
            List<PublicationCoauthor> authors = new ArrayList<>(request.getReplaceCoauthorsCount());
            for (int i = 0; i < request.getReplaceCoauthorsCount(); i++) {
                authors.add(PublicationCoauthor.builder()
                        .publication(publication)
                        .position(i + 1)
                        .fullName(request.getReplaceCoauthors(i))
                        .build());
            }
            publication.getCoauthors().clear();
            publication.getCoauthors().addAll(authors);
        }


        if(changeForCommute){
            CommuteResult result = commutePoints.commute(publication.getJournalTitle(),publication.getType().getId(),publication.getDiscipline().getId(),publication.getIssn(),publication.getEissn(),publication.getPublicationYear());

            if (result.meinJournal() == null || result.meinVersion() == null) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("ISSN/eISSN/title/year/discipline do not match an active MEiN journal.")
                        .asRuntimeException());
                return;
            }

            publication.setMeinPoints(result.points());
            publication.setCycle(result.cycle());
            publication.setMeinJournalId(result.meinJournal().getId() );
            publication.setMeinVersionId(result.meinVersion().getId());
        }

        publicationRepository.save(publication);


        PublicationView publicationView = entityToProto(publication);
        responseObserver.onNext(publicationView);
        responseObserver.onCompleted();

    }

    @Override
    public void deletePublication(DeletePublicationRequest request, StreamObserver<ApiResponse> responseObserver) {


        Publication publication = publicationRepository.findById(request.getId()).orElseThrow(() -> new IllegalArgumentException("Publication not found"));

        if(!publication.getAuthorId().equals(request.getUserId())){
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("This is not yours publication, you cannot delete it, ha-ha-ha").asRuntimeException());
            return;
        }

        publicationRepository.delete(publication);

        ApiResponse response = ApiResponse.newBuilder().setCode(200).setMessage("Deleted").build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }

    @Override
    public void listMyPublications(ListPublicationsRequest request, StreamObserver<ListPublicationsResponse> responseObserver) {
        doList(responseObserver , request.getUserId() , request.getTypeId(), request.getDisciplineId(), request.getCycleId(),
                request.getPage(), request.getSize(), request.getSortBy() , request.getSortDir());
    }

    private void doList(StreamObserver<ListPublicationsResponse> responseObserver, Long authorId, long typeId, long disciplineId, long cycleId,
                        int page, int size, String sortBy, String sortDir){

        int pg = Math.max(0, page);
        int sz = size > 0 ? Math.min(size, 100) : 20;

        String sortProposition = switch(sortBy){
            case "publicationYear" -> "publicationYear";
            case "meinPoints"      -> "meinPoints";
            case "createdAt"       -> "crqeatedAt";
            default                -> "createdAt";
        };

        boolean desc = !"ASC".equalsIgnoreCase(sortDir);
        Pageable pageable = PageRequest.of(pg, sz, desc ? Sort.by(sortProposition).descending() : Sort.by(sortProposition).ascending());

        Specification<Publication> spec = PublicationSpecification.list(
                authorId ,
                typeId       > 0 ? typeId       : null,
                disciplineId > 0 ? disciplineId : null,
                cycleId      > 0 ? cycleId      : null
        );

        Page<Publication> pages = publicationRepository.findAll(spec, pageable);


        PageMeta meta = PageMeta.newBuilder()
                .setPage(pages.getNumber())
                .setSize(pages.getSize())
                .setTotalItems(pages.getTotalElements())
                .setTotalPages(pages.getTotalPages())
                .build();

        ListPublicationsResponse.Builder resp = ListPublicationsResponse.newBuilder()
                .setPage(meta);

        for (Publication p : pages.getContent()) {
            resp.addItems(entityToProto(p));
        }

        responseObserver.onNext(resp.build());
        responseObserver.onCompleted();

    }
}
