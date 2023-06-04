package com.revelvol.JWT.repository;

import com.revelvol.JWT.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User,Integer> {
//    @Query("SELECT u FROM User u WHERE u.email = :email ")
//    Optional<User> findByEmail (@Param("email")  String email);
    Optional<User> findByEmail(String email);
}
