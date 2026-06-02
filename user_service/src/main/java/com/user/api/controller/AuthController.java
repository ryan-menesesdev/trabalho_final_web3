package com.user.api.controller;

import com.user.api.dto.EmailDto;
import com.user.api.dto.EmailRequestDto;
import com.user.api.dto.VerifyCodeRequestDto;
import com.user.api.entitities.Role;
import com.user.api.entitities.User;
import com.user.api.enums.RoleName;
import com.user.api.rabbitmq.UserProducer;
import com.user.api.repositories.UserRepository;
import com.user.api.services.CodigoCacheService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CodigoCacheService codigoCacheService;
    private final UserProducer userProducer;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          CodigoCacheService codigoCacheService,
                          UserProducer userProducer) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.codigoCacheService = codigoCacheService;
        this.userProducer = userProducer;
    }

    @PostMapping("/request-code")
    public ResponseEntity<?> requestCode(@RequestBody EmailRequestDto request) {
        if (request == null) {
            return ResponseEntity.badRequest().body("Campo 'email' é obrigatório.");
        }

        String email = request.email().trim().toLowerCase();

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            String randomPassword = UUID.randomUUID().toString();
            Role role = Role.builder().name(RoleName.ROLE_CUSTOMER).build();
            User newUser = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(randomPassword))
                    .roles(List.of(role))
                    .build();

            return userRepository.save(newUser);
        });

        String code = codigoCacheService.generateAndStore(email);

        UUID userUuid = user.getId();

        EmailDto emailDto = new EmailDto(
                email,
                "Seu código de acesso",
                "Seu código é: " + code,
                userUuid
        );

        userProducer.sendEmail(emailDto);

        return ResponseEntity.ok("Código gerado e enviado por e-mail (simulado).");
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody VerifyCodeRequestDto request) {
        if (request == null) {
            return ResponseEntity.badRequest().body("Campos 'email' e 'code' são obrigatórios.");
        }

        String email = request.email().trim().toLowerCase();
        String code = request.code().trim();

        boolean valid = codigoCacheService.validate(email, code);

        if (!valid) {
            return ResponseEntity.badRequest().body("Código inválido ou expirado.");
        }

        codigoCacheService.remove(email);

        User user = userRepository.findByEmail(email).orElse(null);
        UUID userUuid = (user != null) ? user.getId() : null;

        EmailDto emailDto = new EmailDto(
                email,
                "Código verificado",
                "Seu código foi verificado com sucesso.",
                userUuid
        );

        userProducer.sendEmail(emailDto);

        return ResponseEntity.ok("Código validado com sucesso.");
    }
}
