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


    private static void setResponse(UserInformation userInformation, UserInformationResponse response) {
        response.setUserId(userInformation.getUserId());
        response.setFullName(userInformation.getFullName());
        response.setGender(userInformation.getGender());
        response.setPhoneNumber(userInformation.getPhoneNumber());
        response.setLanguage(userInformation.getLanguage());
        response.setDateOfBirth(userInformation.getDateOfBirth());
    }

    public ApiResponse getUserInformation(String token) {

        // extract the user information from the token id claims
        UserInformation userInformation = userInformationRepository.findById(jwtService.extractUserId(token)).orElseThrow(
                () -> new UserNotFoundException("User Does Not Exist"));

        UserInformationResponse response = new UserInformationResponse(HttpStatus.OK.value(),
                "User Information is found");
        setResponse(userInformation, response);
        return response;

    }


    public ApiResponse addUserInformation(UserInformationRequest request, String token) {
        Integer userId = jwtService.extractUserId(token);
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("User Does Not Exist"));
        UserInformation userInformation = userInformationRepository.findById(userId).orElse(null);

        if (userInformation != null) {
            return new ApiResponse(HttpStatus.BAD_REQUEST.value(),
                    "User Information already exist, please use patch or put to manipulate the data ");
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

            UserInformationResponse response = new UserInformationResponse(HttpStatus.OK.value(),
                    "User Information successfully created");

            setResponse(userInformation, response);

            return response;
        }

    }

    public ApiResponse updateUserInformation(UserInformationRequest request, String token) {
        // update = put
        UserInformation userInformation = userInformationRepository.findById(jwtService.extractUserId(token)).orElseThrow(
                () -> new UserNotFoundException("User Information Does not Exist"));

        userInformation.setGender(request.getGender());
        userInformation.setLanguage(request.getLanguage());
        userInformation.setDateOfBirth(request.getDateOfBirth());
        userInformation.setFullName(request.getFullName());
        userInformation.setPhoneNumber(request.getPhoneNumber());
        userInformationRepository.save(userInformation);

        UserInformationResponse response = new UserInformationResponse(HttpStatus.OK.value(),
                "User updated successfully");

        setResponse(userInformation, response);


        return response;
    }

    public ApiResponse patchUserInformation(UserInformationRequest request, String token) {
        UserInformation userInformation = userInformationRepository.findById(jwtService.extractUserId(token)).orElseThrow(
                () -> new UserNotFoundException("User Information Does not Exist"));

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

        UserInformationResponse response = new UserInformationResponse(HttpStatus.OK.value(),
                "User updated successfully");

        setResponse(userInformation, response);

        return response;
    }

    public ApiResponse deleteUserInformation(String token) {

        Integer userId = jwtService.extractUserId(token);

        userInformationRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("User information Does not Exist"));
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("User Does Not Exist"));

        //synchronize user which are the parent for the fetch
        user.removeUserInformation();
        userRepository.save(user);
        userInformationRepository.deleteById(user.getId());


        UserInformationResponse response = new UserInformationResponse(HttpStatus.OK.value(),
                "User id:" + user.getId() + " information has been deleted");

        return response;

    }
}
