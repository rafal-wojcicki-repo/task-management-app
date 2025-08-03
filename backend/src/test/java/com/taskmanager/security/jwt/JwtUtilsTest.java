package com.taskmanager.security.jwt;

import com.taskmanager.security.services.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private UserDetailsImpl userDetails;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", "testSecretKeyWithAtLeast32CharactersForHS512Algorithm");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 60000); // 1 minute

        // Create user details
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        userDetails = new UserDetailsImpl(1L, "testuser", "test@example.com", "password", authorities);
        
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
        assertEquals("testuser", username);
    }
}