package org.example.article.service;

import com.example.generated.*;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.example.article.entities.Discipline;
import org.example.article.entities.EvalCycle;
import org.example.article.entities.MEiN.article.MeinVersion;
import org.example.article.entities.MEiN.monographs.MeinMonoVersion;
import org.example.article.entities.Publication;
import org.example.article.entities.PublicationType;
import org.example.article.helpers.PublicationSpecification;
import org.example.article.repositories.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static org.example.article.helpers.Mapper.entityToProto;

@Service
public class AdminArticleService extends AdminArticleServiceGrpc.AdminArticleServiceImplBase {

    private final PublicationTypeRepository publicationTypeRepository;
    private final DisciplineRepository disciplineRepository;
    private final PublicationRepository publicationRepository;
    private final EvalCycleRepository evalCycleRepository;
    private final MeinVersionRepository meinVersionRepository;
    private final MeinMonoVersionRepository meinMonoVersionRepository;


    public AdminArticleService(PublicationTypeRepository publicationTypeRepository, DisciplineRepository disciplineRepository, PublicationRepository publicationRepository, EvalCycleRepository evalCycleRepository, MeinVersionRepository meinVersionRepository, MeinMonoVersionRepository meinMonoVersionRepository) {

        this.publicationTypeRepository = publicationTypeRepository;

        this.disciplineRepository = disciplineRepository;
        this.publicationRepository = publicationRepository;
        this.evalCycleRepository = evalCycleRepository;
        this.meinVersionRepository = meinVersionRepository;
        this.meinMonoVersionRepository = meinMonoVersionRepository;
    }

    @Override
    public void adminListPublications(ListAdminPublicationRequest request, StreamObserver<ListPublicationsResponse> responseObserver) {
        Long authorId = request.getOwnerId() > 0 ? request.getOwnerId() : null;
        doList(responseObserver , authorId , request.getTypeId(), request.getDisciplineId(), request.getCycleId(),
                request.getPage(), request.getSize(), request.getSortBy() , request.getSortDir());
    }

    @Override
    public void adminGetPublication(GetPublicationRequest request, StreamObserver<PublicationView> responseObserver) {
        Publication publication = publicationRepository.findWithAllRelations(request.getId()).orElseThrow();

        PublicationView publicationView = entityToProto(publication);
        responseObserver.onNext(publicationView);
        responseObserver.onCompleted();
    }



    @Override
    public void adminListDisciplines(AdminListDisciplinesRequest request, StreamObserver<AdminListDisciplinesResponse> responseObserver) {
        int pg = Math.max(0, request.getPage());
        int sz = request.getSize() > 0 ? Math.min(request.getSize(), 100) : 20;

        boolean desc = !"ASC".equalsIgnoreCase(request.getSortDir());
        Pageable pageable = PageRequest.of(pg, sz, desc ? Sort.by("name").descending() : Sort.by("name").ascending());

        Page<Discipline> page = disciplineRepository.findAll(pageable);

        PageMeta pageMeta = PageMeta.newBuilder()
                .setSize(page.getSize())
                .setPage(page.getNumber())
                .setTotalItems(page.getTotalElements())
                .setTotalPages(page.getTotalPages())
                .build();

        AdminListDisciplinesResponse.Builder response = AdminListDisciplinesResponse.newBuilder()
                .setPage(pageMeta);

        for(Discipline discipline : page.getContent()){
            response.addItems(RefItem.newBuilder()
                    .setId(discipline.getId())
                    .setName(discipline.getName())
                    .build());
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }


    @Override
    public void adminListEvalCycles(AdminListCyclesRequest request, StreamObserver<AdminListCyclesResponse> responseObserver) {
        int pg = Math.max(0, request.getPage());
        int sz = request.getSize() > 0 ? Math.min(request.getSize(), 100) : 20;

        boolean desc = !"ASC".equalsIgnoreCase(request.getSortDir());
        Pageable pageable = PageRequest.of(pg, sz, desc ? Sort.by("name").descending() : Sort.by("name").ascending());

        Page<EvalCycle> page = evalCycleRepository.findAll(pageable);

        PageMeta pageMeta = PageMeta.newBuilder()
                .setSize(page.getSize())
                .setPage(page.getNumber())
                .setTotalItems(page.getTotalElements())
                .setTotalPages(page.getTotalPages())
                .build();

        AdminListCyclesResponse.Builder response = AdminListCyclesResponse.newBuilder()
                .setPage(pageMeta);

        for(EvalCycle evalCycle : page.getContent()){
            long mvId = 0L;

            if (evalCycle.getMeinVersion() != null && evalCycle.getMeinVersion().getId() != null) {
                mvId = evalCycle.getMeinVersion().getId();
            }
            response.addItems(CycleItem.newBuilder()
                    .setId(evalCycle.getId())
                    .setName(evalCycle.getName())
                    .setYearFrom(evalCycle.getYearFrom())
                    .setYearTo(evalCycle.getYearTo())
                    .setIsActive(evalCycle.isActive())
                    .setMeinVersionId(mvId)
                    .build());
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void adminListPublicationTypes(AdminListTypesRequest request, StreamObserver<AdminListTypesResponse> responseObserver) {
        int pg = Math.max(0, request.getPage());
        int sz = request.getSize() > 0 ? Math.min(request.getSize(), 100) : 20;

        boolean desc = !"ASC".equalsIgnoreCase(request.getSortDir());
        Pageable pageable = PageRequest.of(pg, sz, desc ? Sort.by("name").descending() : Sort.by("name").ascending());

        Page<PublicationType> page = publicationTypeRepository.findAll(pageable);
        PageMeta pageMeta = PageMeta.newBuilder()
                .setSize(page.getSize())
                .setPage(page.getNumber())
                .setTotalItems(page.getTotalElements())
                .setTotalPages(page.getTotalPages())
                .build();

        AdminListTypesResponse.Builder response = AdminListTypesResponse.newBuilder().setPage(pageMeta);

        for(PublicationType publicationType : page.getContent()){
            response.addItems(RefItem.newBuilder()
                    .setId(publicationType.getId())
                    .setName(publicationType.getName())
                    .build());
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }


    @Override
    public void adminCreateDiscipline(CreateDisciplineRequest request, StreamObserver<RefItem> responseObserver) {
        String disciplineName = request.getDisciplineName();

        if(disciplineName.isEmpty()){
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("disciplineName is required.").asRuntimeException());
            return;
        }

        if(disciplineRepository.existsByName(disciplineName)){
            responseObserver.onError(Status.ALREADY_EXISTS
                    .withDescription("Discipline \"" + disciplineName + "\" already exists.").asRuntimeException());
            return;
        }

        Discipline d = new Discipline();
        d.setName(disciplineName);

        Discipline saved = disciplineRepository.save(d);


        RefItem response = RefItem.newBuilder()
                .setId(saved.getId())
                .setName(saved.getName())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    @Transactional
    @Override
    public void adminCreateEvalCycle(CreateCycleRequest request, StreamObserver<CycleItem> responseObserver) {

        String evalName = request.getName();
        int yearFrom = request.getYearFrom();
        int yearTo = request.getYearTo();
        boolean active = request.getIsActive();

        if (evalName.isEmpty()) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("name is required.").asRuntimeException());
            return;
        }
        if (yearFrom <= 0 || yearTo <= 0) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("yearFrom and yearTo must be positive.").asRuntimeException());
            return;
        }
        if (yearFrom > yearTo) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("yearFrom cannot be greater than yearTo.").asRuntimeException());
            return;
        }

        if(evalCycleRepository.existsByName(evalName)){
            responseObserver.onError(Status.ALREADY_EXISTS
                    .withDescription("Evaluation cycle \"" + evalName + "\" already exists.").asRuntimeException());
            return;
        }

        if(evalCycleRepository.existsOverlapping(yearFrom, yearTo)){
            responseObserver.onError(Status.FAILED_PRECONDITION
                    .withDescription("The provided year range overlaps an existing evaluation cycle.").asRuntimeException());
            return;
        }
        EvalCycle cycle = new EvalCycle();

        cycle.setName(evalName);
        cycle.setYearFrom(yearFrom);
        cycle.setYearTo(yearTo);
        cycle.setActive(active);

        if (active) {
            evalCycleRepository.deactivateAll();
        }


        EvalCycle saved = evalCycleRepository.save(cycle);

        long mvId = 0;

        if (saved.getMeinVersion() != null && saved.getMeinVersion().getId() != null) {
            mvId = saved.getMeinVersion().getId();
        }
        CycleItem resp = CycleItem.newBuilder()
                .setId(saved.getId())
                .setName(saved.getName() == null ? "" : saved.getName())
                .setYearFrom(saved.getYearFrom())
                .setYearTo(saved.getYearTo())
                .setIsActive(saved.isActive())
                .setMeinVersionId(mvId)
                .build();

        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void adminCreatePublicationType(CreateTypeRequest request, StreamObserver<RefItem> responseObserver) {
        String publicationTypeName = request.getName();

        if(publicationTypeName.isEmpty()){
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("publicationType name is required.").asRuntimeException());
            return;
        }

        if(publicationTypeRepository.existsByName(publicationTypeName)){
            responseObserver.onError(Status.ALREADY_EXISTS
                    .withDescription("PublicationType \"" + publicationTypeName + "\" already exists.").asRuntimeException());
            return;
        }

        PublicationType publicationType = new PublicationType();
        publicationType.setName(publicationTypeName);

        PublicationType saved = publicationTypeRepository.save(publicationType);


        RefItem response = RefItem.newBuilder()
                .setId(saved.getId())
                .setName(saved.getName())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    @Override
    public void adminUpdateDiscipline(UpdateDisciplineRequest request, StreamObserver<RefItem> responseObserver) {

        Long disciplineId = request.getId();
        String disciplineName = request.getDisciplineName();

        Discipline discipline = disciplineRepository.findById(disciplineId).orElseThrow(() -> new StatusRuntimeException(
                Status.NOT_FOUND.withDescription("Discipline not found" + disciplineId)
        ));

        if(discipline.getName().equals(disciplineName)){
            responseObserver.onError(Status.ALREADY_EXISTS
                    .withDescription("Discipline with name " + disciplineName + "already exists").asRuntimeException());
            return;
        }

        discipline.setName(disciplineName);
        disciplineRepository.save(discipline);

        RefItem refItem = RefItem.newBuilder().setId(discipline.getId()).setName(discipline.getName()).build();
        responseObserver.onNext(refItem);
        responseObserver.onCompleted();
    }

    @Override
    @Transactional
    public void adminUpdateEvalCycle(UpdateCycleRequest request, StreamObserver<CycleItem> responseObserver) {
        Long evalId = request.getId();

        EvalCycle cycle = evalCycleRepository.findById(evalId).orElseThrow(() -> new StatusRuntimeException(
                Status.NOT_FOUND.withDescription("EvalCycle not found" + evalId)
        ));

        Set<String> paths = new HashSet<>(request.getUpdateMask().getPathsList());

        String evalName = cycle.getName();
        int yearFrom = cycle.getYearFrom();
        int yearTo = cycle.getYearTo();
        boolean isActive   = cycle.isActive();
        Long meinVersionId = (cycle.getMeinVersion() != null ? cycle.getMeinVersion().getId() : null);
        Long monoVersionId = (cycle.getMeinMonoVersion() != null ? cycle.getMeinVersion().getId() : null);

        if (paths.contains("name"))     evalName = request.getName();
        if (paths.contains("yearFrom"))  yearFrom = request.getYearFrom();
        if (paths.contains("yearTo"))    yearTo = request.getYearTo();
        if (paths.contains("isActive"))  isActive   = request.getIsActive();
        if (paths.contains("meinVersionId")) {
            long raw = request.getMeinVersionId();
            meinVersionId = (raw > 0 ? raw : null);
        }
        if (paths.contains("monoVersionId")) {
            long raw = request.getMonoVersionId();
            monoVersionId = (raw > 0 ? raw : null);
        }

        if (paths.contains("name")) {
            if(evalName == null || evalName.isEmpty()){
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("name must not be blank.").asRuntimeException());
                return;
            }
            if (evalCycleRepository.existsByName(evalName)) {
                responseObserver.onError(Status.ALREADY_EXISTS
                        .withDescription("Evaluation cycle \"" + evalName + "\" already exists.").asRuntimeException());
                return;
            }
        }

        if (paths.contains("yearFrom") || paths.contains("yearTo")) {

            if(evalCycleRepository.existsOverlappingExcludeId(evalId,yearFrom, yearTo)){
                responseObserver.onError(Status.FAILED_PRECONDITION
                        .withDescription("The provided year range overlaps an existing evaluation cycle.").asRuntimeException());
                return;
            }
            if (yearFrom <= 0 || yearTo <= 0) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("yearFrom and yearTo must be positive.").asRuntimeException());
                return;
            }
            if (yearFrom > yearTo) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("yearFrom cannot be greater than yearTo.").asRuntimeException());
                return;
            }

        }

        if(paths.contains("meinVersionId" )&& meinVersionId != null){
            if (!meinVersionRepository.existsById(meinVersionId)) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("meinVersionId not found: " + meinVersionId).asRuntimeException());
                return;
            }
        }
        if(paths.contains("monoVersionId" )&& monoVersionId != null){
            if (!meinMonoVersionRepository.existsById(monoVersionId)) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("monoVersionId not found: " + meinVersionId).asRuntimeException());
                return;
            }
        }

        if(paths.contains("name"))      cycle.setName(evalName);
        if (paths.contains("yearFrom")) cycle.setYearFrom(yearFrom);
        if (paths.contains("yearTo"))   cycle.setYearTo(yearTo);
        if (paths.contains("isActive")) cycle.setActive(isActive);
        if (paths.contains("meinVersionId")) {
            if (meinVersionId == null) {
                cycle.setMeinVersion(null);
            } else {
                MeinVersion mv = meinVersionRepository.findById(meinVersionId).orElseThrow(() -> new RuntimeException("Mein Version not found"));
                cycle.setMeinVersion(mv);
            }
        }
        if (paths.contains("monoVersionId")) {
            if (monoVersionId == null) {
                cycle.setMeinMonoVersion(null);
            } else {
                MeinMonoVersion mmv = meinMonoVersionRepository.findById(monoVersionId).orElseThrow(() -> new RuntimeException("Mein Version not found"));
                cycle.setMeinMonoVersion(mmv);
            }
        }

        if (paths.contains("isActive") && isActive) {
            evalCycleRepository.deactivateAllExcept(evalId);
        }

        evalCycleRepository.save(cycle);

        CycleItem response = CycleItem.newBuilder()
                .setId(cycle.getId())
                .setName(cycle.getName())
                .setYearFrom(cycle.getYearFrom())
                .setYearTo(cycle.getYearTo())
                .setIsActive(cycle.isActive())
                .setMeinVersionId(cycle.getMeinVersion().getId() == null ? 0 : cycle.getMeinVersion().getId())
                .setMonoVersionId(cycle.getMeinMonoVersion().getId() == null ? 0 : cycle.getMeinMonoVersion().getId())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void adminUpdatePublicationType(UpdateTypeRequest request, StreamObserver<RefItem> responseObserver) {
        Long publicationTypeId = request.getId();
        String publicationTypeName = request.getName();

        PublicationType publicationType = publicationTypeRepository.findById(publicationTypeId).orElseThrow(
                () -> new StatusRuntimeException(Status.NOT_FOUND.withDescription("Publication type not found" + publicationTypeId)
                ));

        if(publicationType.getName().equals(publicationTypeName)){
            responseObserver.onError(Status.ALREADY_EXISTS.withDescription("Publication Type with this name already exists").asRuntimeException());
            return;
        }

        if(publicationTypeName == null || publicationTypeName.isEmpty()){
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Publication Type name cabbot be empty").asRuntimeException());
            return;
        }

        publicationType.setName(publicationTypeName);
        publicationTypeRepository.save(publicationType);

        RefItem response = RefItem.newBuilder()
                .setId(publicationType.getId())
                .setName(publicationType.getName())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    @Override
    public void adminDeleteDiscipline(DeleteDisciplineRequest request, StreamObserver<ApiResponse> responseObserver) {
        Long disciplineId = request.getId();

        Discipline discipline = disciplineRepository.findById(disciplineId).orElseThrow(
                () -> new StatusRuntimeException(Status.NOT_FOUND.withDescription("Discipline not found" + disciplineId)));

        disciplineRepository.delete(discipline);

        ApiResponse response = ApiResponse.newBuilder()
                .setMessage("Discipline is deleted")
                .setCode(200)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void adminDeleteEvalCycle(DeleteCycleRequest request, StreamObserver<ApiResponse> responseObserver) {
        Long evalCycleId = request.getId();

        EvalCycle evalCycle = evalCycleRepository.findById(evalCycleId).orElseThrow(
                () -> new StatusRuntimeException(Status.NOT_FOUND.withDescription("Evaluation cycle not found" + evalCycleId)));

        evalCycleRepository.delete(evalCycle);

        ApiResponse response = ApiResponse.newBuilder()
                .setMessage("Evaluation Cycle is deleted")
                .setCode(200)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void adminDeletePublicationType(DeleteTypeRequest request, StreamObserver<ApiResponse> responseObserver) {
        Long publicationTypeId = request.getId();

        PublicationType publicationType = publicationTypeRepository.findById(publicationTypeId).orElseThrow(
                () -> new StatusRuntimeException(Status.NOT_FOUND.withDescription("Publication type not found" + publicationTypeId)));

        publicationTypeRepository.delete(publicationType);

        ApiResponse response = ApiResponse.newBuilder()
                .setMessage("Publication Type is deleted")
                .setCode(200)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     *  Private function for ListPublication
     */
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
