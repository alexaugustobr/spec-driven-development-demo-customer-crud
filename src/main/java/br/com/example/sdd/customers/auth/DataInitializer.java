package br.com.example.sdd.customers.auth;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }

        String adminPassword = "admin123";
        String attendantPassword = "attendant123";

        User admin = new User();
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(User.Role.ADMIN);

        User attendant = new User();
        attendant.setEmail("attendant@example.com");
        attendant.setPassword(passwordEncoder.encode(attendantPassword));
        attendant.setRole(User.Role.ATTENDANT);

        userRepository.saveAll(List.of(admin, attendant));

        logger.info("=== Default users created ===");
        logger.info("Admin     - email: {} | password: {}", admin.getEmail(), adminPassword);
        logger.info("Attendant - email: {} | password: {}", attendant.getEmail(), attendantPassword);
    }
}
