package com.revelvol.JWT.response;


import com.fasterxml.jackson.annotation.JsonIgnore;

public class AuthenticationResponse extends ApiResponse{
    //add builder and data here

    private String token;

    // Manually implemented all args constructor
    public AuthenticationResponse(int statusCode, String message, String token) {
        super(statusCode,message);
        this.token = token;
        addData("token", token);
    }


    // Manually implemented getters and setters (or you can use Lombok's @Getter and @Setter)
    @JsonIgnore
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
        addData("token", token);
    }






}