package com.revelvol.JWT;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JwtApplication {

	// todo fix bug dimana setiap authenticatethe user dia bakal create new user dengan empty email dan new hashed password

	public static void main(String[] args) {
		SpringApplication.run(JwtApplication.class, args);
	}

}
