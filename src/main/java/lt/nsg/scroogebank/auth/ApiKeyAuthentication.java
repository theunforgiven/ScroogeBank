package lt.nsg.scroogebank.auth;

import lt.nsg.scroogebank.entity.User;
import org.springframework.security.authentication.AbstractAuthenticationToken;

public class ApiKeyAuthentication extends AbstractAuthenticationToken {
    private final User user;

    public ApiKeyAuthentication(User user) {
        super(user.getAuthorities());
        this.user = user;
        this.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return this.user.getApiKeyHash();
    }

    @Override
    public Object getPrincipal() {
        return this.user;
    }
}