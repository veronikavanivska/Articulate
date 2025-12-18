package org.example.apigateway.mappers;

import com.example.generated.ChapterView;
import org.example.apigateway.responses.articles.Coauthor;
import org.example.apigateway.responses.articles.CycleItem;
import org.example.apigateway.responses.articles.RefItem;
import org.example.apigateway.responses.mono.ChapterViewResponse;

import java.util.ArrayList;
import java.util.List;

public class ChapterViewMapper {

    public static ChapterViewResponse map(ChapterView chapterView) {
        ChapterViewResponse chapterViewResponse = new ChapterViewResponse();

        RefItem discipline = new RefItem();
        discipline.setId(chapterView.getDiscipline().getId());
        discipline.setName(chapterView.getDiscipline().getName());

        RefItem type = new RefItem();
        type.setId(chapterView.getType().getId());
        type.setName(chapterView.getType().getName());

        CycleItem cycle = new CycleItem();
        cycle.setId(chapterView.getCycle().getId());
        cycle.setName(chapterView.getCycle().getName());
        cycle.setActive(chapterView.getCycle().getIsActive());
        cycle.setYearTo(chapterView.getCycle().getYearTo());
        cycle.setYearFrom(chapterView.getCycle().getYearFrom());
        cycle.setMeinVersionId(chapterView.getCycle().getMeinVersionId());
        List<Coauthor> coauthors = new ArrayList<>();

        for (com.example.generated.Coauthor c : chapterView.getCoauthorList()) {
            Coauthor coauthor = new Coauthor();
            coauthor.setPosition(c.getPosition());
            coauthor.setFullName(c.getFullName());
            coauthor.setUserId(c.getUserId());
            coauthors.add(coauthor);
        }

        chapterViewResponse.setId(chapterView.getId());
        chapterViewResponse.setAuthorId(chapterView.getAuthorId());

        chapterViewResponse.setType(type);
        chapterViewResponse.setDiscipline(discipline);
        chapterViewResponse.setCycle(cycle);

        chapterViewResponse.setMonograficChapterTitle(chapterView.getMonograficChapterTitle());
        chapterViewResponse.setMonograficTitle(chapterView.getMonograficTitle());
        chapterViewResponse.setMonographPublisher(chapterView.getMonographPublisher());
        chapterViewResponse.setDoi(chapterView.getDoi());
        chapterViewResponse.setIsbn(chapterView.getIsbn());
        chapterViewResponse.setPoints(chapterView.getPoints());

        chapterViewResponse.setMeinMonoPublisherId(chapterView.getMeinMonoPublisherId());
        chapterViewResponse.setMeinMonoId(chapterView.getMeinMonoId());

        chapterViewResponse.setCoauthor(coauthors);

        return chapterViewResponse;
    }
}
