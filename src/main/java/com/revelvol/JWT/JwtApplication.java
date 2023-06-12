package com.revelvol.JWT;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JwtApplication {

	// todo fix a bug where, in postmethod if the request body is not found it will return 404 error

	public static void main(String[] args) {
		SpringApplication.run(JwtApplication.class, args);
	}

}
