package org.example.article.entities;


public record CommuteResult(EvalCycle cycle,
                            org.example.article.entities.MEiN.MeinVersion meinVersion, // your package
                            org.example.article.entities.MEiN.MeinJournal meinJournal, // your package
                            int points,
                            boolean notOnListWarning) { }
