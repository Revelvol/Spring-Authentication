package com.revelvol.JWT.controller;


import com.revelvol.JWT.exception.UserNotFoundException;
import com.revelvol.JWT.model.UserInformation;
import com.revelvol.JWT.response.ApiResponse;
import com.revelvol.JWT.service.UserInformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
public class UserInformationController {

    private UserInformationService userInformationService;

    @Autowired
    public UserInformationController(UserInformationService userInformationService) {
        this.userInformationService = userInformationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getUserInformation(
            @RequestHeader HttpHeaders headers) {

        String authHeader = headers.getFirst("Authorization");
        String token = authHeader.substring(7);

        try {
            ApiResponse userInformation = userInformationService.getUserInformation(token);

            return ResponseEntity.ok(userInformation);
        } catch (UserNotFoundException e) {

            ApiResponse response = new ApiResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
