package org.example.auth.helpers;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.example.auth.Client;
import org.example.auth.clients.ProfileCommandClient;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Configuration
public class GrpcInitializer {

    private final Environment environment;
    private final ApplicationContext applicationContext;
    private final List<ManagedChannel> channels = new ArrayList<>();

    public GrpcInitializer(Environment environment, ApplicationContext applicationContext) {
        this.environment = environment;
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void initializeClients() {
        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forPackage(ProfileCommandClient.class.getPackageName()))
        );

        Set<Class<?>> allAnnotatedClasses = reflections.getTypesAnnotatedWith(Client.class);

        for (Class<?> clientClass : allAnnotatedClasses) {
            Client clientAnnotation = clientClass.getAnnotation(Client.class);

            String host = environment.resolvePlaceholders(clientAnnotation.host());
            int port = Integer.parseInt(environment.resolvePlaceholders(clientAnnotation.port()));

            ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                    .usePlaintext()
                    .build();

            channels.add(channel);

            try {
                Object bean = applicationContext.getBean(clientClass);
                Method initMethod = clientClass.getDeclaredMethod("init", Channel.class);
                initMethod.invoke(bean, channel);
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize client: " + clientClass.getName(), e);
            }
        }
    }

    @PreDestroy
    public void shutdownChannels() {
        for (ManagedChannel ch : channels) {
            ch.shutdown();
        }
    }
}