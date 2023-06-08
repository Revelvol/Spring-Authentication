package com.revelvol.JWT.service;

import com.revelvol.JWT.exception.UserNotFoundException;
import com.revelvol.JWT.model.User;
import com.revelvol.JWT.model.UserInformation;
import com.revelvol.JWT.repository.UserInformationRepository;
import com.revelvol.JWT.repository.UserRepository;
import com.revelvol.JWT.request.UserInformationRequest;
import com.revelvol.JWT.response.ApiResponse;
import com.revelvol.JWT.response.UserInformationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class UserInformationService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserInformationRepository userInformationRepository;

    @Autowired
    public UserInformationService(JwtService jwtService, UserRepository userRepository, UserInformationRepository userInformationRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.userInformationRepository = userInformationRepository;
    }

    private User getUser(String token) {
        String email = jwtService.extractUsername(token);

        // todo implement id user insertion to the token body
        return userRepository.findByEmail
                (email).orElseThrow(() -> new UserNotFoundException("User Does not Exist"));
    }

    public ApiResponse getUserInformation(String token) {

        User user = getUser(token);

        UserInformation userInformation = userInformationRepository.findById(user.getId()).orElseThrow(
                () -> new UserNotFoundException("User information Does not Exist")
        );

        UserInformationResponse response = new UserInformationResponse(HttpStatus.OK.value(), "User Information is found");
        setResponse(userInformation, response);
        return response;

    }


    public ApiResponse addUserInformation(UserInformationRequest request, String token) {


        User user = getUser(token);
        System.out.println(user.toString());

        // from the request extract the argument and add the user infomraiotn


        UserInformation userInformation = new UserInformation();
        userInformation.setUser(user);
        userInformation.setGender(request.getGender());
        userInformation.setLanguage(request.getLanguage());
        userInformation.setDateOfBirth(request.getDateOfBirth());
        userInformation.setFullName(request.getFullName());
        userInformation.setPhoneNumber(request.getPhoneNumber());

        System.out.println(userInformation.toString());


        //save the user and user information
        userInformationRepository.save(userInformation);

        UserInformationResponse response = new UserInformationResponse(
                HttpStatus.OK.value(), "user Information successfully created"
        );

        setResponse(userInformation, response);

        return response;


    }

    private static void setResponse(UserInformation userInformation, UserInformationResponse response) {
        response.setUserId(userInformation.getUserId());
        response.setFullName(userInformation.getFullName());
        response.setGender(userInformation.getGender());
        response.setPhoneNumber(userInformation.getPhoneNumber());
        response.setLanguage(userInformation.getLanguage());
        response.setDateOfBirth(userInformation.getDateOfBirth());
    }
}
