package org.example.profiles.helper;

import com.example.generated.DisciplineRef;
import com.example.generated.ProfileWorker;
import org.example.profiles.repositories.ProfileWorkerDisciplineRepository;
import org.springframework.stereotype.Component;

@Component
public class Mapper {

    public final ProfileWorkerDisciplineRepository profileWorkerDisciplineRepository;

    public Mapper(ProfileWorkerDisciplineRepository profileWorkerDisciplineRepository) {
        this.profileWorkerDisciplineRepository = profileWorkerDisciplineRepository;

    }

    public com.example.generated.ProfileWorker buildProtoWorker(Long userId, org.example.profiles.entities.ProfileWorker workerEntity) {

        var links = profileWorkerDisciplineRepository.findAllByUserIdWithDiscipline(userId);

        ProfileWorker.Builder b = ProfileWorker.newBuilder()
                .setDegreeTitle(workerEntity.getDegreeTitle() == null ? "" : workerEntity.getDegreeTitle())
                .setUnitName(workerEntity.getUnitName() == null ? "" : workerEntity.getUnitName());

        for (var link : links) {
            var d = link.getDiscipline();
            if (d != null) {
                b.addDisciplines(DisciplineRef.newBuilder()
                        .setId(d.getId())
                        .setName(d.getName())
                        .build());
            }
        }

        return b.build();
    }

}
