package com.iamdk.directory.init;

import com.iamdk.directory.entity.Group;
import com.iamdk.directory.entity.User;
import com.iamdk.directory.repository.GroupRepository;
import com.iamdk.directory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Data Initializer
 * Creates default admin user and groups on startup
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByLoginName("admin").isEmpty()) {
            createDefaultAdmin();
        }

        if (groupRepository.findByName("Administrators").isEmpty()) {
            createDefaultGroups();
        }
    }

    private void createDefaultAdmin() {
        User admin = User.builder()
            .loginName("admin")
            .email("admin@iamdk.local")
            .password(passwordEncoder.encode("admin123"))
            .firstName("System")
            .lastName("Admin")
            .active(true)
            .failedLoginAttempts(0)
            .build();

        userRepository.save(admin);
        log.info("Default admin user created: admin / admin123");
    }

    private void createDefaultGroups() {
        Group admins = Group.builder()
            .name("Administrators")
            .description("System administrators with full access")
            .build();

        Group users = Group.builder()
            .name("Users")
            .description("Default user group")
            .build();

        groupRepository.save(admins);
        groupRepository.save(users);

        log.info("Default groups created: Administrators, Users");
    }
}
