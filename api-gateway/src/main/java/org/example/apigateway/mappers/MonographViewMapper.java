package org.example.apigateway.mappers;

import com.example.generated.MonographView;
import org.example.apigateway.responses.articles.Coauthor;
import org.example.apigateway.responses.articles.CycleItem;
import org.example.apigateway.responses.articles.RefItem;
import org.example.apigateway.responses.mono.MonographViewResponse;

import java.util.ArrayList;
import java.util.List;

public class MonographViewMapper {

    public static MonographViewResponse map(MonographView monographView) {
        MonographViewResponse monographViewResponse = new MonographViewResponse();

        RefItem discipline = new RefItem();
        discipline.setId(monographView.getDiscipline().getId());
        discipline.setName(monographView.getDiscipline().getName());

        RefItem type = new RefItem();
        type.setId(monographView.getType().getId());
        type.setName(monographView.getType().getName());

        CycleItem cycle = new CycleItem();
        cycle.setId(monographView.getCycle().getId());
        cycle.setName(monographView.getCycle().getName());
        cycle.setActive(monographView.getCycle().getIsActive());
        cycle.setYearTo(monographView.getCycle().getYearTo());
        cycle.setYearFrom(monographView.getCycle().getYearFrom());
        cycle.setMeinVersionId(monographView.getCycle().getMeinVersionId());
        cycle.setMeinMonoVersionId(monographView.getCycle().getMonoVersionId());
        List<Coauthor> coauthors = new ArrayList<>();

        for (com.example.generated.Coauthor c : monographView.getCoauthorList()) {
            Coauthor coauthor = new Coauthor();
            coauthor.setPosition(c.getPosition());
            coauthor.setFullName(c.getFullName());
            coauthor.setUserId(c.getUserId());
            coauthors.add(coauthor);
        }

        monographViewResponse.setId(monographView.getId());
        monographViewResponse.setAuthorId(monographView.getAuthorId());
        monographViewResponse.setDiscipline(discipline);
        monographViewResponse.setType(type);
        monographViewResponse.setCycle(cycle);

        monographViewResponse.setTitle(monographView.getTitle());
        monographViewResponse.setDoi(monographView.getDoi());
        monographViewResponse.setIsbn(monographView.getIsbn());
        monographViewResponse.setPoints(monographView.getPoints());
        monographViewResponse.setMonograficTitle(monographView.getMonograficTitle());
        monographViewResponse.setMeinMonoPublisherId(monographView.getMeinMonoPublisherId());
        monographViewResponse.setMeinMonoId(monographView.getMeinMonoId());
        monographViewResponse.setCoauthors(coauthors);

        return monographViewResponse;
    }
}
