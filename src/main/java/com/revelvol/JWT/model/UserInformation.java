package com.revelvol.JWT.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import org.springframework.lang.NonNull;

import javax.xml.crypto.Data;
import java.util.Date;

@Entity
public class UserInformation {

    // both of this map user id as the primary key for this user information column
    @Id
    @Column(name = "user_id")
    private int userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @NotBlank(message = "fullName cannot be empty")
    private String fullName;

    @Past(message = "Date cannot be pass current date")
    private Date dateOfBirth;

    private String phoneNumber;

    private String gender;

    private String language;

    //todo add address here one to one connection


    public UserInformation() {
    }

    public UserInformation(String fullName, Date dateOfBirth, String phoneNumber, String gender, String language) {
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.language = language;
    }


    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
        return "UserInformation{" +
                "userId=" + userId +
                ", fullName='" + fullName + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", gender='" + gender + '\'' +
                ", language='" + language + '\'' +
                '}';
    }
}
