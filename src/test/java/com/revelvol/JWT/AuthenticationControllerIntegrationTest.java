package com.revelvol.JWT;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revelvol.JWT.model.User;
import com.revelvol.JWT.repository.TestH2RoleRepository;
import com.revelvol.JWT.repository.TestH2UserRepository;
import com.revelvol.JWT.response.ApiResponse;
import org.junit.jupiter.api.*;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// random port to avoid conflict with existing port
// kalo ga mau nyalain server bisa pake @WebMVCTest build in method yang di inject ama controller nya
@AutoConfigureMockMvc
public class AuthenticationControllerIntegrationTest {

    private static RestTemplate restTemplate;
    private static ObjectMapper objectMapper;


    @LocalServerPort // get the random portfrom the RANDOM_PORT
    private int port;
    private String registerUrl;
    private String authenticateUrl;

    @Autowired
    private TestH2UserRepository testH2UserRepository;
    @Autowired
    private TestH2RoleRepository testH2RoleRepository;
    @Autowired
    private MockMvc mockMvc;


    @BeforeAll  //before all test, initialize this rest  template
    public static void init() {
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
    }

    @BeforeEach //before running each test case, execute this
    public void setUp() {
        String baseUrl = "http://localhost";
        registerUrl = baseUrl.concat(":").concat(String.valueOf(port)).concat("/api/v1/auth/register");
        authenticateUrl = baseUrl.concat(":").concat(String.valueOf(port)).concat("/api/v1/auth/authenticate");
        MockitoAnnotations.openMocks(this);

    }

    @AfterEach// after each refresh the database
    public void tearDown() {
        testH2UserRepository.deleteAll();
        testH2RoleRepository.deleteAll();
    }


    @Test
    public void testRegisterUser() { //todo add json mocking

        User user = new User("test123@gmail.com", "12345457647", new HashSet<>());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<User> requestEntity = new HttpEntity<>(user, headers);

        ResponseEntity<ApiResponse> response = restTemplate.exchange(this.registerUrl,
                HttpMethod.POST,
                requestEntity,
                ApiResponse.class);


        //perform validation to the response entity
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(1, testH2UserRepository.findAll().size());


    }

    @Test
    public void testRegisterUser2() { //todo add json mocking
        User user = new User("test123@gmail.com", "12345457647", new HashSet<>());

        ApiResponse response = restTemplate.postForObject(registerUrl, user, ApiResponse.class);

        //perform validation to the response entity
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Assertions.assertEquals(1, testH2UserRepository.findAll().size());
        System.out.println("token " + response.getData().get("token"));


    }

    @Test
    public void testRegisterUserAlreadyExist() { //todo add json mocking and response validation
        User user = new User("test123@gmail.com", "12345457647", new HashSet<>());

        ApiResponse response = restTemplate.postForObject(registerUrl, user, ApiResponse.class); // first post

        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Assertions.assertEquals(1, testH2UserRepository.findAll().size());

        // Assert that the second post throws an error
        Assertions.assertThrows(Exception.class, () -> {
            restTemplate.postForObject(registerUrl, user, ApiResponse.class); // second post should throw an error
        });

    }

    @Test
    void testRegisterUserNoPassword() throws JsonProcessingException {

        User user = new User();
        user.setEmail("test123@gmail.com");
        user.setUserRoles(new HashSet<>());

        try {
            restTemplate.postForObject(registerUrl, user, ApiResponse.class);

        } catch (HttpClientErrorException e) {

            String responseBody = e.getResponseBodyAsString();

            ApiResponse responseMap = objectMapper.readValue(responseBody, ApiResponse.class);
            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), responseMap.getStatusCode());

            Assertions.assertEquals("rawPassword cannot be null", responseMap.getData("field"));
            Assertions.assertEquals("todo fix this validaiton so the validaiton is in request", "dumbass");


        }

        Assertions.assertEquals(0, testH2UserRepository.findAll().size());
    }

    @Test
    void testRegisterNoEmail() throws JsonProcessingException {
        User user = new User();
        user.setPassword("12345678899132");
        user.setUserRoles(new HashSet<>());

        try {
            restTemplate.postForObject(registerUrl, user, ApiResponse.class);

        } catch (HttpClientErrorException e) {

            String responseBody = e.getResponseBodyAsString();

            ApiResponse responseMap = objectMapper.readValue(responseBody, ApiResponse.class);
            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), responseMap.getStatusCode());
            Assertions.assertEquals("must not be null", responseMap.getData("email"));


        }

        Assertions.assertEquals(0, testH2UserRepository.findAll().size());


    }

    @Test
    void testRegisterUserWithSameEmail() throws JsonProcessingException {  //todo add validaiton  to the response body

        User user = new User("test123@gmail.com", "12345457647", new HashSet<>());
        User user2 = new User("test123@gmail.com", "12345457asdasds647", new HashSet<>());

        ApiResponse response = restTemplate.postForObject(registerUrl, user, ApiResponse.class); // first post

        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Assertions.assertEquals(1, testH2UserRepository.findAll().size());

        //insert second user with same
        try {
            restTemplate.postForObject(registerUrl, user2, ApiResponse.class);

        } catch (HttpClientErrorException e) {

            String responseBody = e.getResponseBodyAsString();

            ApiResponse responseMap = objectMapper.readValue(responseBody, ApiResponse.class);
            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), responseMap.getStatusCode());
            Assertions.assertEquals("User already exist", responseMap.getMessage());


        }

        Assertions.assertEquals(1, testH2UserRepository.findAll().size());
    }

    @Test
    void testRegisterUserLessThan8CharPassword() throws JsonProcessingException {
        User user = new User("test123@gmail.com", "12", new HashSet<>());
        try {
            restTemplate.postForObject(registerUrl, user, ApiResponse.class);

        } catch (HttpClientErrorException e) {

            String responseBody = e.getResponseBodyAsString();

            ApiResponse responseMap = objectMapper.readValue(responseBody, ApiResponse.class);
            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), responseMap.getStatusCode());
            Assertions.assertEquals("the request is not valid", responseMap.getMessage());
            Assertions.assertEquals("please enter minimum password length of 8", responseMap.getData("password"));


        }
        Assertions.assertEquals(0, testH2UserRepository.findAll().size());
    }

    @Test
    void testRegisterUserInvalidEmail() {
        //todo add validaiton  to the response body and email REGEX validation
        User user = new User("test123", "12345676889", new HashSet<>());

        ApiResponse response = restTemplate.postForObject(registerUrl, user, ApiResponse.class); // first post

        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
        Assertions.assertEquals(0, testH2UserRepository.findAll().size());

        // Assert that the second post throws an error
        Assertions.assertThrows(Exception.class, () -> {
            restTemplate.postForObject(registerUrl, user, ApiResponse.class); // second post should throw an error
        });
        System.out.println("Data " + response.toString());

    }

    @Test
    void testRegisterUserNoEmailPassword() {
    }
}
