package com.taskmanager.security.jwt;

import com.taskmanager.controller.AuthController;
import com.taskmanager.payload.request.SignupRequest;
import com.taskmanager.security.services.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private UserDetailsImpl userDetails;
    private Authentication authentication;
    @Autowired
    private AuthController controller;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        // Use a Base64-encoded secret because JwtUtils expects Base64 and decodes it before creating the key.
        String base64Secret = "QUJDREVGR0hJSktMTU5PUFFSU1RVVldYWVo5MDg3NjU0MzIxYWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXo5MDg3NjU0MzIx"; // Base64 of 64-byte secret
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", base64Secret);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 60000); // 1 minute

        // Initialize internal signingKey and parser
        jwtUtils.init();

        // Create user details
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        userDetails = new UserDetailsImpl(1L, "sa", "test@example.com", "password", authorities);

        // Create authentication
        authentication = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }

    @Test
    void generateJwtToken_ShouldReturnValidToken() {
        // Act
        String token = jwtUtils.generateJwtToken(authentication);
        
        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void validateJwtToken_WithValidToken_ShouldReturnTrue() {
        // Arrange
        String token = jwtUtils.generateJwtToken(authentication);
        
        // Act
        boolean isValid = jwtUtils.validateJwtToken(token);
        
        // Assert
        assertTrue(isValid);
    }

    @Test
    void validateJwtToken_WithInvalidToken_ShouldReturnFalse() {
        // Act
        boolean isValid = jwtUtils.validateJwtToken("invalidToken");
        
        // Assert
        assertFalse(isValid);
    }

    @Test
    void getUserNameFromJwtToken_ShouldReturnCorrectUsername() {
        // Arrange
        String token = jwtUtils.generateJwtToken(authentication);
        
        // Act
        String username = jwtUtils.getUserNameFromJwtToken(token);
        
        // Assert
        assertEquals("sa", username);
    }

    @Test
    void login() {
        // Arrange
        String token = jwtUtils.generateJwtToken(authentication);

        // Act
        String username = jwtUtils.getUserNameFromJwtToken(token);
        HashSet<String> roles = new HashSet<>();
        roles.add("ROLE_USER");
        SignupRequest signupRequest = new SignupRequest(username, userDetails.getEmail(), new HashSet<>(userDetails.getAuthorities().size()),"password");

        ResponseEntity<?> response = controller.registerUser(signupRequest);
        // Assert
        assertEquals("sa", username);
        assertEquals(200, response.getStatusCode().value());
    }

    public AuthController getController() {
        return controller;
    }

    public void setController(AuthController controller) {
        this.controller = controller;
    }
}