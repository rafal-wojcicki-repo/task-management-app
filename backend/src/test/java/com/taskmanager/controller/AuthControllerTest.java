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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthController using BDD (Behavior-Driven Development) approach.
 * Tests follow Given-When-Then pattern for better readability and maintainability.
 */
@DisplayName("AuthController")
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
        initializeTestData();
    }

    private void initializeTestData() {
        // Setup login request
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        // Setup signup request
        signupRequest = new SignupRequest("newuser", "newuser@example.com", new HashSet<>(Collections.singletonList("user")), "password");

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

    @Nested
    @DisplayName("Login endpoint")
    class AuthenticateUser {

        @Test
        @DisplayName("Should return JWT token when credentials are valid")
        void givenValidCredentials_whenAuthenticate_thenReturnJwtResponse() {
            // Given
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(jwtUtils.generateJwtToken(authentication)).thenReturn("testToken");

            // When
            ResponseEntity<?> response = authController.authenticateUser(loginRequest);

            // Then
            assertThat(response)
                    .isNotNull()
                    .satisfies(r -> assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK));
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(jwtUtils).generateJwtToken(authentication);
        }

        @Test
        @DisplayName("Should throw exception when authentication fails")
        void givenInvalidCredentials_whenAuthenticate_thenThrowException() {
            // Given
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new RuntimeException("Authentication failed"));

            // When & Then
            assertThatThrownBy(() -> authController.authenticateUser(loginRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Authentication failed");

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verifyNoInteractions(jwtUtils);
        }

        @Test
        @DisplayName("Should set SecurityContext after successful authentication")
        void givenValidCredentials_whenAuthenticate_thenSecurityContextIsSet() {
            // Given
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(jwtUtils.generateJwtToken(authentication)).thenReturn("testToken");

            // When
            authController.authenticateUser(loginRequest);

            // Then
            assertThat(SecurityContextHolder.getContext().getAuthentication())
                    .isEqualTo(authentication);
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }
    }

    @Nested
    @DisplayName("Registration endpoint")
    class RegisterUser {

        @Test
        @DisplayName("Should successfully register a new user with valid data")
        void givenNewUser_whenRegister_thenUserIsSaved() {
            // Given
            when(userRepository.existsByUsername(signupRequest.getUsername())).thenReturn(false);
            when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
            when(encoder.encode(signupRequest.getPassword())).thenReturn("encodedPassword");
            when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));

            // When
            ResponseEntity<?> response = authController.registerUser(signupRequest);

            // Then
            assertThat(response)
                    .isNotNull()
                    .satisfies(r -> assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK));
            verify(userRepository).save(any(User.class));
            verify(roleRepository).findByName(ERole.ROLE_USER);
        }

        @Test
        @DisplayName("Should return 400 when username already exists")
        void givenExistingUsername_whenRegister_thenReturnBadRequest() {
            // Given
            when(userRepository.existsByUsername(signupRequest.getUsername())).thenReturn(true);

            // When
            ResponseEntity<?> response = authController.registerUser(signupRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should return 400 when email already exists")
        void givenExistingEmail_whenRegister_thenReturnBadRequest() {
            // Given
            when(userRepository.existsByUsername(signupRequest.getUsername())).thenReturn(false);
            when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(true);

            // When
            ResponseEntity<?> response = authController.registerUser(signupRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should encode password before saving user")
        void givenValidSignup_whenRegister_thenPasswordIsEncoded() {
            // Given
            when(userRepository.existsByUsername(signupRequest.getUsername())).thenReturn(false);
            when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
            when(encoder.encode(signupRequest.getPassword())).thenReturn("encodedPassword");
            when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));

            // When
            authController.registerUser(signupRequest);

            // Then
            verify(encoder).encode(signupRequest.getPassword());
            verify(userRepository).save(argThat(user ->
                    user.getPassword().equals("encodedPassword")
            ));
        }

        @Test
        @DisplayName("Should assign default role (ROLE_USER) when no role specified")
        void givenNoRoleSpecified_whenRegister_thenDefaultRoleAssigned() {
            // Given
            signupRequest.setRole(null);
            when(userRepository.existsByUsername(signupRequest.getUsername())).thenReturn(false);
            when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
            when(encoder.encode(signupRequest.getPassword())).thenReturn("encodedPassword");
            when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));

            // When
            authController.registerUser(signupRequest);

            // Then
            verify(roleRepository).findByName(ERole.ROLE_USER);
            verify(userRepository).save(any(User.class));
        }
    }
}