package com.taskmanager.controller;

import com.taskmanager.model.ERole;
import com.taskmanager.model.Role;
import com.taskmanager.model.User;
import com.taskmanager.payload.request.LoginRequest;
import com.taskmanager.payload.request.SignupRequest;
import com.taskmanager.payload.response.JwtResponse;
import com.taskmanager.payload.response.MessageResponse;
import com.taskmanager.repository.RoleRepository;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.security.jwt.JwtUtils;
import com.taskmanager.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final String ERR_USERNAME_TAKEN = "Error: Username is already taken!";
    private static final String ERR_EMAIL_IN_USE = "Error: Email is already in use!";
    private static final String ERR_ROLE_NOT_FOUND = "Error: Role is not found.";

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder encoder,
                          JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
    }

    // ... existing code ...
    
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            return getResponseEntity(authentication);
        } catch (org.springframework.security.authentication.BadCredentialsException |
                 org.springframework.security.core.userdetails.UsernameNotFoundException ex) {
            return ResponseEntity.status(401).body(new MessageResponse("Invalid username or password"));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(new MessageResponse("Authentication failed"));
        }
    }

    
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse(ERR_USERNAME_TAKEN));
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse(ERR_EMAIL_IN_USE));
        }

        User user = new User(
                signupRequest.getUsername(),
                signupRequest.getEmail(),
                encoder.encode(signupRequest.getPassword())
        );

        Set<Role> roles = resolveRoles(signupRequest.getRole());
        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    private Set<Role> resolveRoles(Set<String> roleNames) {
        // Domyślnie ROLE_USER, jeśli brak lub pusty zbiór
        if (roleNames == null || roleNames.isEmpty()) {
            return Set.of(findRole(ERole.ROLE_USER));
        }

        Set<Role> resolved = new HashSet<>();
        for (String role : roleNames) {
            switch (role.toLowerCase(Locale.ROOT)) {
                case "admin" -> resolved.add(findRole(ERole.ROLE_ADMIN));
                case "mod", "moderator" -> resolved.add(findRole(ERole.ROLE_MODERATOR));
                default -> resolved.add(findRole(ERole.ROLE_USER));
            }
        }
        return resolved;
    }

    private Role findRole(ERole eRole) {
        return roleRepository.findByName(eRole)
                .orElseThrow(() -> new RuntimeException(ERR_ROLE_NOT_FOUND));
    }

    @GetMapping("/token")
    public ResponseEntity<?> generateToken(Authentication authentication) {
        // Only allow for authenticated users; otherwise 401
        if (authentication == null || !authentication.isAuthenticated() ||
                (authentication.getPrincipal() instanceof String &&
                        "anonymousUser".equals(authentication.getPrincipal()))) {
            return ResponseEntity.status(401).body(new MessageResponse("Unauthorized"));
        }

        return getResponseEntity(authentication);
    }

    private ResponseEntity<?> getResponseEntity(Authentication authentication) {
        String jwt = jwtUtils.generateJwtToken(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

}