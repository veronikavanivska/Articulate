package org.example.article.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "discipline")
public class Discipline {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
}
