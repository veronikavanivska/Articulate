package org.example.article.entities;

import org.example.article.entities.MEiN.monographs.MeinMonoPublisher;
import org.example.article.entities.MEiN.monographs.MeinMonoVersion;

public record CommuteResultMonoChapter(
        EvalCycle cycle,
        MeinMonoVersion meinMonoVersion,
        MeinMonoPublisher meinMonoPublisher,
        double points,
        boolean offList) {
}
