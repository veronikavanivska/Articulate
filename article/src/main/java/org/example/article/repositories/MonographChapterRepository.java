package org.example.article.repositories;

import org.example.article.entities.MEiN.monographs.MonographChapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MonographChapterRepository extends JpaRepository<MonographChapter, Long> {

}
