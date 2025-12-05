package org.example.article.entities;


import org.example.article.entities.MEiN.article.MeinJournal;
import org.example.article.entities.MEiN.article.MeinVersion;

public record CommuteResultArticle(EvalCycle cycle,
                                   MeinVersion meinVersion,
                                   MeinJournal meinJournal,
                                   int points,
                                   boolean offList) { }
