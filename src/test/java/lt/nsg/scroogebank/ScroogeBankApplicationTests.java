package lt.nsg.scroogebank;

import lt.nsg.scroogebank.auth.ApiKeyAuthenticationConverter;
import lt.nsg.scroogebank.dto.AccountTransaction;
import lt.nsg.scroogebank.dto.CashOnHandResponse;
import lt.nsg.scroogebank.dto.CreateUserResponse;
import lt.nsg.scroogebank.dto.OpenAccountResponse;
import lt.nsg.scroogebank.dto.OpenTransactionResponse;
import lt.nsg.scroogebank.dto.TransactionType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigInteger;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ScroogeBankApplicationTests {
    @Autowired
    WebTestClient webTestClient;

    @Test
    void canCreateUserAccountAndUseIt() {
        var user = webTestClient.post()
                .uri("/user/create")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(CreateUserResponse.class)
                .returnResult()
                .getResponseBody();
        var apiKey = user.getId() + ":" + user.getApiKey();
        var accountId = webTestClient.post()
                .uri("/account/open")
                .header(ApiKeyAuthenticationConverter.AUTH_TOKEN_HEADER_NAME, apiKey)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(OpenAccountResponse.class)
                .returnResult()
                .getResponseBody()
                .getAccountId();
        AccountTransaction depositTx = new AccountTransaction();
        depositTx.setAmount("$5.00");
        depositTx.setType(TransactionType.deposit);
        var depositResponse = webTestClient.post()
                .uri("/account/transaction/" + accountId)
                .bodyValue(depositTx)
                .header(ApiKeyAuthenticationConverter.AUTH_TOKEN_HEADER_NAME, apiKey)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(OpenTransactionResponse.class)
                .returnResult()
                .getResponseBody();
        Assertions.assertThat(depositResponse.getBalance()).isEqualTo(BigInteger.valueOf(500));
        AccountTransaction withdrawTx = new AccountTransaction();
        withdrawTx.setAmount("$2.00");
        withdrawTx.setType(TransactionType.withdrawal);
        var withdrawalResponse = webTestClient.post()
                .uri("/account/transaction/" + accountId)
                .bodyValue(withdrawTx)
                .header(ApiKeyAuthenticationConverter.AUTH_TOKEN_HEADER_NAME, apiKey)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(OpenTransactionResponse.class)
                .returnResult()
                .getResponseBody();
        Assertions.assertThat(withdrawalResponse.getBalance()).isEqualTo(BigInteger.valueOf(300));
    }

    @Test
    void endPointRoleProtectionWorks() {
        var user = webTestClient.post()
                .uri("/user/create")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(CreateUserResponse.class)
                .returnResult()
                .getResponseBody();
        var userApiKey = user.getId() + ":" + user.getApiKey();
        webTestClient.get()
                .uri("/operations/cash")
                .header(ApiKeyAuthenticationConverter.AUTH_TOKEN_HEADER_NAME, userApiKey)
                .exchange()
                .expectStatus().isForbidden();
        webTestClient.get()
                .uri("/account/")
                .header(ApiKeyAuthenticationConverter.AUTH_TOKEN_HEADER_NAME, userApiKey)
                .exchange()
                .expectStatus()
                .is2xxSuccessful();
        var operatorUser = webTestClient.post()
                .uri("/user/create?role=operator")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(CreateUserResponse.class)
                .returnResult()
                .getResponseBody();
        var operatorApiKey = operatorUser.getId() + ":" + operatorUser.getApiKey();
        webTestClient.get()
                .uri("/account/")
                .header(ApiKeyAuthenticationConverter.AUTH_TOKEN_HEADER_NAME, operatorApiKey)
                .exchange()
                .expectStatus()
                .isForbidden();
        var cashOnHandResponse = webTestClient.get()
                .uri("/operations/cash")
                .header(ApiKeyAuthenticationConverter.AUTH_TOKEN_HEADER_NAME, operatorApiKey)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(CashOnHandResponse.class)
                .returnResult()
                .getResponseBody();
        Assertions.assertThat(cashOnHandResponse.getCashOnHand()).isEqualTo(BigInteger.ZERO);
    }
}


