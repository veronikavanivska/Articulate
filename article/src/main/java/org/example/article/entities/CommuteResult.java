package org.example.article.entities;


public record CommuteResult(EvalCycle cycle,
                            org.example.article.entities.MEiN.MeinVersion meinVersion,
                            org.example.article.entities.MEiN.MeinJournal meinJournal,
                            int points,
                            boolean notOnListWarning) { }
