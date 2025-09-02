package org.example.profiles.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProfileRabbitConfig {
    public static final String EXCHANGE = "user.events";
    public static final String QUEUE_PROFILE = "profile.user";
    public static final String BINDING_KEY = "user.#";

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE,true,false);
    }

    @Bean
    public Queue profileQueue() {
        return QueueBuilder.durable(QUEUE_PROFILE).build();
    }

    @Bean
    public Binding profileBinding(Queue profileQueue, TopicExchange userEventsExchange) {
        return BindingBuilder.bind(profileQueue).to(userEventsExchange).with(BINDING_KEY);
    }

    @Bean
    public MessageConverter jacksonConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
