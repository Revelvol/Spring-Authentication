package com.revelvol.JWT.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String mesasge){
        super(mesasge);
    }
}
