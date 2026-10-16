package org.example.user;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
class UserSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        createIfMissing("admin", "admin123", Role.ADMIN);
        createIfMissing("player", "player123", Role.USER);
    }

    private void createIfMissing(String username, String password, Role role) {
        if (userRepository.existsByUsername(username)) {
            return;
        }

        userRepository.save(new AppUser(username, passwordEncoder.encode(password), role));
    }
}
