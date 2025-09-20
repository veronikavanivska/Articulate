package org.example.article.service;

import com.example.generated.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.example.article.ETL.ETLService;
import org.example.article.entities.CommuteResult;
import org.example.article.entities.Publication;
import org.example.article.entities.PublicationCoauthor;
import org.example.article.repositories.DisciplineRepository;
import org.example.article.repositories.PublicationRepository;
import org.example.article.repositories.PublicationTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class AricleService extends ArticleServiceGrpc.ArticleServiceImplBase {

    final private ETLService ETLService;
    private final PublicationTypeRepository publicationTypeRepository;
    private final CommutePoints commutePoints;
    private final DisciplineRepository disciplineRepository;
    private final PublicationRepository publicationRepository;

    public AricleService(ETLService ETLService, PublicationTypeRepository publicationTypeRepository, CommutePoints commutePoints, DisciplineRepository disciplineRepository, PublicationRepository publicationRepository, RestClient.Builder builder) {
        this.ETLService = ETLService;
        this.publicationTypeRepository = publicationTypeRepository;
        this.commutePoints = commutePoints;
        this.disciplineRepository = disciplineRepository;
        this.publicationRepository = publicationRepository;
    }
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

        CommuteResult result = commutePoints.commute(request.getTypeId(),request.getDisciplineId(),request.getIssn(),request.getEissn(),request.getPublicationYear());

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

        Publication saved =  publicationRepository.save(publication);

        RefItem type = RefItem.newBuilder()
                .setId(saved.getType().getId())
                .setName(saved.getType().getName())
                .build();

        RefItem discipline = RefItem.newBuilder()
                .setId(saved.getDiscipline().getId())
                .setName(saved.getDiscipline().getName())
                .build();

        CycleItem cycle = CycleItem.newBuilder()
                .setName(saved.getCycle().getName())
                .setIsActive(saved.getCycle().isActive())
                .setId(saved.getCycle().getId())
                .setYearFrom(saved.getCycle().getYearFrom())
                .setYearTo(saved.getCycle().getYearTo())
                .build();

        PublicationView.Builder b = PublicationView.newBuilder()
                .setId(saved.getId())
                .setOwnerId(saved.getAuthorId())
                .setTitle(saved.getTitle())
                .setDoi(backfromnorm(saved.getDoi()))
                .setIssn(backfromnorm(saved.getIssn()))
                .setEissn(backfromnorm(saved.getEissn()))
                .setJournalTitle(saved.getJournalTitle())
                .setPublicationYear(saved.getPublicationYear())
                .setMeinPoints(saved.getMeinPoints())
                .setMeinVersionId(saved.getMeinVersionId())
                .setMeinJournalId(saved.getMeinJournalId())
                .setCycle(cycle)
                .setType(type)
                .setDiscipline(discipline);


            saved.getCoauthors().stream()
                    .sorted(java.util.Comparator.comparingInt(PublicationCoauthor::getPosition))
                    .forEach(c -> b.addCoauthors(
                            Coauthor.newBuilder()
                                    .setPosition(c.getPosition())
                                    .setFullName(backfromnorm(c.getFullName()))
                                    .build()
                    ));


        PublicationView publicationView = b.build();
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

        RefItem type = RefItem.newBuilder()
                .setId(publication.getType().getId())
                .setName(publication.getType().getName())
                .build();

        RefItem discipline = RefItem.newBuilder()
                .setId(publication.getDiscipline().getId())
                .setName(publication.getDiscipline().getName())
                .build();

        CycleItem cycle = CycleItem.newBuilder()
                .setName(publication.getCycle().getName())
                .setIsActive(publication.getCycle().isActive())
                .setId(publication.getCycle().getId())
                .setYearFrom(publication.getCycle().getYearFrom())
                .setYearTo(publication.getCycle().getYearTo())
                .build();

        PublicationView.Builder b = PublicationView.newBuilder()
                .setId(publication.getId())
                .setOwnerId(publication.getAuthorId())
                .setTitle(publication.getTitle())
                .setDoi(backfromnorm(publication.getDoi()))
                .setIssn(backfromnorm(publication.getIssn()))
                .setEissn(backfromnorm(publication.getEissn()))
                .setJournalTitle(publication.getJournalTitle())
                .setPublicationYear(publication.getPublicationYear())
                .setMeinPoints(publication.getMeinPoints())
                .setMeinVersionId(publication.getMeinVersionId())
                .setMeinJournalId(publication.getMeinJournalId())
                .setCycle(cycle)
                .setType(type)
                .setDiscipline(discipline);


        publication.getCoauthors().stream()
                .sorted(java.util.Comparator.comparingInt(PublicationCoauthor::getPosition))
                .forEach(c -> b.addCoauthors(
                        Coauthor.newBuilder()
                                .setPosition(c.getPosition())
                                .setFullName(backfromnorm(c.getFullName()))
                                .build()
                ));


        PublicationView publicationView = b.build();
        responseObserver.onNext(publicationView);
        responseObserver.onCompleted();
    }



    private static   String normalize(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private static String backfromnorm(String s) {
        return s == null ? "" : s;
    }


}
