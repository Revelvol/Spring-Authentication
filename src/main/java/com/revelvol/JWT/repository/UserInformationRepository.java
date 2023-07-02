package com.revelvol.JWT.repository;

import com.revelvol.JWT.model.UserInformation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserInformationRepository extends JpaRepository<UserInformation, Integer> {
    Optional<UserInformation> findById(Integer id);
}
