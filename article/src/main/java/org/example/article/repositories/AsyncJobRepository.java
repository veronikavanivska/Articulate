package org.example.article.repositories;

import org.example.article.entities.AsyncJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface AsyncJobRepository extends JpaRepository<AsyncJob, Long> {
    Optional<AsyncJob> findFirstByTypeAndBusinessKeyAndStatusIn(
            String type,
            String businessKey,
            Collection<AsyncJob.Status> statuses
    );
}
