package org.example.article.repositories;

import org.example.article.entities.MEiN.monographs.MonographAuthor;
import org.example.article.entities.MEiN.monographs.MonographChapter;
import org.example.article.entities.PublicationCoauthor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MonographAuthorRepository extends JpaRepository<MonographAuthor, Long> {
    List<MonographAuthor> findByMonographIdOrderByPosition(Long monographId);

    @Modifying
    @Transactional
    @Query("DELETE FROM MonographAuthor c WHERE c.monograph.id = :monographId")
    void deleteByMonographId(@Param("monographId") Long monographId);


    @Modifying
    @Transactional
    @Query(value = """
        update monograph_author
        set full_name = :fullName
        where user_id = :userId
        """, nativeQuery = true)
    int updateFullNameByUserId(@Param("userId") long userId, @Param("fullName") String fullName);

}
