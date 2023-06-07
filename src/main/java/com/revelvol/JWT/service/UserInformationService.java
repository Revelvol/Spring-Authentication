package com.revelvol.JWT.service;

import com.revelvol.JWT.exception.UserNotFoundException;
import com.revelvol.JWT.model.User;
import com.revelvol.JWT.model.UserInformation;
import com.revelvol.JWT.repository.UserInformationRepository;
import com.revelvol.JWT.repository.UserRepository;
import com.revelvol.JWT.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserInformationService {

    private JwtService jwtService;
    private UserRepository userRepository;
    private UserInformationRepository userInformationRepository;

    @Autowired
    public UserInformationService(JwtService jwtService, UserRepository userRepository, UserInformationRepository userInformationRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.userInformationRepository = userInformationRepository;
    }

    public ApiResponse getUserInformation(String token) {

        String email = jwtService.extractUsername(token);

        // todo i think the jwt bearer can be injected with the user id so the code wont fetch user twice
        User user = userRepository.findByEmail
                (email).orElseThrow(() -> new UserNotFoundException("User Does not Exist"));

        UserInformation userInformation = userInformationRepository.findById(user.getId()).orElseThrow(
                () -> new UserNotFoundException("User information Does not Exist")
        );

        ApiResponse response = new ApiResponse(HttpStatus.OK.value(), "User Information is found");
        response.addData("information", userInformation);
        return response;

    }
}
