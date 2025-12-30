package org.example.article.helpers;

import org.example.article.ETL.IssnUtil;
import org.example.article.entities.CommuteResultArticle;
import org.example.article.entities.CommuteResultMono;
import org.example.article.entities.CommuteResultMonoChapter;
import org.example.article.entities.MEiN.article.MeinJournal;
import org.example.article.entities.MEiN.monographs.MeinMonoPublisher;
import org.example.article.entities.PublicationType;
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
    private final MeinMonoPublisherRepository meinMonoPublisherRepository;

    public CommutePoints(EvalCycleRepository evalCycleRepository, PublicationTypeRepository publicationTypeRepository, DisciplineRepository disciplineRepository, MeinJournalRepository meinJournalRepository, MeinJournalCodeRepository meinJournalCodeRepository, MeinMonoPublisherRepository meinMonoPublisherRepository) {
        this.evalCycleRepository = evalCycleRepository;
        this.publicationTypeRepository = publicationTypeRepository;
        this.disciplineRepository = disciplineRepository;
        this.meinJournalRepository = meinJournalRepository;
        this.meinJournalCodeRepository = meinJournalCodeRepository;
        this.meinMonoPublisherRepository = meinMonoPublisherRepository;
    }

    final int OFF_LIST__ARTICLE_POINTS = 5;
    final int OFF_LIST_MONO_POINTS = 20;
    final int OFF_LIST_MONO_CHAPTER_POINTS = 5;
    final double CHAPTER_MULTIPLIER = 0.25;

    public CommuteResultArticle commuteArticle(String joutnalTitle, Long typeId, Long disciplineId,
                                               String issnRaw, String eissnRaw, int year) {

        var cycle = evalCycleRepository.findByYear(year)
                .orElseThrow(() -> new IllegalArgumentException("No eval cycle for year " + year));
        if (cycle.getMeinVersion() == null || cycle.getMeinVersion().getId() == null) {
            return new CommuteResultArticle(cycle, null, null,0, true);
        }
        Long versionId = cycle.getMeinVersion().getId();

        var type = publicationTypeRepository.findById(typeId).orElseThrow();
        var discipline = disciplineRepository.findById(disciplineId).orElseThrow();


        boolean isArticle = "ARTICLE".equalsIgnoreCase(type.getName());
        if (!isArticle) {
            return new CommuteResultArticle(cycle, null, null, 0, false);
        }

        String issn = IssnUtil.normalize(issnRaw);
        String eissn = IssnUtil.normalize(eissnRaw);

        if (issn == null && eissn == null) {
            return new CommuteResultArticle(cycle, null, null, 0, false);
        }

        boolean ok = meinJournalRepository.existsByIssnAndEissn(versionId, issn, eissn);

        if (!ok) {
            return new CommuteResultArticle(cycle, null, null,  OFF_LIST__ARTICLE_POINTS, true);
        }


        Optional<MeinJournal> match = meinJournalRepository.findByVersionAndIssnOrEissnAndTitle(versionId, issn, eissn, joutnalTitle);
        if (match.isEmpty()) {
            return new CommuteResultArticle(cycle, null, null, OFF_LIST__ARTICLE_POINTS, true);
        }

        var journal = match.get();

        boolean isOnJournal = meinJournalCodeRepository.existsMatchInVersion(journal.getId(), disciplineId, versionId);
        if (!isOnJournal) {
            return new CommuteResultArticle(cycle, null, null, OFF_LIST__ARTICLE_POINTS, true);
        }
        return new CommuteResultArticle(cycle, journal.getVersion(), journal, journal.getPoints(), false);

    }

    public CommuteResultMono commuteMono(String monographyPublisher, Long typeId, int year) {
        var cycle = evalCycleRepository.findByYear(year)
                .orElseThrow(() -> new IllegalArgumentException("No eval cycle for year " + year));
        if (cycle.getMeinMonoVersion() == null || cycle.getMeinMonoVersion().getId() == null) {
            return new CommuteResultMono(cycle, null, null,  0, true);
        }

        Long versionId = cycle.getMeinMonoVersion().getId();

        PublicationType type = publicationTypeRepository.findById(typeId).orElseThrow(() -> new IllegalArgumentException("No publication type for type " + typeId));

        boolean monography = "MONOGRAPH".equalsIgnoreCase(type.getName());

        if (!monography) {
            return new CommuteResultMono(cycle, null, null, 0, false);
        }

        Optional<MeinMonoPublisher> meinMonoPublisher = meinMonoPublisherRepository.findByVersionIdAndTitle(versionId, monographyPublisher);
        if (meinMonoPublisher.isEmpty()) {
            return new CommuteResultMono(cycle, null, null, OFF_LIST_MONO_POINTS, true);
        }

        var publisher = meinMonoPublisher.get();

        boolean isOnPublisher = meinMonoPublisherRepository.existsMatchInVersion(versionId, publisher.getId());
        if (!isOnPublisher) {
            return new CommuteResultMono(cycle, null, null, OFF_LIST_MONO_POINTS, true);
        }
        return new CommuteResultMono(cycle, publisher.getVersion(), publisher, publisher.getPoints(), false);
    }

    public CommuteResultMonoChapter commuteChapter(String monographyPublisher,Long typeId, int year){
        var cycle = evalCycleRepository.findByYear(year)
                .orElseThrow(() -> new IllegalArgumentException("No eval cycle for year " + year));
        if(cycle.getMeinMonoVersion() == null || cycle.getMeinMonoVersion().getId() == null) {
            return new CommuteResultMonoChapter(cycle, null, null, 0, false);
        }
        Long versionId = cycle.getMeinMonoVersion().getId();

        PublicationType type = publicationTypeRepository.findById(typeId).orElseThrow(() -> new IllegalArgumentException("No publication type for type " + typeId));

        boolean monographChapter = "CHAPTER".equalsIgnoreCase(type.getName());

        if(!monographChapter){
            return new CommuteResultMonoChapter(cycle, null, null, 0, false);
        }

        Optional<MeinMonoPublisher> meinMonoPublisher = meinMonoPublisherRepository.findByVersionIdAndTitle(versionId,monographyPublisher);
        if(meinMonoPublisher.isEmpty()){
            return new CommuteResultMonoChapter(cycle, null, null, OFF_LIST_MONO_CHAPTER_POINTS, true);
        }

        var publisher = meinMonoPublisher.get();

        boolean isOnPublisher = meinMonoPublisherRepository.existsMatchInVersion(versionId, publisher.getId());
        if(!isOnPublisher){
            return new CommuteResultMonoChapter(cycle, null, null, OFF_LIST_MONO_CHAPTER_POINTS, true);
        }

        return new CommuteResultMonoChapter(cycle,publisher.getVersion(), publisher, publisher.getPoints() * CHAPTER_MULTIPLIER , false );


    }
}