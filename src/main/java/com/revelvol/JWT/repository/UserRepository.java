package com.revelvol.JWT.repository;

import com.revelvol.JWT.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository {
    @Query("SELECT u FROM User u WHERE u.email = :email ")
    Optional<User> findByEmail (@Param("email")  String email);
}
