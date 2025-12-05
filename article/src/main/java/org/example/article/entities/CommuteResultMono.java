package org.example.article.entities;

import org.example.article.entities.MEiN.monographs.MeinMonoPublisher;
import org.example.article.entities.MEiN.monographs.MeinMonoVersion;

public record CommuteResultMono(EvalCycle cycle,
                                MeinMonoVersion meinMonoVersion,
                                MeinMonoPublisher meinMonoPublisher,
                                int points,
                                boolean offList) {
}
