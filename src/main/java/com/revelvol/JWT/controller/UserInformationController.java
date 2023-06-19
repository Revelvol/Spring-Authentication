package com.revelvol.JWT.controller;


import com.revelvol.JWT.exception.UserNotFoundException;
import com.revelvol.JWT.model.UserInformation;
import com.revelvol.JWT.request.UserInformationRequest;
import com.revelvol.JWT.response.ApiResponse;
import com.revelvol.JWT.service.UserInformationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
public class UserInformationController {

    private UserInformationService userInformationService;

    @Autowired
    public UserInformationController(UserInformationService userInformationService) {
        this.userInformationService = userInformationService;
    }

    private static ResponseEntity<ApiResponse> validateRequest(UserInformationRequest request) {
        ApiResponse response;
        // Case: Request Body is Empty
        if (request == null) {
            response = new ApiResponse(HttpStatus.BAD_REQUEST.value(), "Request body is required.");
            return ResponseEntity.badRequest().body(response);
        }
        return null;
    }


    @GetMapping
    public ResponseEntity<ApiResponse> getUserInformation(@RequestHeader HttpHeaders headers) {
        String authHeader = headers.getFirst("Authorization");
        String token = authHeader.substring(7);

        ApiResponse userInformation = userInformationService.getUserInformation(token);
        return ResponseEntity.ok(userInformation);
    }


    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<ApiResponse> postUserInformation(
            @Valid @RequestBody(required = false) UserInformationRequest request,
            @RequestHeader HttpHeaders headers
            ){

        ApiResponse response;
        ResponseEntity<ApiResponse> response1 = validateRequest(
                request);
        if (response1 != null) return response1;


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
            @RequestBody(required = false) UserInformationRequest request,
            @RequestHeader HttpHeaders headers
    ) {
        ApiResponse response;

        ResponseEntity<ApiResponse> response1 = validateRequest(
                request);
        if (response1 != null) return response1;

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
        ResponseEntity<ApiResponse> response1 = validateRequest(
                request);
        if (response1 != null) return response1;
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
