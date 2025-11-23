package org.example.article.entities.MEiN.article;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "mein_code")
public class MeinCode {
    @Id
    private String code;
    private String name;
}
