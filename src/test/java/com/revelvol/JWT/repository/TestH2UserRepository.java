package com.revelvol.JWT.repository;

import com.revelvol.JWT.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TestH2UserRepository extends JpaRepository<User,Integer> {
    Optional<User> findByEmail(String email);
}
