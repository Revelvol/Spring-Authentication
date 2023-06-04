package com.revelvol.JWT.auth;

//rest api to allow creation and delete of user that need to be authenticated


import com.revelvol.JWT.entity.User;
import com.revelvol.JWT.repository.UserRepository;
import com.revelvol.JWT.request.AuthenticationRequest;
import com.revelvol.JWT.request.RegisterRequest;
import com.revelvol.JWT.response.ApiResponse;
import com.revelvol.JWT.response.AuthenticationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService, UserRepository userRepository) {
        this.authenticationService = authenticationService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(
            @RequestBody RegisterRequest request
    ) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user != null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<ApiResponse> register(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }
}
