package org.example.article.service.Async;

import org.example.article.entities.*;
import org.example.article.entities.MEiN.monographs.MonographChapter;
import org.example.article.entities.MEiN.monographs.Monographic;
import org.example.article.helpers.CommutePoints;
import org.example.article.helpers.Recalculation;
import org.example.article.repositories.EvalCycleRepository;
import org.example.article.repositories.MonographChapterRepository;
import org.example.article.repositories.MonographicRepository;
import org.example.article.repositories.PublicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.function.BiConsumer;

@Service
public class AsyncArticleService {

    private final EvalCycleRepository evalCycleRepository;
    private final PublicationRepository publicationRepository;
    private final CommutePoints commutePoints;
    private final MonographicRepository monographicRepository;
    private final MonographChapterRepository monographChapterRepository;

    public AsyncArticleService(EvalCycleRepository evalCycleRepository,
                               PublicationRepository publicationRepository,
                               CommutePoints commutePoints, MonographicRepository monographicRepository, MonographChapterRepository monographChapterRepository) {
        this.evalCycleRepository = evalCycleRepository;
        this.publicationRepository = publicationRepository;
        this.commutePoints = commutePoints;
        this.monographicRepository = monographicRepository;
        this.monographChapterRepository = monographChapterRepository;
    }

    public record RecalcResult(int updated, int unmatched) {
    }

    /**
     * Przelicza punkty dla cyklu.
     * progressCallback może być null.
     * progress: 0–100, phase: opis bieżącego etapu.
     */
    @Transactional
    public RecalcResult recalcArticleCycleScores(
            Long cycleId,
            BiConsumer<Integer, String> progressCallback
    ) {
        EvalCycle evalCycle = evalCycleRepository.findById(cycleId)
                .orElseThrow(() -> new RuntimeException("Not found the cycle"));

        List<Publication> publications = publicationRepository.findAllByCycle(evalCycle);

        int total = publications.size();
        int processed = 0;
        int updated = 0;
        int unmatched = 0;

        if (progressCallback != null) {
            progressCallback.accept(0, "Starting article recalculation");
        }
        for (Publication pub : publications) {

            CommuteResultArticle result = commutePoints.commuteArticle(
                    pub.getJournalTitle(),
                    pub.getType().getId(),
                    pub.getDiscipline().getId(),
                    pub.getIssn(),
                    pub.getEissn(),
                    pub.getPublicationYear()
            );

            if (result == null || result.cycle() == null) {
                unmatched++;
            } else {
                // ZAWSZE aktualizuj punkty + cykl
                pub.setCycle(result.cycle());
                pub.setMeinPoints(result.points());
                pub.setUpdatedAt(Instant.now());

                // ID tylko jeśli dopasowane do MEiN (nie offList)
                if (!result.offList()
                        && result.meinJournal() != null
                        && result.meinVersion() != null) {
                    pub.setMeinVersionId(result.meinVersion().getId());
                    pub.setMeinJournalId(result.meinJournal().getId());
                } else {
                    pub.setMeinVersionId(null);
                    pub.setMeinJournalId(null);
                }

                updated++;
            }

            processed++;

            if (progressCallback != null && (processed % 10 == 0 || processed == total)) {
                int percent = total == 0 ? 100 : (processed * 100 / total);
                progressCallback.accept(percent,
                        "Recalculating articles (" + processed + "/" + total + ")");
            }
        }
        if (!publications.isEmpty()) {
            publicationRepository.saveAll(publications);
        }

        if (progressCallback != null) {
            progressCallback.accept(100, "Article recalculation finished");
        }

        return new RecalcResult(updated, unmatched);
    }

//    public Recalculation recalcMonoCycleScoresInternal(EvalCycle evalCycle) {
//        List<Monographic> monographics = monographicRepository.findAllByCycle(evalCycle);
//        List<MonographChapter> chapters = monographChapterRepository.findAllByCycle(evalCycle);
//
//        int unmatchedMonograph = 0;
//        int unmatchedChapter = 0;
//        int updatedMonograph = 0;
//        int updatedChapter = 0;
//
//        for(MonographChapter mc : chapters){
//            CommuteResultMonoChapter resultMonoChapter = commutePoints.commuteChapter(
//                    mc.getMonographChapterPublisher(),
//                    mc.getType().getId(),
//                    mc.getPublicationYear()
//            );
//
//            if (resultMonoChapter == null
//                    || resultMonoChapter.meinMonoVersion() == null
//                    || resultMonoChapter.meinMonoPublisher() == null) {
//                unmatchedChapter++;
//                continue;
//            }
//
//            mc.setCycle(resultMonoChapter.cycle());
//            mc.setMeinPoints(resultMonoChapter.points());
//            mc.setMeinMonoId(resultMonoChapter.meinMonoVersion().getId());
//            mc.setMeinMonoPublisherId(resultMonoChapter.meinMonoPublisher().getId());
//            mc.setUpdatedAt(Instant.now());
//
//            updatedChapter++;
//
//            if(!chapters.isEmpty()){
//                monographChapterRepository.saveAll(chapters);
//            }
//        }
//
//        for(Monographic m : monographics){
//            CommuteResultMono resultMono = commutePoints.commuteMono(
//                    m.getMonograficTitle(),
//                    m.getType().getId(),
//                    m.getPublicationYear()
//            );
//
//            if (resultMono == null
//                    || resultMono.meinMonoVersion() == null
//                    || resultMono.meinMonoPublisher() == null) {
//                unmatchedMonograph++;
//                continue;
//            }
//
//            m.setCycle(resultMono.cycle());
//            m.setMeinPoints(resultMono.points());
//            m.setMeinMonoId(resultMono.meinMonoVersion().getId());
//            m.setMeinMonoPublisherId(resultMono.meinMonoPublisher().getId());
//            m.setUpdatedAt(Instant.now());
//
//            updatedMonograph++;
//
//            if(!monographics.isEmpty()){
//                monographicRepository.saveAll(monographics);
//            }
//        }
//
//        Recalculation recalculation = new Recalculation();
//        recalculation.setUnmatchdMono(unmatchedMonograph);
//        recalculation.setUnmatchdChapter(unmatchedChapter);
//        recalculation.setUpdatedMono(updatedMonograph);
//        recalculation.setUpdatedChapter(updatedChapter);
//        return recalculation;
//    }
public Recalculation recalcMonoCycleScoresInternal(EvalCycle evalCycle) {
    List<Monographic> monographics = monographicRepository.findAllByCycle(evalCycle);
    List<MonographChapter> chapters = monographChapterRepository.findAllByCycle(evalCycle);

    int unmatchedMonograph = 0;
    int unmatchedChapter = 0;
    int updatedMonograph = 0;
    int updatedChapter = 0;

    // --- CHAPTERS ---
    for (MonographChapter mc : chapters) {
        CommuteResultMonoChapter result = commutePoints.commuteChapter(
                mc.getMonographChapterPublisher(),
                mc.getType().getId(),
                mc.getPublicationYear()
        );

        // jak w publications: unmatched tylko gdy nie ma wyniku lub nie ma cycle
        if (result == null || result.cycle() == null) {
            unmatchedChapter++;
            continue;
        }

        // ZAWSZE aktualizuj punkty + cykl
        mc.setCycle(result.cycle());
        mc.setMeinPoints(result.points());
        mc.setUpdatedAt(Instant.now());

        // ID tylko jeśli dopasowane do MEiN (nie offList)
        if (!result.offList()
                && result.meinMonoVersion() != null
                && result.meinMonoPublisher() != null) {
            mc.setMeinMonoId(result.meinMonoVersion().getId());
            mc.setMeinMonoPublisherId(result.meinMonoPublisher().getId());
        } else {
            mc.setMeinMonoId(null);
            mc.setMeinMonoPublisherId(null);
        }

        updatedChapter++;
    }

    if (!chapters.isEmpty()) {
        monographChapterRepository.saveAll(chapters);
    }

    // --- MONOGRAPHS ---
    for (Monographic m : monographics) {
        CommuteResultMono result = commutePoints.commuteMono(
                m.getMonograficTitle(),
                m.getType().getId(),
                m.getPublicationYear()
        );

        if (result == null || result.cycle() == null) {
            unmatchedMonograph++;
            continue;
        }

        // ZAWSZE aktualizuj punkty + cykl
        m.setCycle(result.cycle());
        m.setMeinPoints(result.points());
        m.setUpdatedAt(Instant.now());

        // ID tylko jeśli dopasowane do MEiN (nie offList)
        if (!result.offList()
                && result.meinMonoVersion() != null
                && result.meinMonoPublisher() != null) {
            m.setMeinMonoId(result.meinMonoVersion().getId());
            m.setMeinMonoPublisherId(result.meinMonoPublisher().getId());
        } else {
            m.setMeinMonoId(null);
            m.setMeinMonoPublisherId(null);
        }

        updatedMonograph++;
    }

    if (!monographics.isEmpty()) {
        monographicRepository.saveAll(monographics);
    }

    Recalculation recalculation = new Recalculation();
    recalculation.setUnmatchdMono(unmatchedMonograph);
    recalculation.setUnmatchdChapter(unmatchedChapter);
    recalculation.setUpdatedMono(updatedMonograph);
    recalculation.setUpdatedChapter(updatedChapter);
    return recalculation;
}

}
