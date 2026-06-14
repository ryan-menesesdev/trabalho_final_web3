package com.user.api.services;

import com.user.api.dto.CreateUserDto;
import com.user.api.dto.LoginUserDto;
import com.user.api.dto.RecoveryJwtTokenDto;
import com.user.api.dto.UserProfileDto;
import com.user.api.entitities.Role;
import com.user.api.entitities.User;
import com.user.api.repositories.UserRepository;
import com.user.api.security.authentication.JwtTokenService;
import com.user.api.security.config.SecurityConfiguration;
import com.user.api.security.userdetails.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

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
        List<String> roles = user.getRoles().stream().map(role -> role.getName().name()).toList();

        return new UserProfileDto(user.getId(), user.getEmail(), roles);
    }

    public RecoveryJwtTokenDto generateTokenFromEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + email));

        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        return new RecoveryJwtTokenDto(jwtTokenService.generateToken(userDetails));
    }
}
