package org.example.profiles.helper;

import lombok.RequiredArgsConstructor;
import org.example.profiles.entities.ProfileAdmin;
import org.example.profiles.entities.ProfileUser;
import org.example.profiles.entities.ProfileWorker;
import org.example.profiles.repositories.ProfileAdminRepository;
import org.example.profiles.repositories.ProfileUserRepository;
import org.example.profiles.repositories.ProfileWorkerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ProfileCommandsHandler {

    private final ProfileUserRepository profileUserRepository;
    private final ProfileWorkerRepository profileWorkerRepository;
    private final ProfileAdminRepository profileAdminRepository;

    @Transactional
    public void ensureUserProfile(long userId) {
        profileUserRepository.findByUserId(userId).ifPresentOrElse(
                u -> {},
                () -> {
                    var profile = new ProfileUser();
                    profile.setUserId(userId);
                    profile.setFullname("");
                    profile.setBio("");
                    profile.setCreatedAt(Instant.now());
                    profile.setUpdatedAt(Instant.now());
                    profileUserRepository.save(profile);
                }
        );
    }

    @Transactional
    public void ensureWorkerProfile(long userId) {
        // żeby nie było zależności kolejności wywołań, dbamy o bazowy profil:
        ensureUserProfile(userId);

        profileUserRepository.findByUserId(userId).ifPresent(user -> {
            profileWorkerRepository.findByUserId(userId).ifPresentOrElse(
                    w -> {},
                    () -> {
                        var pw = new ProfileWorker();
                        pw.setUser(user);
                        pw.setDegreeTitle("");
                        pw.setUnitName("");
                        profileWorkerRepository.save(pw);
                    }
            );
        });
    }

    @Transactional
    public void ensureAdminProfile(long userId) {
        ensureUserProfile(userId);

        profileUserRepository.findByUserId(userId).ifPresent(user -> {
            profileAdminRepository.findByUserId(userId).ifPresentOrElse(
                    a -> {},
                    () -> {
                        var pa = new ProfileAdmin();
                        pa.setUser(user);
                        pa.setUnitName("");
                        profileAdminRepository.save(pa);
                    }
            );
        });
    }

    @Transactional
    public void deleteWorkerProfile(long userId) {
        profileWorkerRepository.findByUserId(userId)
                .ifPresent(profileWorkerRepository::delete);
    }

    @Transactional
    public void deleteAdminProfile(long userId) {
        profileAdminRepository.findByUserId(userId)
                .ifPresent(profileAdminRepository::delete);
    }

    @Transactional
    public void deleteAllProfiles(long userId) {
        // kolejność bezpieczna, gdy są FK z worker/admin do user:
        deleteWorkerProfile(userId);
        deleteAdminProfile(userId);

        profileUserRepository.findByUserId(userId)
                .ifPresent(profileUserRepository::delete);
    }
}

