package org.example.article.helpers;

import com.example.generated.Coauthor;
import com.example.generated.CycleItem;
import com.example.generated.PublicationView;
import com.example.generated.RefItem;
import org.example.article.entities.Publication;
import org.example.article.entities.PublicationCoauthor;
import org.springframework.stereotype.Component;


public final class Mapper {
    private Mapper() {}

    public static PublicationView entityToProto(Publication publication) {

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
            b.setMeinVersionId(publication.getMeinVersionId());  // tu już Long -> long działa
        }
        if (publication.getMeinJournalId() != null) {
            b.setMeinJournalId(publication.getMeinJournalId());
        }

        publication.getCoauthors().stream()
                .sorted(java.util.Comparator.comparingInt(PublicationCoauthor::getPosition))
                .forEach(c -> b.addCoauthors(
                        Coauthor.newBuilder()
                                .setPosition(c.getPosition())
                                .setFullName(backfromnorm(c.getFullName()))
                                .build()
                ));


        PublicationView publicationView = b.build();

        return publicationView;
    }

    public static  String normalize(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    public static String backfromnorm(String s) {
        return s == null ? "" : s;
    }

}
