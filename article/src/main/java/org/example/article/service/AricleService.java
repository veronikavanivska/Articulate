package org.example.article.service;

import com.example.generated.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.example.article.ETL.ETLService;
import org.example.article.entities.CommuteResult;
import org.example.article.entities.Publication;
import org.example.article.entities.PublicationCoauthor;
import org.example.article.helpers.CommutePoints;
import org.example.article.repositories.DisciplineRepository;
import org.example.article.repositories.PublicationRepository;
import org.example.article.repositories.PublicationTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.example.article.helpers.Mapper.*;

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

    /**
    * Worker functions
     * <p>
     * 1) createPublication
     * 2) getPublication
     * 3) updatePublication
     * 4) DeletePublication
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

    }
}
