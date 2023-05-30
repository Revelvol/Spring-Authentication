package com.revelvol.JWT.auth;

import com.revelvol.JWT.repository.UserRepository;
import com.revelvol.JWT.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Autowired
    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService
    }


    public AuthenticationResponse register(RegisterRequest request) {
        var user = User.builder()
                .username(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // encode the password before saving to database
                //.role(Role.USER), might want to add custom role here and add role detail
                .build();

        userRepository.save(user);

        var jwtToken =jwtService.generateToken(user);
        return AuthenticationResponse.builder()

        return null;
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        return null;
    }
}
