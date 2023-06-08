package com.revelvol.JWT.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.revelvol.JWT.model.User;
import jakarta.persistence.*;

import java.util.Date;

public class UserInformationResponse extends ApiResponse {


    private int userId;

    private String fullName;
    private Date dateOfBirth;
    private String phoneNumber;
    private String gender;

    private String language;

    public UserInformationResponse(int statusCode, String message) {
        super(statusCode, message);
    }

    public UserInformationResponse(int statusCode, String message, int userId , String fullName, Date dateOfBirth, String phoneNumber, String gender, String language) {
        super(statusCode, message);
        this.userId = userId;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.language = language;
    }

    @JsonIgnore
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
        addData("user_id", userId);
    }

    @JsonIgnore
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
        addData("full_name", fullName);
    }

    @JsonIgnore
    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        addData("date_of_birth", dateOfBirth);
    }

    @JsonIgnore
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        addData("phone_number", phoneNumber);
    }

    @JsonIgnore
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
        addData("gender", gender);
    }

    @JsonIgnore
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
        addData("language", language);
    }
}
