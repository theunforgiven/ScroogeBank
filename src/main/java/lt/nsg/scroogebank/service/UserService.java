package lt.nsg.scroogebank.service;

import jakarta.persistence.EntityManager;
import lt.nsg.scroogebank.dto.CreateUserResponse;
import lt.nsg.scroogebank.entity.User;
import lt.nsg.scroogebank.entity.UserRoles;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {
    private final EntityManager entityManager;
    private final PasswordEncoder passwordEncoder;

    public UserService(EntityManager entityManager, PasswordEncoder passwordEncoder) {
        this.entityManager = entityManager;
        this.passwordEncoder = passwordEncoder;
    }

    public CreateUserResponse createUser(UserRoles userRoles) {
        UUID rawPassword = UUID.randomUUID();
        var user = new User(UUID.randomUUID(), this.passwordEncoder.encode(rawPassword.toString()), userRoles);
        entityManager.persist(user);
        return new CreateUserResponse(user.getId(), rawPassword);
    }
}
