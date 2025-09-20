package org.example.article.entities;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "publication_type")
public class PublicationType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;
}
