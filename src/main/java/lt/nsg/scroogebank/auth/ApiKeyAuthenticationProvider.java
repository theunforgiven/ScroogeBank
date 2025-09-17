package lt.nsg.scroogebank.auth;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class ApiKeyAuthenticationProvider implements AuthenticationProvider {
    private final AuthenticationService authenticationService;

    public ApiKeyAuthenticationProvider(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        PreAuthenticatedAuthenticationToken apiKeyToken = (PreAuthenticatedAuthenticationToken) authentication;
        var loadedUser = this.authenticationService.loadUser(apiKeyToken);
        if (loadedUser != null) {
            return loadedUser;
        }

        throw new BadCredentialsException("Invalid API key");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(PreAuthenticatedAuthenticationToken.class);
    }
}
