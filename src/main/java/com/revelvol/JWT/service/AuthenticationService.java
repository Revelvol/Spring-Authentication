package com.revelvol.JWT.service;

import com.revelvol.JWT.exception.InvalidPasswordException;
import com.revelvol.JWT.exception.UserAlreadyExistsException;
import com.revelvol.JWT.exception.UserNotFoundException;
import com.revelvol.JWT.model.Role;
import com.revelvol.JWT.model.User;
import com.revelvol.JWT.repository.RoleRepository;
import com.revelvol.JWT.repository.UserRepository;
import com.revelvol.JWT.request.AuthenticationRequest;
import com.revelvol.JWT.request.RegisterRequest;
import com.revelvol.JWT.response.ApiResponse;
import com.revelvol.JWT.response.AuthenticationResponse;
import com.revelvol.JWT.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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


    public ApiResponse register(RegisterRequest request) {
        Set<Role> roles = new HashSet<>();


        Role role = roleRepository.getOrCreateByName("USER");

        roles.add(role);

        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        // perform logic to see if the user already exist
        if (user != null){
            throw new UserAlreadyExistsException("User already exist");
        } else {
            user = new User(request.getEmail(), passwordEncoder.encode(request.getPassword()), roles);

            try {
                user = userRepository.save(user);
            } catch (Exception e) {
                System.out.println("there is error in saving user ");
            }

            Map<String, Object> claims = getClaimsFromUser(user);

            // the connection are added automatically for many to many
            var jwtToken = jwtService.generateToken(claims,user);

            AuthenticationResponse authResponse = new AuthenticationResponse(HttpStatus.OK.value(),
                    "registration success",
                    jwtToken);
            return authResponse;
        }

    }

    private static Map<String, Object> getClaimsFromUser(User user) {
        // extract the roles from the user
        Set<Role> userRoles = user.getUserRoles();
        Set<String> userRolesNameArray = userRoles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        // ad the additional claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("roles", userRolesNameArray);
        return claims;
    }

    public ApiResponse authenticate(AuthenticationRequest request) {

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new UserNotFoundException(
                "User does not exist"));

        if (!passwordEncoder.matches(request.getPassword(),  user.getPassword())){
            throw new InvalidPasswordException("Invalid Password");
        }

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(),
                request.getPassword()));
        Map<String, Object> claims = getClaimsFromUser(user);
        var jwtToken = jwtService.generateToken(claims, user);

        AuthenticationResponse authResponse = new AuthenticationResponse(HttpStatus.OK.value(),
                "authentication success",
                jwtToken);

        return authResponse;

    }


}
