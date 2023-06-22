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

    private UserInformation getInformationFromUser(User user) {
        UserInformation userInformation = userInformationRepository.findById(user.getId()).orElseThrow(
                () -> new UserNotFoundException("User information Does not Exist")
        );
        return userInformation;
    }

    private static void setResponse(UserInformation userInformation, UserInformationResponse response) {
        response.setUserId(userInformation.getUserId());
        response.setFullName(userInformation.getFullName());
        response.setGender(userInformation.getGender());
        response.setPhoneNumber(userInformation.getPhoneNumber());
        response.setLanguage(userInformation.getLanguage());
        response.setDateOfBirth(userInformation.getDateOfBirth());
    }

    public ApiResponse getUserInformation(String token) {

        User user = getUser(token);

        UserInformation userInformation = getInformationFromUser(user);

        UserInformationResponse response = new UserInformationResponse(HttpStatus.OK.value(), "User Information is found");
        setResponse(userInformation, response);
        return response;

    }


    public ApiResponse addUserInformation(UserInformationRequest request, String token) {

        User user = getUser(token);
        // from the request extract the argument and add the user information
        UserInformation userInformation = userInformationRepository.findById(user.getId()).orElse(null);

        if (userInformation != null) {
            return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "User Information already exist, please us patch or put to manipulate the data ");
        } else {

            userInformation = new UserInformation();
            userInformation.setUser(user);
            userInformation.setGender(request.getGender());
            userInformation.setLanguage(request.getLanguage());
            userInformation.setDateOfBirth(request.getDateOfBirth());
            userInformation.setFullName(request.getFullName());
            userInformation.setPhoneNumber(request.getPhoneNumber());


            //save the user and user information
            userInformationRepository.save(userInformation);

            UserInformationResponse response = new UserInformationResponse(
                    HttpStatus.OK.value(), "user Information successfully created"
            );

            setResponse(userInformation, response);

            return response;
        }

    }

    public ApiResponse updateUserInformation(UserInformationRequest request, String token) {
        // update > put
        User user = getUser(token);
        UserInformation userInformation = getInformationFromUser(user);

        userInformation.setGender(request.getGender());
        userInformation.setLanguage(request.getLanguage());
        userInformation.setDateOfBirth(request.getDateOfBirth());
        userInformation.setFullName(request.getFullName());
        userInformation.setPhoneNumber(request.getPhoneNumber());
        userInformationRepository.save(userInformation);

        UserInformationResponse response = new UserInformationResponse(
                HttpStatus.OK.value(), "user Information successfully created"
        );

        setResponse(userInformation, response);


        return response;
    }

    public ApiResponse patchUserInformation(UserInformationRequest request, String token) {
        User user = getUser(token);
        UserInformation userInformation = getInformationFromUser(user);

        if (request.getGender() != null) {
            userInformation.setGender(request.getGender());
        }

        if (request.getLanguage() != null) {
            userInformation.setLanguage(request.getLanguage());
        }

        if (request.getDateOfBirth() != null) {
            userInformation.setDateOfBirth(request.getDateOfBirth());
        }

        if (request.getFullName() != null) {
            userInformation.setFullName(request.getFullName());
        }

        if (request.getPhoneNumber() != null) {
            userInformation.setPhoneNumber(request.getPhoneNumber());
        }

        userInformationRepository.save(userInformation);

        UserInformationResponse response = new UserInformationResponse(
                HttpStatus.OK.value(), "User information successfully updated"
        );

        setResponse(userInformation, response);

        return response;
    }

}
