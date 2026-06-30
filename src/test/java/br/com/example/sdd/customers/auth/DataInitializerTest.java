package br.com.example.sdd.customers.auth;

import br.com.example.sdd.customers.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class DataInitializerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DataInitializer dataInitializer;

    @Test
    void createsAdminAndAttendantOnEmptyDatabase() {
        List<User> users = userRepository.findAll();

        assertThat(users).hasSize(2);
        assertThat(users).anyMatch(u -> u.getRole() == User.Role.ADMIN);
        assertThat(users).anyMatch(u -> u.getRole() == User.Role.ATTENDANT);
        assertThat(users).allMatch(u -> u.getPassword().startsWith("$2a$"));
    }

    @Test
    void doesNotCreateDuplicatesWhenUsersAlreadyExist() {
        long countBefore = userRepository.count();

        assertThatNoException().isThrownBy(() -> dataInitializer.run(null));

        long countAfter = userRepository.count();
        assertThat(countAfter).isEqualTo(countBefore);
    }
}
