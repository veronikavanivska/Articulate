package org.example.article.helpers;

import jakarta.persistence.criteria.JoinType;
import org.example.article.entities.Publication;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class PublicationSpecification {
   private PublicationSpecification() {}

    public static Specification<Publication> list(
            Long userId, Long typeId, Long disciplineId, Long cycleId, String title) {
       return (root, query, criteriaBuilder) -> {
           List<jakarta.persistence.criteria.Predicate> preds = new ArrayList<>();

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
           if (title != null && !title.trim().isEmpty()) {
               String pattern = "%" + title.trim().toLowerCase() + "%";
               preds.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern));
           }

           return criteriaBuilder.and(preds.toArray(jakarta.persistence.criteria.Predicate[]::new));
       };

    }
}