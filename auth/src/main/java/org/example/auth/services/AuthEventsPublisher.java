package org.example.auth.services;

import org.example.auth.rabbitmq.AuthRabbitConfig;
import org.example.auth.rabbitmq.EventEnvelope;
import org.example.auth.rabbitmq.Events;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuthEventsPublisher {
    private final AmqpTemplate amqp;

    public AuthEventsPublisher(AmqpTemplate amqp) {
        this.amqp = amqp;
    }
    public void userRegistered(Long userId, String email) {
        var evt = new EventEnvelope<>(
                "UserRegistered", "v1", java.time.Instant.now(),
                new Events.UserRegistered(userId, email)
        );
        amqp.convertAndSend(AuthRabbitConfig.EXCHANGE, "user.registered", evt);
    }

    public void roleAssigned(Long userId, String role) {
        var evt = new EventEnvelope<>(
                "UserRoleAssigned", "v1", java.time.Instant.now(),
                new Events.UserRoleAssigned(userId, role)
        );
        amqp.convertAndSend(AuthRabbitConfig.EXCHANGE, "user.role.assigned", evt);
    }

    public void roleRevoked(Long userId, String role) {
        var evt = new EventEnvelope<>(
                "UserRoleRevoked", "v1", java.time.Instant.now(),
                new Events.UserRoleRevoked(userId, role)
        );
        amqp.convertAndSend(AuthRabbitConfig.EXCHANGE, "user.role.revoked", evt);
    }

    public void userDeleted(Long userId) {
        var evt = new EventEnvelope<>(
                "UserDeleted", "v1", java.time.Instant.now(),
                new Events.UserDeleted(userId)
        );
        amqp.convertAndSend(AuthRabbitConfig.EXCHANGE, "user.deleted", evt);
    }

}
