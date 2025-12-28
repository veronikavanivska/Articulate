package org.example.article.helpers;

import org.springframework.data.jpa.domain.Specification;

public final class SpecText {
    private SpecText() {}

    public static <T> Specification<T> containsIgnoreCase(String value, String fieldName) {
        return (root, query, cb) -> {
            if (value == null || value.trim().isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + value.trim().toLowerCase() + "%";
            return cb.like(cb.lower(root.get(fieldName)), pattern);
        };
    }
}
