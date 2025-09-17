package lt.nsg.scroogebank.auth;

import jakarta.persistence.EntityManager;
import lt.nsg.scroogebank.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthenticationService {
    private final PasswordEncoder encoder;
    private final EntityManager entityManager;
    private final String hashForMissingUser;

    public AuthenticationService(PasswordEncoder encoder, EntityManager entityManager) {
        this.encoder = encoder;
        this.entityManager = entityManager;
        this.hashForMissingUser = encoder.encode(UUID.randomUUID().toString());
    }

    public ApiKeyAuthentication loadUser(PreAuthenticatedAuthenticationToken token) {
        var user = this.entityManager.find(User.class, token.getPrincipal());
        String hashedCredential = hashForMissingUser;
        if (user != null) {
            hashedCredential = user.getApiKeyHash();
        }

        var result = encoder.matches(token.getCredentials().toString(), hashedCredential);
        if (result && user != null) {
            return new ApiKeyAuthentication(user);
        } else {
            return null;
        }
    }
}
