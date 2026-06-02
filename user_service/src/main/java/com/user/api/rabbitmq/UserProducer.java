package com.user.api.rabbitmq;

import com.user.api.dto.EmailDto;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UserProducer {
    private final RabbitTemplate rabbitTemplate;
    private final String queueName;

    public UserProducer(
            RabbitTemplate rabbitTemplate,
            @Value("${broker.queue.email.name}") String queueName
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.queueName = queueName;
    }

    public void sendEmail(EmailDto dto) {
        try {
            rabbitTemplate.convertAndSend(queueName, dto);
            System.out.println("Mensagem enviada para fila: " + queueName);
        } catch (AmqpException ex) {
            ex.printStackTrace();
        }
    }
}
