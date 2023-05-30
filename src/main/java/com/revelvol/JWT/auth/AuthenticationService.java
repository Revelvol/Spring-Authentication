package com.revelvol.JWT.auth;

import com.revelvol.JWT.entity.Role;
import com.revelvol.JWT.entity.User;
import com.revelvol.JWT.repository.RoleRepository;
import com.revelvol.JWT.repository.UserRepository;
import com.revelvol.JWT.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;

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

    @Autowired
    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.roleRepository = roleRepository;
    }


    public AuthenticationResponse register(RegisterRequest request) {
        Set<Role> roles = new HashSet<>(); //might want to initiate role her
        Role role = roleRepository.getOrCreateByName("USER");
        roles.add(role);
        User user = new User(request.getEmail(), passwordEncoder.encode(request.getPassword()), roles);

        user = userRepository.save(user);

        // add the connection for user and role
        // todo carefull here since we load all users upon ading connection so it might bebetter to justadd connection to the intermediary table
        role.getUsers().add(user);
        roleRepository.save(role);
        var jwtToken = jwtService.generateToken(user);

        AuthenticationResponse authResponse = new AuthenticationResponse(jwtToken);

        return null;
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        return null;
    }
}
