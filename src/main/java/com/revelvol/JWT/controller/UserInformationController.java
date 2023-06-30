package com.revelvol.JWT.controller;


import com.revelvol.JWT.exception.UserNotFoundException;
import com.revelvol.JWT.model.User;
import com.revelvol.JWT.repository.UserRepository;
import com.revelvol.JWT.request.UserInformationRequest;
import com.revelvol.JWT.response.ApiResponse;
import com.revelvol.JWT.response.UserInformationResponse;
import com.revelvol.JWT.service.JwtService;
import com.revelvol.JWT.service.UserInformationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
public class UserInformationController {

    private UserInformationService userInformationService;
    private UserRepository userRepository;

    private JwtService jwtService;


    @Autowired
    public UserInformationController(UserInformationService userInformationService, UserRepository userRepository, JwtService jwtService) {
        this.userInformationService = userInformationService;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
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
        // todo fix this logic by adding id to the jsot token
        ApiResponse userInformation = null;
        try {
            userInformation = userInformationService.getUserInformation(token);
            return ResponseEntity.ok(userInformation);
        } catch (UserNotFoundException e) {

            String email = jwtService.extractUsername(token);
            User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(
                    "User Does not Exist"));

            UserInformationResponse emptyResponse = new UserInformationResponse(200,
                    "User Information is not found, returning empty user information body");
            emptyResponse.setUserId(user.getId());
            return ResponseEntity.ok(emptyResponse);
        }

    }

    @Transactional
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<ApiResponse> postUserInformation(
            @Valid @RequestBody(required = false) UserInformationRequest request,
            @RequestHeader HttpHeaders headers
    ) {

        ApiResponse response;
        ResponseEntity<ApiResponse> response1 = validateRequest(
                request);
        if (response1 != null) return response1;
        String token = headers.getFirst("Authorization").substring(7);
        response = userInformationService.addUserInformation(request, token);
        return ResponseEntity.ok(response);

    }


    @PutMapping(consumes = "application/json", produces = "application/json")
    @Transactional
    public ResponseEntity<ApiResponse> putUserInformation(
            @RequestBody(required = false) UserInformationRequest request,
            @RequestHeader HttpHeaders headers
    ) {
        ApiResponse response;

        ResponseEntity<ApiResponse> response1 = validateRequest(
                request);
        if (response1 != null) return response1;
        String token = headers.getFirst("Authorization").substring(7);
        response = userInformationService.updateUserInformation(request, token); // Replace with your update logic
        return ResponseEntity.ok(response);

    }

    @PatchMapping(consumes = "application/json", produces = "application/json")
    @Transactional
    public ResponseEntity<ApiResponse> patchUserInformation(
            @RequestBody UserInformationRequest request,
            @RequestHeader HttpHeaders headers
    ) {
        ApiResponse response;
        ResponseEntity<ApiResponse> response1 = validateRequest(
                request);
        if (response1 != null) return response1;
        String token = headers.getFirst("Authorization").substring(7);
        response = userInformationService.patchUserInformation(request, token); // Replace with your patch logic
        return ResponseEntity.ok(response);

    }


}
