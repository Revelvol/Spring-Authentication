package com.revelvol.JWT.tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revelvol.JWT.model.User;
import com.revelvol.JWT.repository.TestH2RoleRepository;
import com.revelvol.JWT.repository.TestH2UserInformationRepository;
import com.revelvol.JWT.repository.TestH2UserRepository;
import com.revelvol.JWT.request.RegisterRequest;
import com.revelvol.JWT.request.UserInformationRequest;
import com.revelvol.JWT.response.ApiResponse;
import com.revelvol.JWT.service.JwtService;
import org.junit.jupiter.api.*;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class UserInformationControllerIntegrationTest {
    private static RestTemplate restTemplate;
    private static ObjectMapper objectMapper;


    private static HttpHeaders headers;

    private static JwtService jwtService;
    @LocalServerPort
    private int port;

    private String url;
    private String registerUrl;
    private String authenticateUrl;

    private String token;
    private WebTestClient webTestClient;


    @Autowired
    private TestH2UserRepository testH2UserRepository;
    @Autowired
    private TestH2RoleRepository testH2RoleRepository;

    @Autowired
    private TestH2UserInformationRepository testH2UserInformationRepository;


    @BeforeAll  //before all test, initialize this rest  template
    public static void init() {
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
        jwtService = new JwtService();
        headers = new HttpHeaders();
    }

    @BeforeEach //before running each test case, execute this
    public void setUp() {
        String baseUrl = "http://localhost";
        url = baseUrl.concat(":").concat(String.valueOf(port)).concat("/api/v1/user");
        registerUrl = baseUrl.concat(":").concat(String.valueOf(port)).concat("/api/v1/auth/register");
        authenticateUrl = baseUrl.concat(":").concat(String.valueOf(port)).concat("/api/v1/auth/authenticate");

        RegisterRequest registerRequest = new RegisterRequest("test@gmail.com", "1234567890");

        ApiResponse response = restTemplate.postForObject(registerUrl, registerRequest, ApiResponse.class);

        token = response.getData("token").toString();

        // new web test client setup for patch
        webTestClient = WebTestClient.bindToServer().baseUrl(url).build();


        MockitoAnnotations.openMocks(this);


    }

    @AfterEach// after each refresh the database
    public void tearDown() {
        testH2UserRepository.deleteAll();
        testH2RoleRepository.deleteAll();
        testH2UserInformationRepository.deleteAll();
    }

    @Test
    void testGetUserNoToken() throws JsonProcessingException {

        try {
            restTemplate.getForObject(url, Object.class);
        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            ApiResponse responseMap = objectMapper.readValue(responseBody, ApiResponse.class);


            Assertions.assertEquals(responseMap.getStatusCode(), HttpStatus.FORBIDDEN.value());
        }
        Assertions.assertEquals(0, testH2UserInformationRepository.findAll().size());
    }

    @Test
    void testGetUserWithValidTokenReturn400NoUser() {
        headers.setBearerAuth(token);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(url, HttpMethod.GET, entity, ApiResponse.class);
        } catch (HttpClientErrorException e) {
            Assertions.assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
            Assertions.assertEquals(
                    "400 : \"{\"statusCode\":400,\"message\":\"User information Does not Exist\",\"data\":{}}\"",
                    e.getMessage());
        }
        Assertions.assertEquals(0, testH2UserInformationRepository.findAll().size());
    }

    @Test
    void testGetUserWithExpiredToken() throws JsonProcessingException {
        RegisterRequest registerRequest = new RegisterRequest("test@gmail.com", "1234567890");
        // generate the expired token for the registered user
        User user = new User();
        user.setEmail("test@gmail.com");
        user.setPassword("1234567890");
        String expiredToken = jwtService.generateExpiredToken(user);


        // do the http exchange to the get user but it was expired

        headers.setBearerAuth(expiredToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(url, HttpMethod.GET, entity, ApiResponse.class);
        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();

            ApiResponse responseMap = objectMapper.readValue(responseBody, ApiResponse.class);
            Assertions.assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
            Assertions.assertEquals("JWT token has expired. Please renew the token.", responseMap.getMessage());
        }

        Assertions.assertEquals(0, testH2UserInformationRepository.findAll().size());
    }

    @Test
    void testGetUserWithMalformedToken() throws JsonProcessingException {
        String malformedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGdtYWlsLmNvbSIsImlhdCI6MTUxNjIzOTAyMiwgImV4cCI6MTUxNjIzOTAyMy";


        // do the http exchange to the get user but it was expired

        headers.setBearerAuth(malformedToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(url, HttpMethod.GET, entity, ApiResponse.class);
        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();

            ApiResponse responseMap = objectMapper.readValue(responseBody, ApiResponse.class);
            Assertions.assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
            Assertions.assertEquals("Token is not a valid JWT", responseMap.getMessage());
        }

        Assertions.assertEquals(0, testH2UserInformationRepository.findAll().size());
    }

    @Test
    void testGetUserWithSignatureException() throws JsonProcessingException {
        String signatureExceptionToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGdtYWlsLmNvbSIsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNTE2MjM5MDIzfQ.iGT_frriLe4L3xmAMkdAx_Ob7MsR4n2nHHiTBssIU9k";


        // do the http exchange to the get user but it was expired

        headers.setBearerAuth(signatureExceptionToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(url, HttpMethod.GET, entity, ApiResponse.class);
        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();

            ApiResponse responseMap = objectMapper.readValue(responseBody, ApiResponse.class);
            Assertions.assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
            Assertions.assertEquals("Token Signature is invalid", responseMap.getMessage());
        }

        Assertions.assertEquals(0, testH2UserInformationRepository.findAll().size());
    }

    @Test
    void testGetUserReturnEmptyInformation() {
        // test to get user based on the json token, but the information is empty because the user havent add their data
        headers.setBearerAuth(token);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<ApiResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, ApiResponse.class);

        Assertions.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assertions.assertEquals("User Information is not found, returning empty user information body",
                response.getBody().getMessage());
        Assertions.assertNotNull(response.getBody().getData("userId"));

        Assertions.assertEquals(0, testH2UserInformationRepository.findAll().size());

    }

    @Test
    void testPostAndGetUserOK() {

        headers.setBearerAuth(token);
        UserInformationRequest payload = new UserInformationRequest();
        payload.setFullName("Udin Tester");
        Date curDate = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("WIB"));
        payload.setGender("M");
        payload.setDateOfBirth(curDate);
        payload.setLanguage("Indonesia");
        payload.setPhoneNumber("+628543284939");
        ;


        HttpEntity<UserInformationRequest> entity = new HttpEntity<UserInformationRequest>(payload, headers);

        ResponseEntity<ApiResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, ApiResponse.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());


        Assertions.assertEquals("Udin Tester", response.getBody().getData("fullName"));
        Assertions.assertEquals("M", response.getBody().getData("gender"));
        Assertions.assertEquals("+628543284939", response.getBody().getData("phoneNumber"));
        Assertions.assertEquals(dateFormat.format(curDate), response.getBody().getData("dateOfBirth"));
        Assertions.assertEquals("Indonesia", response.getBody().getData("language"));
        Assertions.assertEquals(1, testH2UserInformationRepository.findAll().size());

    }

    @Test
    void testPostInvalidInformationRequestNoFullName() throws JsonProcessingException {
        headers.setBearerAuth(token);
        UserInformationRequest payload = new UserInformationRequest();
        Date curDate = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("WIB"));
        payload.setGender("M");
        payload.setDateOfBirth(curDate);
        payload.setLanguage("Indonesia");
        payload.setPhoneNumber("+628543284939");
        ;


        HttpEntity<UserInformationRequest> entity = new HttpEntity<UserInformationRequest>(payload, headers);

        try {
            restTemplate.exchange(url, HttpMethod.POST, entity, ApiResponse.class);


        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            ApiResponse response = objectMapper.readValue(responseBody, ApiResponse.class);
            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
            Assertions.assertEquals("must not be null", response.getData("fullName"));

        }
        Assertions.assertEquals(0, testH2UserInformationRepository.findAll().size());
    }

    @Test
    void testPostInvalidInformationRequestInvalidPhoneNumber() throws JsonProcessingException {
        headers.setBearerAuth(token);
        UserInformationRequest payload = new UserInformationRequest();
        payload.setFullName("Udin Tester");
        Date curDate = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("WIB"));
        payload.setGender("M");
        payload.setDateOfBirth(curDate);
        payload.setLanguage("Indonesia");
        payload.setPhoneNumber("08543284939");
        ;


        HttpEntity<UserInformationRequest> entity = new HttpEntity<UserInformationRequest>(payload, headers);

        try {
            restTemplate.exchange(url, HttpMethod.POST, entity, ApiResponse.class);


        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            ApiResponse response = objectMapper.readValue(responseBody, ApiResponse.class);
            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
            Assertions.assertEquals("Phone number must be in international format, e.g., +1234567890",
                    response.getData("phoneNumber"));

        }
        Assertions.assertEquals(0, testH2UserInformationRepository.findAll().size());
    }

    @Test
    void testPostInvalidInformationRequestInvalidGender() throws JsonProcessingException {
        headers.setBearerAuth(token);
        UserInformationRequest payload = new UserInformationRequest();
        payload.setFullName("Udin Tester");
        Date curDate = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("WIB"));
        payload.setGender("Male");
        payload.setDateOfBirth(curDate);
        payload.setLanguage("Indonesia");
        payload.setPhoneNumber("+628543284939");
        ;


        HttpEntity<UserInformationRequest> entity = new HttpEntity<UserInformationRequest>(payload, headers);

        try {
            restTemplate.exchange(url, HttpMethod.POST, entity, ApiResponse.class);


        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            ApiResponse response = objectMapper.readValue(responseBody, ApiResponse.class);
            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
            Assertions.assertEquals("Gender must be either 'M' or 'F'", response.getData("gender"));

        }
        Assertions.assertEquals(0, testH2UserInformationRepository.findAll().size());
    }

    @Test
    void testPostInvalidInformationRequestInvalidAll() throws JsonProcessingException {
        headers.setBearerAuth(token);
        UserInformationRequest payload = new UserInformationRequest();

        Date curDate = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("WIB"));
        payload.setGender("m");
        payload.setDateOfBirth(curDate);
        payload.setLanguage("Indonesia");
        payload.setPhoneNumber("08543284939");
        ;


        HttpEntity<UserInformationRequest> entity = new HttpEntity<UserInformationRequest>(payload, headers);

        try {
            restTemplate.exchange(url, HttpMethod.POST, entity, ApiResponse.class);
        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            ApiResponse response = objectMapper.readValue(responseBody, ApiResponse.class);
            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
            Assertions.assertEquals("must not be null", response.getData("fullName"));
            Assertions.assertEquals("Phone number must be in international format, e.g., +1234567890",
                    response.getData("phoneNumber"));
            Assertions.assertEquals("Gender must be either 'M' or 'F'", response.getData("gender"));

        }
        Assertions.assertEquals(0, testH2UserInformationRepository.findAll().size());
    }

    // put test
    @Test
    void putUserInformationErrorNotFound() throws JsonProcessingException {
        //test sebelum post dia mestinya return 0 Not Found
        headers.setBearerAuth(token);
        UserInformationRequest payload = new UserInformationRequest();
        Date curDate = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("WIB"));

        payload.setFullName("udin hunter 2");
        payload.setGender("M");
        payload.setDateOfBirth(curDate);
        payload.setLanguage("Indonesia");
        payload.setPhoneNumber("+628543284939");
        ;

        HttpEntity<UserInformationRequest> entity = new HttpEntity<UserInformationRequest>(payload, headers);
        try {
            restTemplate.exchange(url, HttpMethod.PUT, entity, ApiResponse.class);
        } catch (HttpClientErrorException e) {
            ApiResponse response = objectMapper.readValue(e.getResponseBodyAsString(), ApiResponse.class);

            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
            Assertions.assertEquals("User Information Does not Exist", response.getMessage());
        }
        Assertions.assertEquals(0, testH2UserInformationRepository.findAll().size());


    }

    private ResponseEntity<ApiResponse> postDefaultUser() {

        Date curDate = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("WIB"));

        headers.setBearerAuth(token);
        UserInformationRequest payload = new UserInformationRequest();
        payload.setFullName("Udin Tester");

        payload.setGender("M");
        payload.setDateOfBirth(curDate);
        payload.setLanguage("Indonesia");
        payload.setPhoneNumber("+628543284939");


        HttpEntity<UserInformationRequest> entity = new HttpEntity<UserInformationRequest>(payload, headers);

        ResponseEntity<ApiResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, ApiResponse.class);
        return response;
    }

    @Test
    void putUserSuccessfully() {
        postDefaultUser();

        headers.setBearerAuth(token);
        UserInformationRequest payload = new UserInformationRequest();

        //the data
        payload.setFullName("Udin Tester updated");
        payload.setGender("F");
        Date curDate = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("WIB"));
        payload.setDateOfBirth(curDate);
        payload.setLanguage("Singapore");
        payload.setPhoneNumber("+61854312121");

        //put the user and get the response
        HttpEntity<UserInformationRequest> entity = new HttpEntity<UserInformationRequest>(payload, headers);

        ResponseEntity<ApiResponse> updatedResponse = restTemplate.exchange(url,
                HttpMethod.PUT,
                entity,
                ApiResponse.class);

        Assertions.assertEquals(HttpStatus.OK, updatedResponse.getStatusCode());

        Assertions.assertEquals("User updated successfully", updatedResponse.getBody().getMessage());
        Assertions.assertEquals("Udin Tester updated", updatedResponse.getBody().getData("fullName"));
        Assertions.assertEquals("F", updatedResponse.getBody().getData("gender"));
        Assertions.assertEquals("+61854312121", updatedResponse.getBody().getData("phoneNumber"));
        Assertions.assertEquals(dateFormat.format(curDate), updatedResponse.getBody().getData("dateOfBirth"));
        Assertions.assertEquals("Singapore", updatedResponse.getBody().getData("language"));
        Assertions.assertEquals(1, testH2UserInformationRepository.findAll().size());

    }

    @Test
    void putUserInvalidFullName() throws JsonProcessingException {
        postDefaultUser();
        headers.setBearerAuth(token);
        UserInformationRequest payload = new UserInformationRequest();
        Date curDate = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("WIB"));
        payload.setGender("M");
        payload.setDateOfBirth(curDate);
        payload.setLanguage("Indonesia");
        payload.setPhoneNumber("+628543284939");
        ;


        HttpEntity<UserInformationRequest> entity = new HttpEntity<UserInformationRequest>(payload, headers);

        try {
            restTemplate.exchange(url, HttpMethod.PUT, entity, ApiResponse.class);


        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            ApiResponse response = objectMapper.readValue(responseBody, ApiResponse.class);
            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
            Assertions.assertEquals("must not be null", response.getData("fullName"));

        }

        Assertions.assertEquals(1, testH2UserInformationRepository.findAll().size());
    }

    @Test
    void putUserInvalidGender() throws JsonProcessingException {
        postDefaultUser();
        headers.setBearerAuth(token);
        UserInformationRequest payload = new UserInformationRequest();
        payload.setFullName("Udin Tester updated");
        Date curDate = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("WIB"));
        payload.setGender("emale");
        payload.setDateOfBirth(curDate);
        payload.setLanguage("Malaysia");
        payload.setPhoneNumber("+62854328491");
        ;


        HttpEntity<UserInformationRequest> entity = new HttpEntity<UserInformationRequest>(payload, headers);

        try {
            restTemplate.exchange(url, HttpMethod.PUT, entity, ApiResponse.class);


        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            ApiResponse response = objectMapper.readValue(responseBody, ApiResponse.class);
            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
            Assertions.assertEquals("Gender must be either 'M' or 'F'", response.getData("gender"));

        }
        Assertions.assertEquals(1, testH2UserInformationRepository.findAll().size());

    }


    @Test
    void putUserInvalidPhoneNumber() throws JsonProcessingException {
        postDefaultUser();
        headers.setBearerAuth(token);
        UserInformationRequest payload = new UserInformationRequest();
        payload.setFullName("Udin Tester");
        Date curDate = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("WIB"));
        payload.setGender("M");
        payload.setDateOfBirth(curDate);
        payload.setLanguage("Indonesia");
        payload.setPhoneNumber("08543284939");
        ;


        HttpEntity<UserInformationRequest> entity = new HttpEntity<UserInformationRequest>(payload, headers);

        try {
            restTemplate.exchange(url, HttpMethod.PUT, entity, ApiResponse.class);


        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            ApiResponse response = objectMapper.readValue(responseBody, ApiResponse.class);
            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
            Assertions.assertEquals("Phone number must be in international format, e.g., +1234567890",
                    response.getData("phoneNumber"));

        }
        Assertions.assertEquals(1, testH2UserInformationRepository.findAll().size());
    }

    @Test
    void putUserInvalidAll() throws JsonProcessingException {

        postDefaultUser();
        headers.setBearerAuth(token);
        UserInformationRequest payload = new UserInformationRequest();
        Date curDate = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("WIB"));
        payload.setGender("Male");
        payload.setDateOfBirth(curDate);
        payload.setLanguage("Indonesiaas");
        payload.setPhoneNumber("0543284939");
        ;


        HttpEntity<UserInformationRequest> entity = new HttpEntity<UserInformationRequest>(payload, headers);

        try {
            restTemplate.exchange(url, HttpMethod.PUT, entity, ApiResponse.class);


        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            ApiResponse response = objectMapper.readValue(responseBody, ApiResponse.class);
            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
            Assertions.assertEquals("must not be null", response.getData("fullName"));
            Assertions.assertEquals("Phone number must be in international format, e.g., +1234567890",
                    response.getData("phoneNumber"));
            Assertions.assertEquals("Gender must be either 'M' or 'F'", response.getData("gender"));

        }
        Assertions.assertEquals(1, testH2UserInformationRepository.findAll().size());
    }

    @Test
    void putUserSuccessFullOnlyFullNameReturnFullNameOnly() {
        ResponseEntity<ApiResponse> defaultResponse = postDefaultUser();

        headers.setBearerAuth(token);
        UserInformationRequest payload = new UserInformationRequest();

        //the data
        payload.setFullName("Udin Tester updated");

        //put the user and get the response
        HttpEntity<UserInformationRequest> entity = new HttpEntity<UserInformationRequest>(payload, headers);

        ResponseEntity<ApiResponse> updatedResponse = restTemplate.exchange(url,
                HttpMethod.PUT,
                entity,
                ApiResponse.class);

        Assertions.assertEquals(HttpStatus.OK, updatedResponse.getStatusCode());

        Assertions.assertNotEquals(defaultResponse.getBody(), updatedResponse.getBody());

        Assertions.assertEquals("User updated successfully", updatedResponse.getBody().getMessage());
        Assertions.assertEquals("Udin Tester updated", updatedResponse.getBody().getData("fullName"));
        Assertions.assertEquals(null, updatedResponse.getBody().getData("gender"));
        Assertions.assertEquals(null, updatedResponse.getBody().getData("phoneNumber"));
        Assertions.assertEquals(null, updatedResponse.getBody().getData("dateOfBirth"));
        Assertions.assertEquals(null, updatedResponse.getBody().getData("language"));
        Assertions.assertEquals(1, testH2UserInformationRepository.findAll().size());
    }

    // test patch user
    @Test
    void patchUserSuccessfully() {
        postDefaultUser();


        UserInformationRequest payload = new UserInformationRequest();

        //the data
        payload.setFullName("Udin Tester updated");
        payload.setGender("F");
        Date curDate = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("WIB"));
        payload.setDateOfBirth(curDate);
        payload.setPhoneNumber("+61854312121");

        //put the user and get the response
        webTestClient.patch().header(HttpHeaders.AUTHORIZATION,
                "Bearer " + token).contentType(MediaType.APPLICATION_JSON).body(Mono.just(payload),
                UserInformationRequest.class).exchange().expectStatus().isOk().expectBody(ApiResponse.class).consumeWith(
                response -> {
                    ApiResponse updatedResponse = response.getResponseBody();
                    Assertions.assertEquals("User updated successfully", updatedResponse.getMessage());
                    Assertions.assertEquals("Udin Tester updated", updatedResponse.getData("fullName"));
                    Assertions.assertEquals("F", updatedResponse.getData("gender"));
                    Assertions.assertEquals("+61854312121", updatedResponse.getData("phoneNumber"));
                    Assertions.assertEquals(dateFormat.format(curDate), updatedResponse.getData("dateOfBirth"));
                    Assertions.assertEquals("Indonesia", updatedResponse.getData("language"));
                    Assertions.assertEquals(1, testH2UserInformationRepository.findAll().size());
                });


    }

    @Test
    void patchUserInvalidFullName() throws JsonProcessingException {
        postDefaultUser();
        UserInformationRequest payload = new UserInformationRequest();
        Date curDate = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("WIB"));
        payload.setGender("M");
        payload.setDateOfBirth(curDate);
        payload.setLanguage("Indonesia");
        payload.setPhoneNumber("+628543284939");
        ;

        webTestClient.patch().header(HttpHeaders.AUTHORIZATION,
                "Bearer " + token).contentType(MediaType.APPLICATION_JSON).body(Mono.just(payload),
                UserInformationRequest.class).exchange().expectStatus().is4xxClientError().expectBody(ApiResponse.class).consumeWith(
                response -> {
                    ApiResponse updatedResponse = response.getResponseBody();
                    Assertions.assertEquals("the request is not valid", updatedResponse.getMessage());
                    Assertions.assertEquals("must not be null", updatedResponse.getData("fullName"));
                    Assertions.assertEquals(1, testH2UserInformationRepository.findAll().size());
                });

    }

    @Test
    void patchUserInvalidGender() throws JsonProcessingException {
        postDefaultUser();
        headers.setBearerAuth(token);
        UserInformationRequest payload = new UserInformationRequest();
        payload.setFullName("Udin Tester updated");
        Date curDate = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("WIB"));
        payload.setGender("emale");
        payload.setDateOfBirth(curDate);
        payload.setLanguage("Malaysia");
        payload.setPhoneNumber("+62854328491");
        ;


        webTestClient.patch().header(HttpHeaders.AUTHORIZATION,
                "Bearer " + token).contentType(MediaType.APPLICATION_JSON).body(Mono.just(payload),
                UserInformationRequest.class).exchange().expectStatus().is4xxClientError().expectBody(ApiResponse.class).consumeWith(
                response -> {
                    ApiResponse updatedResponse = response.getResponseBody();
                    Assertions.assertEquals("the request is not valid", updatedResponse.getMessage());
                    Assertions.assertEquals("Gender must be either 'M' or 'F'", updatedResponse.getData("gender"));
                    Assertions.assertEquals(1, testH2UserInformationRepository.findAll().size());
                });


    }


    @Test
    void patchUserInvalidPhoneNumber() throws JsonProcessingException {
        postDefaultUser();
        headers.setBearerAuth(token);
        UserInformationRequest payload = new UserInformationRequest();
        payload.setFullName("Udin Tester");
        Date curDate = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("WIB"));
        payload.setGender("M");
        payload.setDateOfBirth(curDate);
        payload.setLanguage("Indonesia");
        payload.setPhoneNumber("08543284939");
        ;


        webTestClient.patch().header(HttpHeaders.AUTHORIZATION,
                "Bearer " + token).contentType(MediaType.APPLICATION_JSON).body(Mono.just(payload),
                UserInformationRequest.class).exchange().expectStatus().is4xxClientError().expectBody(ApiResponse.class).consumeWith(
                response -> {
                    ApiResponse updatedResponse = response.getResponseBody();
                    Assertions.assertEquals("the request is not valid", updatedResponse.getMessage());
                    Assertions.assertEquals("Phone number must be in international format, e.g., +1234567890",
                            updatedResponse.getData("phoneNumber"));

                });
        Assertions.assertEquals(1, testH2UserInformationRepository.findAll().size());
    }

    @Test
    void patchUserInvalidAll() throws JsonProcessingException {
        postDefaultUser();
        headers.setBearerAuth(token);
        UserInformationRequest payload = new UserInformationRequest();
        Date curDate = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("WIB"));
        payload.setGender("Male");
        payload.setDateOfBirth(curDate);
        payload.setLanguage("Indonesiaas");
        payload.setPhoneNumber("0543284939");
        ;


        HttpEntity<UserInformationRequest> entity = new HttpEntity<UserInformationRequest>(payload, headers);

        webTestClient.patch().header(HttpHeaders.AUTHORIZATION,
                "Bearer " + token).contentType(MediaType.APPLICATION_JSON).body(Mono.just(payload),
                UserInformationRequest.class).exchange().expectStatus().is4xxClientError().expectBody(ApiResponse.class).consumeWith(
                response -> {
                    ApiResponse updatedResponse = response.getResponseBody();
                    Assertions.assertEquals("the request is not valid", updatedResponse.getMessage());
                    Assertions.assertEquals("must not be null", updatedResponse.getData("fullName"));
                    Assertions.assertEquals("Phone number must be in international format, e.g., +1234567890",
                            updatedResponse.getData("phoneNumber"));
                    Assertions.assertEquals("Gender must be either 'M' or 'F'", updatedResponse.getData("gender"));
                    Assertions.assertEquals(1, testH2UserInformationRepository.findAll().size());
                });

    }

    @Test
    void patchUserSuccessFullOnlyFullNameReturnFullNameOnly() {
        ResponseEntity<ApiResponse> defaultResponse = postDefaultUser();

        headers.setBearerAuth(token);
        UserInformationRequest payload = new UserInformationRequest();

        //the data
        payload.setFullName("Udin Tester updated");


        // patch the user
        webTestClient.patch().header(HttpHeaders.AUTHORIZATION,
                "Bearer " + token).contentType(MediaType.APPLICATION_JSON).body(Mono.just(payload),
                UserInformationRequest.class).exchange().expectStatus().isOk().expectBody(ApiResponse.class).consumeWith(
                response -> {
                    ApiResponse updatedResponse = response.getResponseBody();
                    Assertions.assertEquals("Udin Tester updated", updatedResponse.getData("fullName"));
                    Assertions.assertEquals("M", updatedResponse.getData("gender"));
                    Assertions.assertEquals("+628543284939", updatedResponse.getData("phoneNumber"));
                    Assertions.assertNotNull(updatedResponse.getData("dateOfBirth"));
                    Assertions.assertEquals("Indonesia", updatedResponse.getData("language"));
                    Assertions.assertEquals(1, testH2UserInformationRepository.findAll().size());
                });
    }

    // delete test mapping


    @Test
    void testDeleteButNoUserInformation() {
        webTestClient.delete().header(HttpHeaders.AUTHORIZATION,
                "Bearer " + token).exchange().expectStatus().is4xxClientError().expectBody(ApiResponse.class).consumeWith(response -> {
            ApiResponse responseBody = response.getResponseBody();
            Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
            Assertions.assertEquals("User information Does not Exist", responseBody.getMessage());
            Assertions.assertEquals(0, testH2UserInformationRepository.findAll().size());
        });

    }

    @Test
    void testDeleteSuccessfully() {
        postDefaultUser();

        webTestClient.delete().header(HttpHeaders.AUTHORIZATION,
                "Bearer " + token).exchange().expectStatus().isOk().expectBody(ApiResponse.class).consumeWith(response -> {
            ApiResponse responseBody = response.getResponseBody();
            Assertions.assertEquals(HttpStatus.OK.value(), responseBody.getStatusCode());
            Assertions.assertEquals(0, testH2UserInformationRepository.count());
        });


    }
}
