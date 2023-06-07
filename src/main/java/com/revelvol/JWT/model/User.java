package com.revelvol.JWT.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;


@Entity
@Table(name = "_user")
public class User implements UserDetails {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(unique = true)
    private String email;
    private String password;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name= "role_id")
    )
    private Set<Role> userRoles = new HashSet<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private UserInformation userInformation;


    //getter and setter

    public User() {
    }

    public User(String email, String password, Set<Role> userRoles) {
        this.email = email;
        this.password = password;
        this.userRoles = userRoles;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        //map the authorities from the user roles set
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (Role role : userRoles) {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
        }
        return authorities;
    }


    public String getPassword() {
        return password;
    }
    // OVERRIDE THIS
    @Override
    public String getUsername() {
        //get email as the unique username
        return getEmail();
    }

    // OVERIDE THIS
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // OVERIDE THIS
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // OVERIDE THIS
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // OVERIDE THIS
    @Override
    public boolean isEnabled() {
        return true;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Role> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(Set<Role> userRoles) {
        this.userRoles = userRoles;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", userRoles=" +
                '}';
    }





}
