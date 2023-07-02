package com.revelvol.JWT.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

public class UserInformationRequest {

    @NotNull
    private String fullName;

    @Past(message = "Date of birth must be in the past")
    @DateTimeFormat
    private Date dateOfBirth;

    @Pattern(regexp = "^\\+\\d{1,3}\\d{4,14}$", message = "Phone number must be in international format, e.g., +1234567890")
    private String phoneNumber;

    @Pattern(regexp = "^[MF]$", message = "Gender must be either 'M' or 'F'")
    private String gender;

    private String language;

    public UserInformationRequest() {
    }

    public UserInformationRequest(String fullName, Date dateOfBirth, String phoneNumber, String gender, String language) {
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.language = language;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public String toString() {
        return "{" +
                "fullName='" + fullName + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", gender='" + gender + '\'' +
                ", language='" + language + '\'' +
                '}';
    }
}
