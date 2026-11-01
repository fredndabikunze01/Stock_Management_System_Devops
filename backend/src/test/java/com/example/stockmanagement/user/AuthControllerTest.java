package com.example.stockmanagement.user;

import com.example.stockmanagement.StockManagementApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = StockManagementApplication.class)
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository userRepository;

    @BeforeEach
    void clearUsers() {
        userRepository.deleteAll();
    }

    @Test
    void userCanRegisterLoginViewDashboardAndLogout() throws Exception {
        MvcResult registration = mockMvc.perform(post("/api/auth/register")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"student","email":"student@example.com","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("student"))
                .andReturn();

        mockMvc.perform(get("/api/auth/me").session((org.springframework.mock.web.MockHttpSession)
                        registration.getRequest().getSession(false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("student@example.com"));

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"student@example.com","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("student@example.com"))
                .andReturn();

        mockMvc.perform(get("/api/auth/me").session((org.springframework.mock.web.MockHttpSession)
                        login.getRequest().getSession(false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("student@example.com"));

        mockMvc.perform(post("/api/auth/logout")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .session((org.springframework.mock.web.MockHttpSession)
                                login.getRequest().getSession(false)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginRejectsIncorrectPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"student","email":"student@example.com","password":"password123"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"student@example.com","password":"wrongpass"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
