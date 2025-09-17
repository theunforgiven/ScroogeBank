package lt.nsg.scroogebank.auth;

import lt.nsg.scroogebank.BaseDataTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.UUID;

@Import({AuthenticationService.class})
class AuthenticationServiceTest extends BaseDataTest {
    @Autowired
    private AuthenticationService authenticationService;

    @Test
    void testAuthenticationServiceValidatesPassword() {
        var correctPassword = new PreAuthenticatedAuthenticationToken(testUser.getId(), rawPassword);
        var loadedCorrectUser = authenticationService.loadUser(correctPassword);
        Assertions.assertEquals(new ApiKeyAuthentication(testUser), loadedCorrectUser);
    }

    @Test
    void testAuthenticationServiceHandlesInvalidPassword() {
        var incorrectPassword = new PreAuthenticatedAuthenticationToken(testUser.getId(), UUID.randomUUID());
        var loadedIncorrectUser = authenticationService.loadUser(incorrectPassword);
        Assertions.assertNull(loadedIncorrectUser);
    }
}