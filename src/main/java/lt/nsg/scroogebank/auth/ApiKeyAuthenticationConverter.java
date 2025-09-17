package lt.nsg.scroogebank.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component()
public class ApiKeyAuthenticationConverter implements AuthenticationConverter {
    public static String AUTH_TOKEN_HEADER_NAME = "X-API-KEY";

    @Override
    public Authentication convert(HttpServletRequest request) {
        String rawHeaderValue = request.getHeader(AUTH_TOKEN_HEADER_NAME);
        if (rawHeaderValue == null) {
            return null;
        }
        if (rawHeaderValue.trim().isEmpty() || !rawHeaderValue.contains(":")) {
            throw new BadCredentialsException("Invalid API Key");
        }
        var apiKeyParts = rawHeaderValue.split(":");
        UUID apiKeyId = UUID.randomUUID();
        UUID apiKeySecret = UUID.randomUUID();
        try {
            apiKeyId = UUID.fromString(apiKeyParts[0]);
            apiKeySecret = UUID.fromString(apiKeyParts[1]);
        } catch (Throwable t) {
            //ignore
        }
        return new PreAuthenticatedAuthenticationToken(apiKeyId, apiKeySecret);
    }
}
