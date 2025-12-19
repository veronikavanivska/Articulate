package org.example.article.helpers;

import com.example.generated.*;
import org.example.article.entities.MEiN.monographs.MonographAuthor;
import org.example.article.entities.MEiN.monographs.MonographChapter;
import org.example.article.entities.MEiN.monographs.MonographChapterAuthor;
import org.example.article.entities.MEiN.monographs.Monographic;
import org.example.article.entities.Publication;
import org.example.article.entities.PublicationCoauthor;


public final class Mapper {
    private Mapper() {}

    public static PublicationView entityToProtoArticle(Publication publication) {

        RefItem type = RefItem.newBuilder()
                .setId(publication.getType().getId())
                .setName(publication.getType().getName())
                .build();

        RefItem discipline = RefItem.newBuilder()
                .setId(publication.getDiscipline().getId())
                .setName(publication.getDiscipline().getName())
                .build();

        CycleItem.Builder cycle = CycleItem.newBuilder()
                .setName(publication.getCycle().getName())
                .setIsActive(publication.getCycle().isActive())
                .setId(publication.getCycle().getId())
                .setYearFrom(publication.getCycle().getYearFrom())
                .setYearTo(publication.getCycle().getYearTo())
                .setActiveYear(publication.getCycle().getActiveYear());

        if(publication.getCycle().getMeinVersion() != null) {
             cycle.setMeinVersionId(publication.getCycle().getMeinVersion().getId());
        }else {
            cycle.setMeinVersionId(0);
        }
        if(publication.getCycle().getMeinMonoVersion() != null) {
            cycle.setMonoVersionId(publication.getCycle().getMeinMonoVersion().getId());
        }else {
            cycle.setMonoVersionId(0);
        }


        cycle.build();

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
                .setCycle(cycle)
                .setType(type)
                .setDiscipline(discipline);


        if (publication.getMeinVersionId() != null) {
            b.setMeinVersionId(publication.getMeinVersionId());
        }else{
            b.setMeinVersionId(0);
        }
        if (publication.getMeinJournalId() != null) {
            b.setMeinJournalId(publication.getMeinJournalId());
        }else{
            b.setMeinJournalId(0);
        }

        publication.getCoauthors().stream()
                .sorted(java.util.Comparator.comparingInt(PublicationCoauthor::getPosition))
                .forEach(c -> {
                    Coauthor.Builder cb = Coauthor.newBuilder()
                            .setPosition(c.getPosition())
                            .setFullName(backfromnorm(c.getFullName()));
                    if (c.getUserId() != null) cb.setUserId(c.getUserId());

                    b.addCoauthors(cb.build());
                });


        PublicationView publicationView = b.build();

        return publicationView;
    }

    public static MonographView entityToProtoMonograph(Monographic monograph) {
        RefItem type = RefItem.newBuilder()
                .setId(monograph.getType().getId())
                .setName(monograph.getType().getName())
                .build();

        RefItem discipline = RefItem.newBuilder()
                .setId(monograph.getDiscipline().getId())
                .setName(monograph.getDiscipline().getName())
                .build();

        CycleItem.Builder cycle = CycleItem.newBuilder()
                .setName(monograph.getCycle().getName())
                .setIsActive(monograph.getCycle().isActive())
                .setId(monograph.getCycle().getId())
                .setYearFrom(monograph.getCycle().getYearFrom())
                .setYearTo(monograph.getCycle().getYearTo())
                .setActiveYear(monograph.getCycle().getActiveYear());

        if(monograph.getCycle().getMeinVersion()!= null) {
            cycle.setMeinVersionId(monograph.getCycle().getMeinVersion().getId());
        }else {
            cycle.setMeinVersionId(0);
        }
        if(monograph.getCycle().getMeinMonoVersion() != null) {
            cycle.setMonoVersionId(monograph.getCycle().getMeinMonoVersion().getId());
        }else {
            cycle.setMonoVersionId(0);
        }

        cycle.build();

        MonographView.Builder b = MonographView.newBuilder()
                .setId(monograph.getId())
                .setAuthorId(monograph.getAuthorId())
                .setTitle(monograph.getTitle())
                .setDoi(backfromnorm(monograph.getDoi()))
                .setIsbn(backfromnorm(monograph.getIsbn()))
                .setPoints(monograph.getMeinPoints())
                .setMonograficTitle(monograph.getMonograficTitle())
                .setPublicationYear(monograph.getPublicationYear())
                .setCycle(cycle)
                .setType(type)
                .setDiscipline(discipline);

        if (monograph.getMeinMonoPublisherId() != null) {
            b.setMeinMonoPublisherId(monograph.getMeinMonoPublisherId());
        }else{
            b.setMeinMonoPublisherId(0);
        }
        if (monograph.getMeinMonoId() != null) {
            b.setMeinMonoId(monograph.getMeinMonoId());
        }else{
            b.setMeinMonoId(0);
        }

        monograph.getCoauthors().stream()
                .sorted(java.util.Comparator.comparingInt(MonographAuthor::getPosition))
                .forEach(c -> {
                    Coauthor.Builder cb = Coauthor.newBuilder()
                            .setPosition(c.getPosition())
                            .setFullName(backfromnorm(c.getFullName()));
                    if (c.getUserId() != null) cb.setUserId(c.getUserId());

                    b.addCoauthor(cb.build());
                });


        MonographView monographView = b.build();

        return monographView;

    }

    public static ChapterView entityToProtoChapter(MonographChapter monographChapter) {
        RefItem type = RefItem.newBuilder()
                .setId(monographChapter.getType().getId())
                .setName(monographChapter.getType().getName())
                .build();

        RefItem discipline = RefItem.newBuilder()
                .setId(monographChapter.getDiscipline().getId())
                .setName(monographChapter.getDiscipline().getName())
                .build();

        CycleItem.Builder cycle = CycleItem.newBuilder()
                .setName(monographChapter.getCycle().getName())
                .setIsActive(monographChapter.getCycle().isActive())
                .setId(monographChapter.getCycle().getId())
                .setYearFrom(monographChapter.getCycle().getYearFrom())
                .setYearTo(monographChapter.getCycle().getYearTo())
                .setActiveYear(monographChapter.getCycle().getActiveYear());

        if(monographChapter.getCycle().getMeinVersion() != null) {
            cycle.setMeinVersionId(monographChapter.getCycle().getMeinVersion().getId());
        }else {
            cycle.setMeinVersionId(0);
        }
        if(monographChapter.getCycle().getMeinMonoVersion() != null) {
            cycle.setMonoVersionId(monographChapter.getCycle().getMeinMonoVersion().getId());
        }else {
            cycle.setMonoVersionId(0);
        }

        cycle.build();

        ChapterView.Builder b = ChapterView.newBuilder()
                .setId(monographChapter.getId())
                .setAuthorId(monographChapter.getAuthorId())
                .setMonograficChapterTitle(monographChapter.getMonograficChapterTitle())
                .setDoi(backfromnorm(monographChapter.getDoi()))
                .setIsbn(backfromnorm(monographChapter.getIsbn()))
                .setPoints(monographChapter.getMeinPoints())
                .setMonograficTitle(monographChapter.getMonograficTitle())
                .setMonographPublisher(monographChapter.getMonographChapterPublisher())
                .setPublicationYear(monographChapter.getPublicationYear())
                .setCycle(cycle)
                .setType(type)
                .setDiscipline(discipline);

        if (monographChapter.getMeinMonoPublisherId() != null) {
            b.setMeinMonoPublisherId(monographChapter.getMeinMonoPublisherId());
        }else{
            b.setMeinMonoPublisherId(0);
        }
        if (monographChapter.getMeinMonoId() != null) {
            b.setMeinMonoId(monographChapter.getMeinMonoId());
        }else {
            b.setMeinMonoId(0);
        }

        monographChapter.getCoauthors().stream()
                .sorted(java.util.Comparator.comparingInt(MonographChapterAuthor::getPosition))
                .forEach(c -> {
                    Coauthor.Builder cb = Coauthor.newBuilder()
                            .setPosition(c.getPosition())
                            .setFullName(backfromnorm(c.getFullName()));
                    if (c.getUserId() != null) cb.setUserId(c.getUserId());

                    b.addCoauthor(cb.build());
                });


        ChapterView chapterView = b.build();

        return chapterView;
    }

    public static  String normalize(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    public static String backfromnorm(String s) {
        return s == null ? "" : s;
    }

}
