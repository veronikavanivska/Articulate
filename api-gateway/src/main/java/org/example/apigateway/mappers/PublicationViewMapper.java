package org.example.apigateway.mappers;

import com.example.generated.PublicationView;
import org.example.apigateway.responses.articles.Coauthor;
import org.example.apigateway.responses.articles.CycleItem;
import org.example.apigateway.responses.articles.PublicationViewResponse;
import org.example.apigateway.responses.articles.RefItem;

import java.util.ArrayList;
import java.util.List;

public class PublicationViewMapper {

    public static PublicationViewResponse map(PublicationView publicationView) {
        PublicationViewResponse publicationResponse = new PublicationViewResponse();

        RefItem discipline = new RefItem();
        discipline.setId(publicationView.getDiscipline().getId());
        discipline.setName(publicationView.getDiscipline().getName());

        RefItem type = new RefItem();
        type.setId(publicationView.getType().getId());
        type.setName(publicationView.getType().getName());

        CycleItem cycle = new CycleItem();
        cycle.setId(publicationView.getCycle().getId());
        cycle.setName(publicationView.getCycle().getName());
        cycle.setActive(publicationView.getCycle().getIsActive());
        cycle.setYearTo(publicationView.getCycle().getYearTo());
        cycle.setYearFrom(publicationView.getCycle().getYearFrom());
        cycle.setMeinVersionId(publicationView.getCycle().getMeinVersionId());
        cycle.setMeinMonoVersionId(publicationView.getCycle().getMonoVersionId());
        List<Coauthor> coauthors = new ArrayList<>();

        for (com.example.generated.Coauthor c : publicationView.getCoauthorsList()) {
            Coauthor coauthor = new Coauthor();
            coauthor.setPosition(c.getPosition());
            coauthor.setFullName(c.getFullName());
            coauthor.setUserId(c.getUserId());
            coauthors.add(coauthor);
        }

        publicationResponse.setId(publicationView.getId());
        publicationResponse.setDiscipline(discipline);
        publicationResponse.setType(type);
        publicationResponse.setCycle(cycle);
        publicationResponse.setPublicationYear(publicationView.getPublicationYear());
        publicationResponse.setIssn(publicationView.getIssn());
        publicationResponse.setJournalTitle(publicationView.getJournalTitle());
        publicationResponse.setEissn(publicationView.getEissn());
        publicationResponse.setDoi(publicationView.getDoi());
        publicationResponse.setCoauthors(coauthors);
        publicationResponse.setMeinVersionId(publicationView.getMeinVersionId());
        publicationResponse.setMeinJournalId(publicationView.getMeinJournalId());
        publicationResponse.setMeinPoints(publicationView.getMeinPoints());
        publicationResponse.setOwnerId(publicationView.getOwnerId());
        publicationResponse.setTitle(publicationView.getTitle());

        return publicationResponse;
    }
}
