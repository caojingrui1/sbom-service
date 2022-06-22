package org.openeuler.sbom.manager;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openeuler.sbom.manager.model.UserEntity;
import org.openeuler.sbom.manager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = SbomManagerApplication.class)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Test
    @Order(1)
    public void removeAllRecord() throws Exception {
        userService.deleteAllUsers();
    }

    @Test()
    @Order(2)
    public void findAllRecordBeforeAdd() throws Exception {
        this.mockMvc.perform(get("/sbom/allUser"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(0)));
    }

    @Test
    @Order(3)
    public void addUser() throws Exception {
        UserEntity user = new UserEntity();
        user.setUserName("First");
        user.setEmail("first@someemail.com");

        this.mockMvc.perform(post("/sbom/addUser")
                        .param("name", "First")
                        .param("email", "first@someemail.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk()).
                andExpect(content().string("Saved"));
    }

    @Test
    @Order(4)
    public void addUserRecord() throws Exception {
        UserEntity user = new UserEntity();
        user.setUserName("Second");
        user.setEmail("second@someemail.com");
        this.mockMvc.perform(post("/sbom/addUserRecord")
                        .content(new ObjectMapper().writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().string("Saved"));
    }

    @Test()
    @Order(5)
    public void findAllRecordAfterAdd() throws Exception {
        String responseJsonString = this.mockMvc.perform(get("/sbom/allUser"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(2)))
                .andExpect(jsonPath("$.[0].userName").value("First"))
                .andReturn().getResponse().getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        List<UserEntity> users = objectMapper.readValue(responseJsonString, new TypeReference<List<UserEntity>>() {
        });

        Assert.notEmpty(users, "users list is empty");
    }


    @Test()
    @Order(6)
    public void findRecordByName() {
        Assert.isTrue(!CollectionUtils.isEmpty(userService.findByUserName1("First")), "can`t find First");
        Assert.isTrue(!CollectionUtils.isEmpty(userService.findByUserName2("Second")), "can`t find Second 2");
        Assert.isTrue(!CollectionUtils.isEmpty(userService.findByUserName3("Second")), "can`t find Second 3");
    }
}

