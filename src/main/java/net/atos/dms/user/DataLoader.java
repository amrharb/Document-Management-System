package net.atos.dms.user;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner seed(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String testEmail = "test@local";
            if (!userRepository.existsByEmail(testEmail)) {
                User u = new User();
                u.setEmail(testEmail);
                u.setPassword(passwordEncoder.encode("password123"));
                u.setNid("TESTNID001");
                userRepository.save(u);
                System.out.println("Seeded test user: " + testEmail + " / password123");
            } else {
                System.out.println("Test user already exists.");
            }
        };
    }
}
