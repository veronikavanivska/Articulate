package org.example.article.service.Async;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityManager;
import org.example.article.entities.AsyncJob;
import org.example.article.entities.EvalCycle;
import org.example.article.entities.MEiN.monographs.MonographChapter;
import org.example.article.entities.MEiN.monographs.Monographic;
import org.example.article.entities.Publication;
import org.example.article.helpers.CommutePoints;
import org.example.article.helpers.Recalculation;
import org.example.article.repositories.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;

@Service
public class AsyncMeinService {

    private final AsyncJobRepository asyncJobRepository;
    private final AsyncArticleService articleService;
    private final EvalCycleRepository evalCycleRepository;
    private final PublicationRepository publicationRepository;
    private final CommutePoints commutePoints;
    private final MeinJournalCodeRepository meinJournalCodeRepository;
    private final MeinJournalRepository meinJournalRepository;
    private final MeinVersionRepository meinVersionRepository;
    private final EntityManager entityManager;
    private final TransactionTemplate tx;
    private final ObjectMapper objectMapper;
    private final MeinMonoVersionRepository meinMonoVersionRepository;
    private final MonographChapterRepository monographChapterRepository;
    private final MonographicRepository monographicRepository;

    public AsyncMeinService(AsyncJobRepository asyncJobRepository,
                            AsyncArticleService articleService,
                            EvalCycleRepository evalCycleRepository,
                            PublicationRepository publicationRepository,
                            CommutePoints commutePoints,
                            MeinJournalCodeRepository meinJournalCodeRepository,
                            MeinJournalRepository meinJournalRepository,
                            MeinVersionRepository meinVersionRepository,
                            EntityManager entityManager,
                            TransactionTemplate tx,
                            ObjectMapper objectMapper, MeinMonoVersionRepository meinMonoVersionRepository, MonographChapterRepository monographChapterRepository, MonographicRepository monographicRepository) {
        this.asyncJobRepository = asyncJobRepository;
        this.articleService = articleService;
        this.evalCycleRepository = evalCycleRepository;
        this.publicationRepository = publicationRepository;
        this.commutePoints = commutePoints;
        this.meinJournalCodeRepository = meinJournalCodeRepository;
        this.meinJournalRepository = meinJournalRepository;
        this.meinVersionRepository = meinVersionRepository;
        this.entityManager = entityManager;
        this.tx = tx;
        this.objectMapper = objectMapper;
        this.meinMonoVersionRepository = meinMonoVersionRepository;
        this.monographChapterRepository = monographChapterRepository;
        this.monographicRepository = monographicRepository;
    }

    // 1) Job: RECALC_CYCLE_SCORES (ARTICLES)
    @Async("meinTaskExecutor")
    public void executeRecalcCycleScoresJob(Long jobId) {
        AsyncJob job = asyncJobRepository.findById(jobId).orElseThrow();

        try {
            job.setStatus(AsyncJob.Status.RUNNING);
            job.setPhase("Preparing");
            job.setProgressPercent(0);
            job.setMessage("Starting article recalculation");
            asyncJobRepository.save(job);

            JsonNode payload = objectMapper.readTree(job.getRequestPayload());
            long cycleId = payload.get("cycleId").asLong();

            AsyncArticleService.RecalcResult result = articleService.recalcArticleCycleScores(
                    cycleId,
                    (progress, phase) -> {
                        job.setProgressPercent(progress);
                        job.setPhase(phase);
                        asyncJobRepository.save(job);
                    }
            );

            ObjectNode resultJson = objectMapper.createObjectNode();
            resultJson.put("updated", result.updated());
            resultJson.put("unmatched", result.unmatched());

            job.setStatus(AsyncJob.Status.DONE);
            job.setPhase("Finished");
            job.setMessage("Article recalculation completed");
            job.setResultPayload(objectMapper.writeValueAsString(resultJson));
            job.setProgressPercent(100);
            job.setFinishedAt(Instant.now());
            asyncJobRepository.save(job);

        } catch (Exception e) {
            job.setStatus(AsyncJob.Status.FAILED);
            job.setPhase("Failed");
            job.setMessage("Article recalculation failed");
            job.setErrorMessage(e.getMessage());
            job.setFinishedAt(Instant.now());
            asyncJobRepository.save(job);
        }
    }

    @Async("meinTaskExecutor")
    public void executeDeleteMeinVersionJob(Long jobId) {
        AsyncJob job = asyncJobRepository.findById(jobId).orElseThrow();

        try {
            job.setStatus(AsyncJob.Status.RUNNING);
            job.setPhase("Preparing delete");
            job.setProgressPercent(0);
            job.setMessage("Starting MEiN version deletion");
            asyncJobRepository.save(job);

            JsonNode payload = objectMapper.readTree(job.getRequestPayload());
            long versionId = payload.get("versionId").asLong();

            tx.executeWithoutResult(status -> {
                entityManager.createNativeQuery("SET LOCAL synchronous_commit = off")
                        .executeUpdate();


                var cycles = evalCycleRepository.findAllByMeinVersionId(versionId);
                for (EvalCycle cycle : cycles) {
                    cycle.setMeinVersion(null);
                }
                if (!cycles.isEmpty()) {
                    evalCycleRepository.saveAll(cycles);
                }

                var publications = publicationRepository.findAllByMeinVersionId(versionId);
                for (Publication pub : publications) {
                    pub.setMeinPoints(0);
                    pub.setMeinVersionId(null);
                    pub.setMeinJournalId(null);
                    pub.setUpdatedAt(Instant.now());
                }
                if (!publications.isEmpty()) {
                    publicationRepository.saveAll(publications);
                }
            });

            job.setPhase("Removing MEiN journals");
            job.setProgressPercent(50);
            job.setMessage("Removing MEiN journals and version");
            asyncJobRepository.save(job);

            tx.executeWithoutResult(status -> {
                entityManager.createNativeQuery("SET LOCAL synchronous_commit = off")
                        .executeUpdate();

                meinJournalCodeRepository.deleteCodesByVersion(versionId);
                meinJournalRepository.deleteJournalsByVersion(versionId);
                meinVersionRepository.deleteById(versionId);
                meinVersionRepository.flush();
            });

            ObjectNode resultJson = objectMapper.createObjectNode();
            resultJson.put("deletedVersionId", versionId);

            job.setStatus(AsyncJob.Status.DONE);
            job.setPhase("Finished");
            job.setMessage("MEiN version deletion completed");
            job.setResultPayload(objectMapper.writeValueAsString(resultJson));
            job.setProgressPercent(100);
            job.setFinishedAt(Instant.now());
            asyncJobRepository.save(job);

        } catch (Exception e) {
            job.setStatus(AsyncJob.Status.FAILED);
            job.setPhase("Failed");
            job.setMessage("MEiN version deletion failed");
            job.setErrorMessage(e.getMessage());
            job.setFinishedAt(Instant.now());
            asyncJobRepository.save(job);
        }
    }

    @Async("meinTaskExecutor")
    public void executeDeleteMeinMonoVersionJob(Long jobId) {
        AsyncJob job = asyncJobRepository.findById(jobId).orElseThrow();

        try {
            job.setStatus(AsyncJob.Status.RUNNING);
            job.setPhase("Preparing delete");
            job.setProgressPercent(0);
            job.setMessage("Starting MeinMonoVersion deletion");
            asyncJobRepository.save(job);

            JsonNode payload = objectMapper.readTree(job.getRequestPayload());
            long versionId = payload.get("versionId").asLong();

            tx.executeWithoutResult(status -> {
                entityManager.createNativeQuery("SET LOCAL synchronous_commit = off")
                        .executeUpdate();

                List<EvalCycle> cycles = evalCycleRepository.findAllByMeinMonoVersion(versionId);
                for (EvalCycle cycle : cycles) {
                    cycle.setMeinMonoVersion(null);
                }
                if (!cycles.isEmpty()) {
                    evalCycleRepository.saveAll(cycles);
                }

                List<Monographic> monographics = monographicRepository.findAllByMeinMonoId(versionId);
                for (Monographic m : monographics) {
                    m.setMeinPoints(0);
                    m.setMeinMonoId(null);
                    m.setMeinMonoPublisherId(null);
                    m.setUpdatedAt(Instant.now());
                }
                if (!monographics.isEmpty()) {
                    monographicRepository.saveAll(monographics);
                }

                List<MonographChapter> chapters = monographChapterRepository.findAllByMeinMonoId(versionId);
                for (MonographChapter mc : chapters) {
                    mc.setMeinPoints(0.0);
                    mc.setMeinMonoId(null);
                    mc.setMeinMonoPublisherId(null);
                    mc.setUpdatedAt(Instant.now());
                }
                if (!chapters.isEmpty()) {
                    monographChapterRepository.saveAll(chapters);
                }
            });

            job.setPhase("Removing MeinMonoVersion");
            job.setProgressPercent(60);
            job.setMessage("Marking MeinMonoVersion as deleted");
            asyncJobRepository.save(job);


            tx.executeWithoutResult(status -> {
                entityManager.createNativeQuery("SET LOCAL synchronous_commit = off")
                        .executeUpdate();

                 meinMonoVersionRepository.deleteById(versionId);
            });

            ObjectNode resultJson = objectMapper.createObjectNode();
            resultJson.put("deletedMonoVersionId", versionId);

            job.setStatus(AsyncJob.Status.DONE);
            job.setPhase("Finished");
            job.setMessage("MeinMonoVersion deletion completed");
            job.setResultPayload(objectMapper.writeValueAsString(resultJson));
            job.setProgressPercent(100);
            job.setFinishedAt(Instant.now());
            asyncJobRepository.save(job);

        } catch (Exception e) {
            job.setStatus(AsyncJob.Status.FAILED);
            job.setPhase("Failed");
            job.setMessage("MeinMonoVersion deletion failed");
            job.setErrorMessage(e.getMessage());
            job.setFinishedAt(Instant.now());
            asyncJobRepository.save(job);
        }
    }

    @Async("meinTaskExecutor")
    public void executeRecalcMonoCycleScoresJob(Long jobId) {
        AsyncJob job = asyncJobRepository.findById(jobId).orElseThrow();

        try {
            job.setStatus(AsyncJob.Status.RUNNING);
            job.setPhase("Preparing mono recalc");
            job.setProgressPercent(0);
            job.setMessage("Starting monograph cycle recalculation");
            asyncJobRepository.save(job);

            JsonNode payload = objectMapper.readTree(job.getRequestPayload());
            long cycleId = payload.get("cycleId").asLong();

            EvalCycle evalCycle = evalCycleRepository.findById(cycleId)
                    .orElseThrow(() -> new RuntimeException("EvalCycle not found: " + cycleId));

            Recalculation recalculation = articleService.recalcMonoCycleScoresInternal(evalCycle);

            ObjectNode resultJson = objectMapper.createObjectNode();
            resultJson.put("updatedMonographs", recalculation.getUpdatedMono());
            resultJson.put("unmatchedMonographs", recalculation.getUnmatchdMono());
            resultJson.put("updatedChapters", recalculation.getUpdatedChapter());
            resultJson.put("unmatchedChapters", recalculation.getUnmatchdChapter());

            job.setStatus(AsyncJob.Status.DONE);
            job.setPhase("Finished");
            job.setMessage("Monograph cycle recalculation completed");
            job.setResultPayload(objectMapper.writeValueAsString(resultJson));
            job.setProgressPercent(100);
            job.setFinishedAt(Instant.now());
            asyncJobRepository.save(job);

        } catch (Exception e) {
            job.setStatus(AsyncJob.Status.FAILED);
            job.setPhase("Failed");
            job.setMessage("Monograph cycle recalculation failed");
            job.setErrorMessage(e.getMessage());
            job.setFinishedAt(Instant.now());
            asyncJobRepository.save(job);
        }
    }
}
