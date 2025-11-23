package org.example.article.helpers;

import org.example.article.ETL.IssnUtil;
import org.example.article.entities.CommuteResult;
import org.example.article.entities.MEiN.article.MeinJournal;
import org.example.article.repositories.*;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CommutePoints {

    private final EvalCycleRepository evalCycleRepository;
    private final PublicationTypeRepository publicationTypeRepository;
    private final DisciplineRepository disciplineRepository;
    private final MeinJournalRepository meinJournalRepository;
    private final MeinJournalCodeRepository meinJournalCodeRepository;

    public CommutePoints(EvalCycleRepository evalCycleRepository, PublicationTypeRepository publicationTypeRepository, DisciplineRepository disciplineRepository, MeinJournalRepository meinJournalRepository, MeinJournalCodeRepository meinJournalCodeRepository) {
        this.evalCycleRepository = evalCycleRepository;
        this.publicationTypeRepository = publicationTypeRepository;
        this.disciplineRepository = disciplineRepository;
        this.meinJournalRepository = meinJournalRepository;
        this.meinJournalCodeRepository = meinJournalCodeRepository;
    }

    public CommuteResult commute(String joutnalTitle, Long typeId, Long disciplineId,
                                 String issnRaw, String eissnRaw, int year){

        final int OFF_LIST_POINTS = 5;

        var cycle = evalCycleRepository.findByYear(year)
                .orElseThrow(() -> new IllegalArgumentException("No eval cycle for year " + year));
        if (cycle.getMeinVersion() == null || cycle.getMeinVersion().getId() == null) {
            throw new IllegalArgumentException("Eval cycle has no MEiN version");
        }
        Long versionId = cycle.getMeinVersion().getId();

        var type = publicationTypeRepository.findById(typeId).orElseThrow();
        var discipline = disciplineRepository.findById(disciplineId).orElseThrow();


        boolean isArticle = "ARTICLE".equalsIgnoreCase(type.getName());
        if(!isArticle){
            return new CommuteResult(cycle, null, null, 0, false);
        }

        String issn = IssnUtil.normalize(issnRaw);
        String eissn = IssnUtil.normalize(eissnRaw);

        if (issn == null && eissn == null) {
            return new CommuteResult(cycle, null, null, 0, false);
        }

        boolean ok = meinJournalRepository.existsByIssnAndEissn(versionId,issn, eissn);

        if (!ok) {
            return new CommuteResult(cycle, null, null, 0, true);
        }


        Optional<MeinJournal> match = meinJournalRepository.findByVersionAndIssnOrEissnAndTitle(versionId,issn, eissn,joutnalTitle);
        if (match.isEmpty()) {
            return new CommuteResult(cycle, null, null,  OFF_LIST_POINTS, true);
        }

        var journal = match.get();

        boolean isOnJournal = meinJournalCodeRepository.existsMatchInVersion(journal.getId(),disciplineId,versionId);
        if(!isOnJournal){
//            throw new IllegalArgumentException(
//                    "Wybrana dyscyplina („" + discipline.getName() + "”) nie jest powiązana z tym czasopismem w aktywnej liście MEiN.");
            return new CommuteResult(cycle, null, null, OFF_LIST_POINTS, true);
        }
        return new CommuteResult(cycle,journal.getVersion(), journal, journal.getPoints(), false);

    }
}
