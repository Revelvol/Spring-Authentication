package com.revelvol.JWT;

import com.revelvol.JWT.controller.AuthenticationController;
import com.revelvol.JWT.controller.DemoController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JwtApplicationTests {

	@Autowired
	private AuthenticationController authenticationController;

	@Autowired
	private DemoController demoController;

	@Test
	void contextLoads() throws Exception {
		Assertions.assertNotNull(authenticationController);
		Assertions.assertNotNull(demoController);
	}

}
