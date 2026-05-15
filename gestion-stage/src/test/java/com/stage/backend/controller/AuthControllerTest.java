package com.stage.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stage.backend.config.WebSecurityTestConfig;
import com.stage.backend.dto.LoginRequest;
import com.stage.backend.dto.SignupRequest;
import com.stage.backend.entity.User;
import com.stage.backend.security.JwtUtils;
import com.stage.backend.security.UserDetailsServiceImpl;
import com.stage.backend.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests boîte noire de AuthController via @WebMvcTest.
 *
 * @Import(WebSecurityTestConfig.class) : charge la config de sécurité de test
 * qui autorise /api/auth/** sans authentification (comme la prod).
 * Sans cela, @WebMvcTest charge la config par défaut Spring Security
 * qui requiert une auth HTTP Basic sur tous les endpoints → 401 partout.
 */
@WebMvcTest(AuthController.class)
@Import(WebSecurityTestConfig.class)
@DisplayName("AuthController — Tests boîte noire (@WebMvcTest)")
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean UserService userService;
    @MockBean AuthenticationManager authenticationManager;
    @MockBean JwtUtils jwtUtils;
    @MockBean UserDetailsServiceImpl userDetailsService;

    // ════════════════════════════════════════════════════════════════════════
    // SIGNUP valide → 200
    // ════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("POST /signup — valide → 200 OK")
    void signup_valid_returns200() throws Exception {
        var req = validSignupRequest();
        var saved = new User();
        saved.setUsername("ali");
        when(userService.registerUser(any(), any())).thenReturn(saved);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /signup — email invalide → 400")
    void signup_invalidEmail_returns400() throws Exception {
        var req = validSignupRequest();
        req.setEmail("pas-un-email");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any(), any());
    }

    @Test
    @DisplayName("POST /signup — username trop court (<3) → 400")
    void signup_usernameTooShort_returns400() throws Exception {
        var req = validSignupRequest();
        req.setUsername("ab");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /signup — password trop court (<6) → 400")
    void signup_passwordTooShort_returns400() throws Exception {
        var req = validSignupRequest();
        req.setPassword("abc");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /signup — username manquant → 400")
    void signup_missingUsername_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@test.com\",\"password\":\"secret123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /signup — JSON malformé → 400")
    void signup_malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"username\" : }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /signup — corps vide → 400")
    void signup_emptyBody_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /signup — username déjà pris → 400")
    void signup_duplicateUsername_returns400() throws Exception {
        var req = validSignupRequest();
        when(userService.registerUser(any(), any()))
                .thenThrow(new ResponseStatusException(BAD_REQUEST,
                        "Ce nom d'utilisateur est déjà pris."));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ════════════════════════════════════════════════════════════════════════
    // SIGNIN valide → 200
    // ════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("POST /signin — valide → 200 OK + accessToken")
    void signin_valid_returns200WithToken() throws Exception {
        var loginReq = new LoginRequest();
        loginReq.setUsername("ali");
        loginReq.setPassword("secret123");

        UserDetails ud = org.springframework.security.core.userdetails.User
                .withUsername("ali")
                .password("encoded")
                .authorities(new SimpleGrantedAuthority("ROLE_ETUDIANT"))
                .build();

        var auth = new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtUtils.generateJwtToken(any())).thenReturn("mocked.jwt.token");

        var user = new User();
        user.setId(1L);
        user.setUsername("ali");
        user.setEmail("ali@test.com");
        when(userService.findByUsername("ali")).thenReturn(user);

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mocked.jwt.token"))
                .andExpect(jsonPath("$.username").value("ali"))
                .andExpect(jsonPath("$.type").value("Bearer"));
    }

    @Test
    @DisplayName("POST /signin — mauvais mot de passe → 401")
    void signin_badCredentials_returns401() throws Exception {
        var loginReq = new LoginRequest();
        loginReq.setUsername("ali");
        loginReq.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /signin — username manquant → 400")
    void signin_missingUsername_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"secret123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /signin — password manquant → 400")
    void signin_missingPassword_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ali\"}"))
                .andExpect(status().isBadRequest());
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private SignupRequest validSignupRequest() {
        var req = new SignupRequest();
        req.setUsername("alibaba");
        req.setEmail("ali@test.com");
        req.setPassword("secret123");
        req.setRoles(Set.of("etudiant"));
        return req;
    }
}