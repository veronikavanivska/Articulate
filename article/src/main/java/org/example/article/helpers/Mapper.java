package org.example.article.helpers;

import com.example.generated.*;
import org.example.article.entities.MEiN.monographs.MonographAuthor;
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
                .setYearTo(publication.getCycle().getYearTo());

        if(publication.getCycle().getMeinVersion().getId() != null) {
             cycle.setMeinVersionId(publication.getCycle().getMeinVersion().getId());
        }
        if(publication.getCycle().getMeinMonoVersion().getId() != null) {
            cycle.setMonoVersionId(publication.getCycle().getMeinMonoVersion().getId());
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
        }
        if (publication.getMeinJournalId() != null) {
            b.setMeinJournalId(publication.getMeinJournalId());
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
                .setYearTo(monograph.getCycle().getYearTo());

        if(monograph.getCycle().getMeinVersion().getId() != null) {
            cycle.setMeinVersionId(monograph.getCycle().getMeinVersion().getId());
        }
        if(monograph.getCycle().getMeinMonoVersion().getId() != null) {
            cycle.setMonoVersionId(monograph.getCycle().getMeinMonoVersion().getId());
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
                .setCycle(cycle)
                .setType(type)
                .setDiscipline(discipline);

        if (monograph.getMeinMonoPublisherId() != null) {
            b.setMeinMonoPublisherId(monograph.getMeinMonoPublisherId());
        }
        if (monograph.getMeinMonoId() != null) {
            b.setMeinMonoId(monograph.getMeinMonoId());
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

    public static  String normalize(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    public static String backfromnorm(String s) {
        return s == null ? "" : s;
    }

}
