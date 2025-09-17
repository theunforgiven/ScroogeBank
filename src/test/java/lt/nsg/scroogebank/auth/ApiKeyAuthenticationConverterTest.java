package lt.nsg.scroogebank.auth;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiKeyAuthenticationConverterTest {
    private final ApiKeyAuthenticationConverter converter = new ApiKeyAuthenticationConverter();

    @Test
    void canConvertRequestToPreAuthToken() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        UUID apiKeyId = UUID.randomUUID();
        UUID apiKeySecret = UUID.randomUUID();
        var headerValue = apiKeyId + ":" + apiKeySecret;
        mockRequest.addHeader(ApiKeyAuthenticationConverter.AUTH_TOKEN_HEADER_NAME, headerValue);
        PreAuthenticatedAuthenticationToken result = (PreAuthenticatedAuthenticationToken) converter.convert(mockRequest);
        assertEquals(apiKeyId, result.getPrincipal());
        assertEquals(apiKeySecret, result.getCredentials());
    }
}