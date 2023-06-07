package com.revelvol.JWT.repository;

import com.revelvol.JWT.model.UserInformation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInformationRepository extends JpaRepository<UserInformation, Integer> {

}
