package com.revelvol.JWT.auth;

import com.revelvol.JWT.entity.Role;
import com.revelvol.JWT.entity.User;
import com.revelvol.JWT.repository.RoleRepository;
import com.revelvol.JWT.repository.UserRepository;
import com.revelvol.JWT.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, RoleRepository roleRepository, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.roleRepository = roleRepository;
        this.authenticationManager = authenticationManager;
    }


    public AuthenticationResponse register(RegisterRequest request) {
        Set<Role> roles = new HashSet<>();


        Role role = roleRepository.getOrCreateByName("USER");

        roles.add(role);
        User user = new User(request.getEmail(), passwordEncoder.encode(request.getPassword()), roles);

        user = userRepository.save(user);

        // the connection are added automatically for many to many
        var jwtToken = jwtService.generateToken(user);

        AuthenticationResponse authResponse = new AuthenticationResponse(jwtToken);

        return authResponse;
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(); // todo implement exception handling here

        var jwtToken = jwtService.generateToken(user);

        AuthenticationResponse authResponse = new AuthenticationResponse(jwtToken);

        return authResponse;
    }
}
