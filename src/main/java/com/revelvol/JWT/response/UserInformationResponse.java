package com.revelvol.JWT.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.revelvol.JWT.model.User;
import jakarta.persistence.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

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
        addData("userId", userId);
    }

    @JsonIgnore
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
        addData("fullName", fullName);
    }

    @JsonIgnore
    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("WIB"));
        this.dateOfBirth = dateOfBirth;
        addData("dateOfBirth", dateFormat.format(dateOfBirth));
    }

    @JsonIgnore
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        addData("phoneNumber", phoneNumber);
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
