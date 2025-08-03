package com.taskmanager.config;

import com.taskmanager.model.ERole;
import com.taskmanager.model.Role;
import com.taskmanager.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        initRoles();
    }

    private void initRoles() {
        // Create roles if they don't exist
        for (ERole role : ERole.values()) {
            if (!roleRepository.existsById(role.ordinal() + 1)) {
                Role newRole = new Role();
                newRole.setName(role);
                roleRepository.save(newRole);
            }
        }
    }
}