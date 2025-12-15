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
            long meinId = evalCycle.getMeinVersion() != null ? evalCycle.getMeinVersion().getId() : 0;
            long monoId = evalCycle.getMeinMonoVersion() != null ? evalCycle.getMeinMonoVersion().getId() : 0;
            response.addItems(CycleItem.newBuilder()
                    .setId(evalCycle.getId())
                    .setName(evalCycle.getName())
                    .setYearFrom(evalCycle.getYearFrom())
                    .setYearTo(evalCycle.getYearTo())
                    .setIsActive(evalCycle.isActive())
                    .setMeinVersionId(meinId)
                    .setMonoVersionId(monoId)
                    .build());
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }


}
