package com.user.api.services;

import com.user.api.dto.*;
import com.user.api.entitities.Role;
import com.user.api.entitities.User;
import com.user.api.enums.RoleName;
import com.user.api.repositories.UserRepository;
import com.user.api.security.authentication.JwtTokenService;
import com.user.api.security.config.SecurityConfiguration;
import com.user.api.security.userdetails.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityConfiguration securityConfiguration;

    public RecoveryJwtTokenDto authenticateUser(LoginUserDto loginUserDto) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(loginUserDto.email(), loginUserDto.password());
        Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return new RecoveryJwtTokenDto(jwtTokenService.generateToken(userDetails));
    }

    public void createUser(CreateUserDto createUserDto) {
        User newUser = User.builder()
                .email(createUserDto.email())
                .password(securityConfiguration.passwordEncoder().encode(createUserDto.password()))
                .roles(List.of(Role.builder().name(createUserDto.role()).build()))
                .build();

        userRepository.save(newUser);
    }

    public UserProfileDto getCurrentUserProfile(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + email));

        List<String> roles = user.getRoles().stream()
                .filter(role -> role != null && role.getName() != null)
                .map(role -> role.getName().name())
                .toList();

        return new UserProfileDto(user.getId(), user.getName(), user.getEmail(), roles);
    }

    public RecoveryJwtTokenDto generateTokenFromEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + email));

        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        return new RecoveryJwtTokenDto(jwtTokenService.generateToken(userDetails));
    }

    public UserProfileDto updateProfile(String email, UpdateProfileDto dto) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + email));

        user.setName(dto.name());

        RoleName roleNameDesejada;

        try {
            roleNameDesejada = RoleName.valueOf(dto.role().toString());
        } catch (Exception e) {
            roleNameDesejada = RoleName.valueOf(dto.role().name());
        }

        RoleName finalRoleNameDesejada = roleNameDesejada;
        boolean jaPossuiRole = user.getRoles() != null && user.getRoles().stream().anyMatch(r -> r.getName() == finalRoleNameDesejada);

        if (!jaPossuiRole) {
            if (user.getRoles() == null || user.getRoles().isEmpty()) {
                user.setRoles(new ArrayList<>(List.of(Role.builder().name(roleNameDesejada).build())));
            } else {
                user.getRoles().get(0).setName(roleNameDesejada);
            }
        }

        userRepository.save(user);

        return new UserProfileDto(user.getId(), user.getName(), user.getEmail(), List.of(roleNameDesejada.name()));
    }
}
