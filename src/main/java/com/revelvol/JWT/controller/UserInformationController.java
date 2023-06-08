package com.revelvol.JWT.controller;


import com.revelvol.JWT.exception.UserNotFoundException;
import com.revelvol.JWT.model.UserInformation;
import com.revelvol.JWT.request.UserInformationRequest;
import com.revelvol.JWT.response.ApiResponse;
import com.revelvol.JWT.service.UserInformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<ApiResponse> postUserInformation(
            @RequestBody UserInformationRequest request,
            @RequestHeader HttpHeaders headers
            ){

        ApiResponse response;
        // todo implement error controler advice for more global error handling
        String token = headers.getFirst("Authorization").substring(7);

        try {
            response = userInformationService.addUserInformation(request,token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {

            response = new ApiResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<ApiResponse> putUserInformation(
            @RequestBody UserInformationRequest request,
            @RequestHeader HttpHeaders headers
    ) {
        ApiResponse response;
        String token = headers.getFirst("Authorization").substring(7);

        try {
            response = userInformationService.updateUserInformation(request, token); // Replace with your update logic
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response = new ApiResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PatchMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<ApiResponse> patchUserInformation(
            @RequestBody UserInformationRequest request,
            @RequestHeader HttpHeaders headers
    ) {
        ApiResponse response;
        String token = headers.getFirst("Authorization").substring(7);

        try {
            response = userInformationService.patchUserInformation(request, token); // Replace with your patch logic
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response = new ApiResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }



}
