package com.revelvol.JWT;


import com.revelvol.JWT.model.User;
import com.revelvol.JWT.repository.TestH2RoleRepository;
import com.revelvol.JWT.repository.TestH2UserRepository;
import com.revelvol.JWT.response.ApiResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// kalo ga mau nyalain server bisapake @WebMVCTest Build in method yang di inject ama controller nya
@AutoConfigureMockMvc
// web environment random port usefull to avoid conflict in test environment (kalo nyalain server)
public class HttpRequestTest {

    private static RestTemplate restTemplate;
    /*
    @Autowired
    private TestRestTemplate restTemplate; // auto from spring test

    @Test
    public void defaultDemoReturnDefaultMessage() throws Exception {
        Assertions.assertEquals(this.restTemplate.getForObject("http://localhost:" + port + "/api/v1/demo-controller",
                String.class), "this is a secured endpoint");
    }

     */
    @LocalServerPort
    private int port;
    private String registerUrl;
    private String authenticateUrl;
//    @MockBean
//    private PasswordEncoder passwordEncoder;


    @Autowired
    private TestH2UserRepository testH2UserRepository;
    @Autowired
    private TestH2RoleRepository testH2RoleRepository;
    @Autowired
    private MockMvc mockMvc;


    @BeforeAll
    public static void init() {
        restTemplate = new RestTemplate();
    }

    @BeforeEach
    public void setUp() {
        String baseUrl = "http://localhost";
        registerUrl = baseUrl.concat(":").concat(String.valueOf(port)).concat("/api/v1/auth/register");
        authenticateUrl = baseUrl.concat(":").concat(String.valueOf(port)).concat("/api/v1/auth/authenticate");
        MockitoAnnotations.openMocks(this);


    }


    @Test
    public void shouldReturnDefaultMessage() throws Exception {
        System.out.println(this.registerUrl);
        System.out.println(this.authenticateUrl);
        this.mockMvc.perform(get("/")).andDo(print()).andExpect(status().isOk()).andExpect(content().string(
                containsString("ok"))); // this is ok return does not work
    }

    @Test
    public void testAddUser() {


        User user = new User("test123", "12345457647", new HashSet<>());
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


}
