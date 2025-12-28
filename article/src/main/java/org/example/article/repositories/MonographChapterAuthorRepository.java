package org.example.article.repositories;

import org.example.article.entities.MEiN.monographs.MonographAuthor;
import org.example.article.entities.MEiN.monographs.MonographChapterAuthor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MonographChapterAuthorRepository extends JpaRepository<MonographChapterAuthor, Long> {
    List<MonographChapterAuthor> findByMonographChapterIdOrderByPosition(Long chapterId);

    @Modifying
    @Transactional
    @Query("DELETE FROM MonographChapterAuthor c WHERE c.monographChapter.id = :chapterId")
    void deleteByMonographChapterId(@Param("chapterId") Long chapterId);


    @Modifying
    @Transactional
    @Query(value = """
        update monograph_chapter_author
        set full_name = :fullName
        where user_id = :userId
        """, nativeQuery = true)
    int updateFullNameByUserId(@Param("userId") long userId, @Param("fullName") String fullName);

}
