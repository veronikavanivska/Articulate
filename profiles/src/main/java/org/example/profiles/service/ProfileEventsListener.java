//package org.example.profiles.service;
//
//
//import lombok.extern.slf4j.Slf4j;
//import org.example.profiles.entities.ProfileAdmin;
//import org.example.profiles.entities.ProfileUser;
//import org.example.profiles.entities.ProfileWorker;
//import org.example.profiles.rabbitmq.EventEnvelope;
//import org.example.profiles.rabbitmq.Events;
//import org.example.profiles.rabbitmq.ProfileRabbitConfig;
//import org.example.profiles.repositories.ProfileAdminRepository;
//import org.example.profiles.repositories.ProfileUserRepository;
//import org.example.profiles.repositories.ProfileWorkerRepository;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.Instant;
//
//@Slf4j
//@Service
//public class ProfileEventsListener {
//
//    private final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
//    private final ProfileUserRepository profileUserRepository;
//    private final ProfileWorkerRepository profileWorkerRepository;
//    private final ProfileAdminRepository profileAdminRepository;
//
//    public ProfileEventsListener(ProfileUserRepository profileUserRepository, ProfileWorkerRepository profileWorkerRepository, ProfileAdminRepository profileAdminRepository) {
//        this.profileUserRepository = profileUserRepository;
//        this.profileWorkerRepository = profileWorkerRepository;
//        this.profileAdminRepository = profileAdminRepository;
//    }
//
//    @RabbitListener(queues = ProfileRabbitConfig.QUEUE_PROFILE)
//    @Transactional
//    public void onEvent(EventEnvelope<?> envelope) {
//        switch (envelope.type()) {
//            case "UserRegistered" -> handleUserRegistered(cast(envelope, Events.UserRegistered.class));
//            case "UserRoleAssigned" -> handleRoleAssigned(cast(envelope, Events.UserRoleAssigned.class));
//            case "UserRoleRevoked" -> handleRoleRevoked(cast(envelope, Events.UserRoleRevoked.class));
//            case "UserDeleted" -> handleUserDeleted(cast(envelope, Events.UserDeleted.class));
//            default -> {
//                log.warn("Unhandled event type: {}", envelope.type());
//            }
//        }
//    }
//
//
//    private <T> EventEnvelope<T> cast(EventEnvelope<?> env, Class<T> clazz) {
//        T data = mapper.convertValue(env.data(), clazz);
//        return new EventEnvelope<>(env.type(), env.version(), env.occuresAt(), data);
//    }
//
//    private void handleUserDeleted(EventEnvelope<?> envelope) {
//        EventEnvelope<Events.UserDeleted> typed = cast(envelope, Events.UserDeleted.class);
//
//        Events.UserDeleted data = typed.data();
//
//        profileUserRepository.findByUserId(data.userId()).ifPresent(profileUserRepository::delete);
//
//    }
//
//    private void handleRoleRevoked(EventEnvelope<?> envelope) {
//        EventEnvelope<Events.UserRoleRevoked> typed = cast(envelope, Events.UserRoleRevoked.class);
//
//        Events.UserRoleRevoked data = typed.data();
//        if("ROLE_ADMIN".equals(data.role())) {
//            profileAdminRepository.findByUserId(data.userId()).ifPresent(profileAdminRepository::delete);
//        }
//        if("ROLE_WORKER".equals(data.role())) {
//            profileWorkerRepository.findByUserId(data.userId()).ifPresent(profileWorkerRepository::delete);
//        }
//
//    }
//
//    private void handleRoleAssigned(EventEnvelope<?> envelope) {
//        EventEnvelope<Events.UserRoleAssigned> typed = cast(envelope, Events.UserRoleAssigned.class);
//
//        Events.UserRoleAssigned data = typed.data();
//        if("ROLE_WORKER".equals(data.role())) {
//            profileUserRepository.findByUserId(data.userId()).ifPresent(user -> {
//                profileWorkerRepository.findByUserId(data.userId()).ifPresentOrElse(
//                        worker -> {},
//                        () ->{
//                            var profileWorker = new ProfileWorker();
//                            profileWorker.setUser(user);
//                            profileWorker.setDegreeTitle("");
//                            profileWorker.setUnitName("");
//                            profileWorkerRepository.save(profileWorker);
//                });
//            });
//        }
//        if("ROLE_ADMIN".equals(data.role())) {
//            profileUserRepository.findByUserId(data.userId()).ifPresent(user -> {
//                profileAdminRepository.findByUserId(data.userId()).ifPresentOrElse(
//                        admin -> {},
//                        ()-> {
//                            var profileAdmin = new ProfileAdmin();
//                            profileAdmin.setUser(user);
//                            profileAdmin.setUnitName("");
//                            profileAdminRepository.save(profileAdmin);
//                        });
//            });
//        }
//    }
//
//    private void handleUserRegistered(EventEnvelope<?> envelope) {
//        EventEnvelope<Events.UserRegistered> typed = cast(envelope, Events.UserRegistered.class);
//
//        Events.UserRegistered d = typed.data();
//        profileUserRepository.findByUserId(d.userId()).ifPresentOrElse(
//                u -> {},
//                ()->{
//                    var profile = new ProfileUser();
//                    profile.setUserId(d.userId());
//                    profile.setFullname("");
//                    profile.setBio("");
//                    profile.setUpdatedAt(Instant.now());
//                    profile.setCreatedAt(Instant.now());
//                    profileUserRepository.save(profile);
//
//                }
//        );
//    }
//
//
//}
