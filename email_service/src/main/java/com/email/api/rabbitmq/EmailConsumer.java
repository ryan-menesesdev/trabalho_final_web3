package com.email.api.rabbitmq;

import com.email.api.dto.EmailDto;
import com.email.api.service.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class EmailConsumer {

    private final EmailService emailService;

    public EmailConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = "${broker.queue.email.name}")
    public void listenEmailQueue(@Payload EmailDto emailDto) {
        emailService.sendEmail(emailDto);
        System.out.println("Email recebido na fila para: " + emailDto.emailTo());
    }
}
