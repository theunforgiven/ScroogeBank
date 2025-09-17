package lt.nsg.scroogebank;

import jakarta.persistence.EntityManager;
import lt.nsg.scroogebank.config.UtilityConfig;
import lt.nsg.scroogebank.entity.User;
import lt.nsg.scroogebank.entity.UserRoles;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({UtilityConfig.class})
@ActiveProfiles("test")
public class BaseDataTest {
    protected final UUID rawPassword = UUID.randomUUID();
    @Autowired
    protected PasswordEncoder passwordEncoder;
    @Autowired
    protected EntityManager entityManager;

    protected User testUser;

    @BeforeEach
    public void setUp() {
        // Initialize test data before each test method
        this.testUser = createUser();
    }

    protected User createUser() {
        var user = new User(UUID.randomUUID(), passwordEncoder.encode(rawPassword.toString()), UserRoles.user);
        entityManager.persist(user);
        return user;
    }
}