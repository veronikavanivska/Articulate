package org.example.article.helpers;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import org.example.article.clients.SlotsClient;
import org.example.article.Client;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.lang.reflect.Method;
import java.util.Set;

@Configuration
public class GrpcInitializer {

    private final Environment environment;

    public GrpcInitializer(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void initializeClients() {

        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forPackage(SlotsClient.class.getPackageName()))
        );

        Set<Class<?>> allAnnotatedClasses = reflections.getTypesAnnotatedWith(Client.class);

        for (Class<?> clientClass : allAnnotatedClasses) {
            Client clientAnnotation = clientClass.getAnnotation(Client.class);

            String host = environment.resolvePlaceholders(clientAnnotation.host());
            int port = Integer.parseInt(environment.resolvePlaceholders(clientAnnotation.port()));

            ManagedChannel channel = ManagedChannelBuilder
                    .forAddress(host, port)
                    .usePlaintext()
                    .build();

            try {
                Method initMethod = clientClass.getDeclaredMethod("init", Channel.class);
                initMethod.invoke(null, channel);
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize client: " + clientClass.getName(), e);
            }
        }
    }
}