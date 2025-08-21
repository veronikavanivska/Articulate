package org.example.profiles;

import org.example.profiles.server.GrpcServer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ProfilesApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProfilesApplication.class, args);
    }

    @Bean
    public CommandLineRunner startGrpcServer(GrpcServer grpcServer) {
        return args -> grpcServer.start();
    }
}
