package com.revelvol.JWT.auth;



public class AuthenticationResponse {
    //add builder and data here

    private String token;

    // Manually implemented no-args constructor
    public AuthenticationResponse() {
    }

    // Manually implemented all-args constructor
    public AuthenticationResponse(String token) {
        this.token = token;
    }

    // Manually implemented getters and setters (or you can use Lombok's @Getter and @Setter)
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }




}