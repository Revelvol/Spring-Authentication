package com.revelvol.JWT;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest() // kalo ga mau nyalain server bisapake @WebMVCTest Build in method yang di inject ama controller nya
@AutoConfigureMockMvc
// web environment random port usefull to avoid conflict in test environment (kalo nyalain server)
public class HttpRequestTest {

    /*
    @Value(value = "${local.server.port}")
    private int port;


    @Autowired
    private TestRestTemplate restTemplate; // auto from spring test

    @Test
    public void defaultDemoReturnDefaultMessage() throws Exception {
        Assertions.assertEquals(this.restTemplate.getForObject("http://localhost:" + port + "/api/v1/demo-controller",
                String.class), "this is a secured endpoint");
    }

     */

    @Autowired
    private MockMvc mockMvc;

    @Test void shouldReturnDefaultMessage()throws Exception {
        this.mockMvc.perform(get("/")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("ok"))); // this is ok does not work
    }

}
