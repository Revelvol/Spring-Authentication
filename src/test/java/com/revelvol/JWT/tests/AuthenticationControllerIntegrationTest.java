package com.revelvol.JWT.tests;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revelvol.JWT.model.User;
import com.revelvol.JWT.repository.TestH2RoleRepository;
import com.revelvol.JWT.repository.TestH2UserRepository;
import com.revelvol.JWT.request.AuthenticationRequest;
import com.revelvol.JWT.request.RegisterRequest;
import com.revelvol.JWT.response.ApiResponse;
import com.revelvol.JWT.service.JwtService;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// random port to avoid conflict with existing port
// kalo ga mau nyalain server bisa pake @WebMVCTest build in method yang di inject ama controller nya
@AutoConfigureMockMvc
public class AuthenticationControllerIntegrationTest {

    private static RestTemplate restTemplate;
    private static ObjectMapper objectMapper;

    private static JwtService jwtService;


    @LocalServerPort // get the random portfrom the RANDOM_PORT
    private int port;
    private String registerUrl;
    private String authenticateUrl;

    @Autowired
    private TestH2UserRepository testH2UserRepository;
    @Autowired
    private TestH2RoleRepository testH2RoleRepository;


    @BeforeAll  //before all test, initialize this rest  template
    public static void init() {
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
        jwtService = Mockito.mock(JwtService.class);
        Mockito.when(jwtService.extractUsername(Mockito.anyString())).thenReturn("mocked_username");
        Mockito.when(jwtService.isTokenValid(Mockito.anyString(), Mockito.any(User.class))).thenReturn(true);
    }

    @BeforeEach //before running each test case, execute this
    public void setUp() {
        String baseUrl = "http://localhost";
        registerUrl = baseUrl.concat(":").concat(String.valueOf(port)).concat("/api/v1/auth/register");
        authenticateUrl = baseUrl.concat(":").concat(String.valueOf(port)).concat("/api/v1/auth/authenticate");
        MockitoAnnotations.openMocks(this);

        //mock the jwt service


    }

    @AfterEach// after each refresh the database
    public void tearDown() {
        testH2UserRepository.deleteAll();
        testH2RoleRepository.deleteAll();
    }

    @Test
    public void testRegisterUser() {

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
    public void testRegisterUserWithValidToken() {
        User user = new User("test123@gmail.com", "12345457647", new HashSet<>());

        ApiResponse response = restTemplate.postForObject(registerUrl, user, ApiResponse.class);

        //perform validation to the response entity
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Assertions.assertEquals(1, testH2UserRepository.findAll().size());


        String token = response.getData().get("token").toString();


        Assertions.assertEquals("mocked_username", jwtService.extractUsername(token));

    }

    @Test
    public void testRegisterUserAlreadyExist() {

        RegisterRequest request = new RegisterRequest("test123@gmail.com", "12332131");

        ApiResponse response = restTemplate.postForObject(registerUrl, request, ApiResponse.class); // first post

        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Assertions.assertEquals(1, testH2UserRepository.findAll().size());

        // Assert that the second post throws an error
        Assertions.assertThrows(Exception.class, () -> {
            restTemplate.postForObject(registerUrl, request, ApiResponse.class); // second post should throw an error
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
            System.out.println("response " + responseMap.toString());

            Assertions.assertEquals("must not be null", responseMap.getData("password"));


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
    void testRegisterUserWithSameEmail() throws JsonProcessingException {

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
    void testRegisterUserInvalidEmail() throws JsonProcessingException {
        //todo add validaiton  to the response body and email REGEX validation
        User user = new User("test123", "12345676889", new HashSet<>());

        try {
            restTemplate.postForObject(registerUrl, user, ApiResponse.class);

        } catch (HttpClientErrorException e) {

            String responseBody = e.getResponseBodyAsString();

            ApiResponse responseMap = objectMapper.readValue(responseBody, ApiResponse.class);
            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), responseMap.getStatusCode());
            Assertions.assertEquals("the request is not valid", responseMap.getMessage());
            Assertions.assertEquals("must be a well-formed email address", responseMap.getData("email"));


        }
        Assertions.assertEquals(0, testH2UserRepository.findAll().size());


    }

    @Test
    void testRegisterUserNoEmailPassword() throws JsonProcessingException {
        User user = new User();
        user.setUserRoles(new HashSet<>());
        try {
            restTemplate.postForObject(registerUrl, user, ApiResponse.class);

        } catch (HttpClientErrorException e) {

            String responseBody = e.getResponseBodyAsString();


            ApiResponse responseMap = objectMapper.readValue(responseBody, ApiResponse.class);
            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), responseMap.getStatusCode());
            Assertions.assertEquals("the request is not valid", responseMap.getMessage());
            Assertions.assertEquals("must not be null", responseMap.getData("email"));
            Assertions.assertEquals("must not be null", responseMap.getData("password"));


        }

    }

    // Verification Endpoint


    @Test
    void testAuthenticateValidUser() {
        RegisterRequest registerRequest= new RegisterRequest("test@gmail.com", "123456778");
        AuthenticationRequest authenticationRequest = new AuthenticationRequest("test@gmail.com", "123456778");

        // register the user
        ApiResponse registerResponse=  restTemplate.postForObject(registerUrl, registerRequest, ApiResponse.class);


        // authenticate the user and get new  token
        ApiResponse authenticationResponse = restTemplate.postForObject(authenticateUrl, authenticationRequest, ApiResponse.class);

        Assertions.assertEquals(1, testH2UserRepository.findAll().size());

        Assertions.assertNotNull(registerResponse.getData("token"));
        Assertions.assertNotNull(authenticationResponse.getData("token"));

    }

    @Test
    void testAuthenticateInvalidPassword() throws JsonProcessingException {
        RegisterRequest registerRequest= new RegisterRequest("test@gmail.com", "123456778");
        AuthenticationRequest authenticationRequest = new AuthenticationRequest("test@gmail.com", "178a");

        // register the user
        ApiResponse registerResponse=  restTemplate.postForObject(registerUrl, registerRequest, ApiResponse.class);


        // this should return error
        try {
            restTemplate.postForObject(authenticateUrl, authenticationRequest, ApiResponse.class);


        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();



            ApiResponse responseMap = objectMapper.readValue(responseBody, ApiResponse.class);
            System.out.println("response "+ responseMap.toString());


            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), responseMap.getStatusCode());
            Assertions.assertEquals("Invalid Password", responseMap.getMessage());


        }

        Assertions.assertEquals(1, testH2UserRepository.findAll().size());

    }

    @Test
    void testAuthenticateUserInvalidEmail() throws JsonProcessingException {
        RegisterRequest registerRequest= new RegisterRequest("test@gmail.com", "123456778");
        AuthenticationRequest authenticationRequest = new AuthenticationRequest("test123@gmail.com", "123456778");

        // register the user
        ApiResponse registerResponse=  restTemplate.postForObject(registerUrl, registerRequest, ApiResponse.class);


        // this should return error
        try {
            restTemplate.postForObject(authenticateUrl, authenticationRequest, ApiResponse.class);


        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();



            ApiResponse responseMap = objectMapper.readValue(responseBody, ApiResponse.class);
            System.out.println("response "+ responseMap.toString());


            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), responseMap.getStatusCode());
            Assertions.assertEquals("User does not exist", responseMap.getMessage());


        }

        Assertions.assertEquals(1, testH2UserRepository.findAll().size());
    }

    @Test
    void testAuthenticateUserNotValidEmail() throws JsonProcessingException {
        RegisterRequest registerRequest= new RegisterRequest("test@gmail.com", "123456778");
        AuthenticationRequest authenticationRequest = new AuthenticationRequest("test123", "123456778");

        // register the user
        ApiResponse registerResponse=  restTemplate.postForObject(registerUrl, registerRequest, ApiResponse.class);


        // this should return error
        try {
            restTemplate.postForObject(authenticateUrl, authenticationRequest, ApiResponse.class);


        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();



            ApiResponse responseMap = objectMapper.readValue(responseBody, ApiResponse.class);
            System.out.println("response "+ responseMap.toString());


            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), responseMap.getStatusCode());
            Assertions.assertEquals("the request is not valid", responseMap.getMessage());

            HashMap<String, Object> expectedResponseData = new HashMap<>();

            expectedResponseData.put("email", "must be a well-formed email address");

            Assertions.assertEquals(expectedResponseData, responseMap.getData());


        }

        Assertions.assertEquals(1, testH2UserRepository.findAll().size());
    }

    @Test
    void testAuthenticateUserNoEmail() throws JsonProcessingException {
        RegisterRequest registerRequest= new RegisterRequest("test@gmail.com", "123456778");
        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setPassword("123456778");

        // register the user
        ApiResponse registerResponse=  restTemplate.postForObject(registerUrl, registerRequest, ApiResponse.class);


        // this should return error
        try {
            restTemplate.postForObject(authenticateUrl, authenticationRequest, ApiResponse.class);


        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();



            ApiResponse responseMap = objectMapper.readValue(responseBody, ApiResponse.class);
            System.out.println("response "+ responseMap.toString());


            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), responseMap.getStatusCode());
            Assertions.assertEquals("the request is not valid", responseMap.getMessage());

            HashMap<String,Object> expectedResponseData = new HashMap<>();

            expectedResponseData.put("email", "must not be null");

            Assertions.assertEquals(expectedResponseData, responseMap.getData());




        }

        Assertions.assertEquals(1, testH2UserRepository.findAll().size());
    }

    @Test
    void testAuthenticateUserNoPassword() throws JsonProcessingException {
        RegisterRequest registerRequest= new RegisterRequest("test@gmail.com", "123456778");
        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setEmail("test@gmail.com");

        // register the user
        ApiResponse registerResponse=  restTemplate.postForObject(registerUrl, registerRequest, ApiResponse.class);


        // this should return error
        try {
            restTemplate.postForObject(authenticateUrl, authenticationRequest, ApiResponse.class);


        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();



            ApiResponse responseMap = objectMapper.readValue(responseBody, ApiResponse.class);
            System.out.println("response "+ responseMap.toString());


            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), responseMap.getStatusCode());
            Assertions.assertEquals("the request is not valid", responseMap.getMessage());
            Map<String,Object> expectedResponseData = new HashMap<>();
            expectedResponseData.put("password", "must not be null");

            Assertions.assertEquals(expectedResponseData, responseMap.getData());


        }

        Assertions.assertEquals(1, testH2UserRepository.findAll().size());


    }

    @Test
    void testAuthenticateUserNoEmailAndNoPassword() throws JsonProcessingException {
        RegisterRequest registerRequest= new RegisterRequest("test@gmail.com", "123456778");
        AuthenticationRequest authenticationRequest = new AuthenticationRequest();

        // register the user
        ApiResponse registerResponse=  restTemplate.postForObject(registerUrl, registerRequest, ApiResponse.class);


        // this should return error
        try {
            restTemplate.postForObject(authenticateUrl, authenticationRequest, ApiResponse.class);


        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();



            ApiResponse responseMap = objectMapper.readValue(responseBody, ApiResponse.class);
            System.out.println("response "+ responseMap.toString());


            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), responseMap.getStatusCode());
            Assertions.assertEquals("the request is not valid", responseMap.getMessage());

            HashMap<String, Object> expectedResponseData = new HashMap<>();

            expectedResponseData.put("email", "must not be null");
            expectedResponseData.put("password","must not be null");
            Assertions.assertEquals(expectedResponseData, responseMap.getData());




        }

        Assertions.assertEquals(1, testH2UserRepository.findAll().size());
    }

    @Test
    void testAuthenticateUserInvalidEmailAndNoPassword() throws JsonProcessingException {
        RegisterRequest registerRequest= new RegisterRequest("test@gmail.com", "123456778");
        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setEmail("test123");

        // register the user
        ApiResponse registerResponse=  restTemplate.postForObject(registerUrl, registerRequest, ApiResponse.class);


        // this should return error
        try {
            restTemplate.postForObject(authenticateUrl, authenticationRequest, ApiResponse.class);


        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();



            ApiResponse responseMap = objectMapper.readValue(responseBody, ApiResponse.class);
            System.out.println("response "+ responseMap.toString());


            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), responseMap.getStatusCode());
            Assertions.assertEquals("the request is not valid", responseMap.getMessage());


            Map<String, Object> expectedResponseData = new HashMap<>();

            expectedResponseData.put("password", "must not be null");
            expectedResponseData.put("email","must be a well-formed email address");

            Assertions.assertEquals(expectedResponseData,responseMap.getData());


        }

        Assertions.assertEquals(1, testH2UserRepository.findAll().size());
    }
}
