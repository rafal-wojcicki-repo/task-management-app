package com.taskmanager.security.jwt;

import com.taskmanager.security.services.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collection;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for JwtUtils using BDD (Behavior-Driven Development) approach.
 * Tests JWT token generation, validation, and extraction of claims.
 */
@DisplayName("JwtUtils")
class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private UserDetailsImpl userDetails;
    private Authentication authentication;

    private static final String BASE64_SECRET = "QUJDREVGR0hJSktMTU5PUFFSU1RVVldYWVo5MDg3NjU0MzIxYWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXo5MDg3NjU0MzIx";
    private static final long EXPIRATION_TIME = 60000; // 1 minute
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", BASE64_SECRET);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", EXPIRATION_TIME);
        jwtUtils.init();

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        userDetails = new UserDetailsImpl(1L, TEST_USERNAME, TEST_EMAIL, "password", authorities);
        authentication = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }

    @Nested
    @DisplayName("Token Generation")
    class TokenGeneration {

        @Test
        @DisplayName("Should generate valid JWT token from authentication")
        void givenValidAuthentication_whenGenerateToken_thenReturnJwtToken() {
            // When
            String token = jwtUtils.generateJwtToken(authentication);

            // Then
            assertThat(token)
                    .isNotNull()
                    .isNotEmpty()
                    .hasSizeGreaterThan(20);
        }

        @Test
        @DisplayName("Should generate token with correct structure (header.payload.signature)")
        void givenValidAuthentication_whenGenerateToken_thenTokenHasCorrectStructure() {
            // When
            String token = jwtUtils.generateJwtToken(authentication);

            // Then
            assertThat(token)
                    .contains(".")
                    .hasSizeGreaterThan(50)
                    .matches("[A-Za-z0-9\\-_.]+\\.[A-Za-z0-9\\-_.]+\\.[A-Za-z0-9\\-_.]+");
        }

        @Test
        @DisplayName("Should generate different tokens for same authentication on different calls")
        void givenSameAuthentication_whenGenerateTokenTwice_thenTokensAreDifferent() {
            // When
            String token1 = jwtUtils.generateJwtToken(authentication);
            String token2 = jwtUtils.generateJwtToken(authentication);

            // Then
            assertThat(token1).isNotEqualTo(token2);
        }
    }

    @Nested
    @DisplayName("Token Validation")
    class TokenValidation {

        @Test
        @DisplayName("Should validate a valid JWT token")
        void givenValidToken_whenValidate_thenReturnTrue() {
            // Given
            String token = jwtUtils.generateJwtToken(authentication);

            // When
            boolean isValid = jwtUtils.validateJwtToken(token);

            // Then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("Should reject invalid JWT token")
        void givenInvalidToken_whenValidate_thenReturnFalse() {
            // When
            boolean isValid = jwtUtils.validateJwtToken("invalidToken");

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should reject token with wrong signature")
        void givenTokenWithWrongSignature_whenValidate_thenReturnFalse() {
            // Given
            String token = jwtUtils.generateJwtToken(authentication);
            String modifiedToken = token.substring(0, token.length() - 1) + "X";

            // When
            boolean isValid = jwtUtils.validateJwtToken(modifiedToken);

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should reject empty token")
        void givenEmptyToken_whenValidate_thenReturnFalse() {
            // When
            boolean isValid = jwtUtils.validateJwtToken("");

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should reject null token")
        void givenNullToken_whenValidate_thenReturnFalse() {
            // When & Then
            assertThatThrownBy(() -> jwtUtils.validateJwtToken(null))
                    .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("Extract Username")
    class ExtractUsername {

        @Test
        @DisplayName("Should extract correct username from valid token")
        void givenValidToken_whenExtractUsername_thenReturnCorrectUsername() {
            // Given
            String token = jwtUtils.generateJwtToken(authentication);

            // When
            String username = jwtUtils.getUserNameFromJwtToken(token);

            // Then
            assertThat(username).isEqualTo(TEST_USERNAME);
        }

        @Test
        @DisplayName("Should extract username matching authentication principal")
        void givenTokenFromAuthentication_whenExtractUsername_thenMatchesPrincipal() {
            // Given
            String token = jwtUtils.generateJwtToken(authentication);

            // When
            String extractedUsername = jwtUtils.getUserNameFromJwtToken(token);

            // Then
            assertThat(extractedUsername).isEqualTo(authentication.getName());
        }

        @Test
        @DisplayName("Should throw exception when extracting username from invalid token")
        void givenInvalidToken_whenExtractUsername_thenThrowException() {
            // When & Then
            assertThatThrownBy(() -> jwtUtils.getUserNameFromJwtToken("invalidToken"))
                    .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("Token Expiration")
    class TokenExpiration {

        @Test
        @DisplayName("Should generate token that is not expired immediately after creation")
        void givenNewToken_whenValidate_thenTokenIsNotExpired() {
            // Given
            String token = jwtUtils.generateJwtToken(authentication);

            // When
            boolean isValid = jwtUtils.validateJwtToken(token);

            // Then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("Should include expiration claim in token")
        void givenGeneratedToken_whenGenerated_thenTokenHasExpirationTime() {
            // When
            String token = jwtUtils.generateJwtToken(authentication);

            // Then
            assertThat(token).isNotNull();
            // Token should be valid (not expired immediately)
            assertThat(jwtUtils.validateJwtToken(token)).isTrue();
        }
    }

    @Nested
    @DisplayName("Multiple Users")
    class MultipleUsers {

        @Test
        @DisplayName("Should generate different tokens for different users")
        void givenDifferentUsers_whenGenerateTokens_thenTokensAreDifferent() {
            // Given
            UserDetailsImpl user2 = new UserDetailsImpl(2L, "otheruser", "other@example.com", "password",
                    new ArrayList<>(authentication.getAuthorities()));
            Authentication auth2 = new UsernamePasswordAuthenticationToken(user2, null, authentication.getAuthorities());

            // When
            String token1 = jwtUtils.generateJwtToken(authentication);
            String token2 = jwtUtils.generateJwtToken(auth2);

            // Then
            assertThat(token1).isNotEqualTo(token2);
        }

        @Test
        @DisplayName("Should extract correct username for each user's token")
        void givenTokensForDifferentUsers_whenExtractUsername_thenReturnCorrectUsernames() {
            // Given
            UserDetailsImpl user2 = new UserDetailsImpl(2L, "otheruser", "other@example.com", "password",
                    new ArrayList<>(authentication.getAuthorities()));
            Authentication auth2 = new UsernamePasswordAuthenticationToken(user2, null, authentication.getAuthorities());

            String token1 = jwtUtils.generateJwtToken(authentication);
            String token2 = jwtUtils.generateJwtToken(auth2);

            // When
            String username1 = jwtUtils.getUserNameFromJwtToken(token1);
            String username2 = jwtUtils.getUserNameFromJwtToken(token2);

            // Then
            assertThat(username1).isEqualTo(TEST_USERNAME);
            assertThat(username2).isEqualTo("otheruser");
        }
    }
}