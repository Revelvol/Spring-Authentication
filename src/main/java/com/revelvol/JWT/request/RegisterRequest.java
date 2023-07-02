package com.revelvol.JWT.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @Email
    @NotNull
    private String email;

    @Size(min=8, message = "please enter minimum password length of 8")
    @NotNull
    private String password;

    public RegisterRequest() {
    }

    public RegisterRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
