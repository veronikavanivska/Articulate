package org.example.article.helpers;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.apache.poi.ss.formula.functions.T;
import org.example.article.entities.MEiN.monographs.MonographChapter;
import org.example.article.entities.MEiN.monographs.Monographic;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public record ChapterSpecification() {

    public static Specification<MonographChapter> list(
            Long userId, Long typeId, Long disciplineId, Long cycleId){
        return(root, query, criteriaBuilder) -> {
            List<Predicate> preds = new ArrayList<>();

            if(userId != null) {
                preds.add(criteriaBuilder.equal(root.get("authorId"), userId));
            }
            if(typeId != null) {
                preds.add(criteriaBuilder.equal(root.join("type", JoinType.LEFT).get("id"), typeId));
            }

            if(disciplineId != null) {
                preds.add(criteriaBuilder.equal(root.join("discipline", JoinType.LEFT).get("id"), disciplineId));
            }

            if(cycleId != null) {
                preds.add(criteriaBuilder.equal(root.join("cycle", JoinType.LEFT).get("id"), cycleId));
            }

            return criteriaBuilder.and(preds.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }
}
