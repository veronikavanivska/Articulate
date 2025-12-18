package org.example.profiles.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ProfileWorkerStatementId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "discipline_id")
    private Long disciplineId;

    @Column(name = "eval_year")
    private Integer evalYear;
}
