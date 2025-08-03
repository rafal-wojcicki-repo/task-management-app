package com.taskmanager.controller;

import com.taskmanager.model.ERole;
import com.taskmanager.model.Role;
import com.taskmanager.model.User;
import com.taskmanager.payload.request.LoginRequest;
import com.taskmanager.payload.request.SignupRequest;
import com.taskmanager.repository.RoleRepository;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.security.jwt.JwtUtils;
import com.taskmanager.security.services.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthController authController;

    private LoginRequest loginRequest;
    private SignupRequest signupRequest;
    private Authentication authentication;
    private UserDetailsImpl userDetails;
    private Role userRole;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup login request
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        // Setup signup request
        signupRequest = new SignupRequest();
        signupRequest.setUsername("newuser");
        signupRequest.setEmail("newuser@example.com");
        signupRequest.setPassword("password");
        Set<String> roles = new HashSet<>();
        roles.add("user");
        signupRequest.setRole(roles);

        // Setup authentication
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        userDetails = new UserDetailsImpl(1L, "testuser", "test@example.com", "password", authorities);
        authentication = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

        // Setup role
        userRole = new Role();
        userRole.setId(1);
        userRole.setName(ERole.ROLE_USER);
    }

    @Test
    void authenticateUser_ShouldReturnJwtResponse() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("testToken");

        // Act
        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils).generateJwtToken(authentication);
    }

    @Test
    void registerUser_WithNewUser_ShouldReturnSuccessMessage() {
        // Arrange
        when(userRepository.existsByUsername(signupRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
        when(encoder.encode(signupRequest.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));

        // Act
        ResponseEntity<?> response = authController.registerUser(signupRequest);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_WithExistingUsername_ShouldReturnBadRequest() {
        // Arrange
        when(userRepository.existsByUsername(signupRequest.getUsername())).thenReturn(true);

        // Act
        ResponseEntity<?> response = authController.registerUser(signupRequest);

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_WithExistingEmail_ShouldReturnBadRequest() {
        // Arrange
        when(userRepository.existsByUsername(signupRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(true);

        // Act
        ResponseEntity<?> response = authController.registerUser(signupRequest);

        // Assert
        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
        verify(userRepository, never()).save(any(User.class));
    }
}