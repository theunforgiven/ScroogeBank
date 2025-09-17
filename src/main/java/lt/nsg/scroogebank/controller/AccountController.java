package lt.nsg.scroogebank.controller;

import jakarta.transaction.Transactional;
import lt.nsg.scroogebank.dto.AccountListResponse;
import lt.nsg.scroogebank.dto.AccountTransaction;
import lt.nsg.scroogebank.dto.OpenAccountResponse;
import lt.nsg.scroogebank.dto.OpenLoanRequest;
import lt.nsg.scroogebank.dto.OpenTransactionResponse;
import lt.nsg.scroogebank.entity.User;
import lt.nsg.scroogebank.entity.UserRole;
import lt.nsg.scroogebank.service.AccountService;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.UUID;

@RestController()
@RequestMapping("/account")
@Transactional(Transactional.TxType.REQUIRED)
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Secured(UserRole.ROLE_USER)
    @GetMapping("/")
    public AccountListResponse listAccounts(@AuthenticationPrincipal User user) {
        return this.accountService.getAccountList(user.getId());
    }

    @Secured(UserRole.ROLE_USER)
    @PostMapping("/open")
    public OpenAccountResponse openAccount(@AuthenticationPrincipal User user) {
        UUID accountId = this.accountService.openAccount(user.getId());
        return new OpenAccountResponse(user.getId(), accountId, true);
    }

    @Secured(UserRole.ROLE_USER)
    @PostMapping("/open_loan")
    public OpenAccountResponse openAccount(@AuthenticationPrincipal User user, @RequestBody OpenLoanRequest loanRequest) {
        UUID accountId = this.accountService.openLoan(user.getId(), loanRequest.getParsedAmount());
        return new OpenAccountResponse(user.getId(), accountId, true);
    }

    @Secured(UserRole.ROLE_USER)
    @PostMapping("/close/{accountId}")
    public OpenAccountResponse closeAccount(@AuthenticationPrincipal User user, @PathVariable() UUID accountId) {
        this.accountService.closeAccount(user.getId(), accountId);
        return new OpenAccountResponse(user.getId(), accountId, false);
    }

    @Secured(UserRole.ROLE_USER)
    @PostMapping("/transaction/{accountId}")
    public OpenTransactionResponse accountTransaction(@AuthenticationPrincipal User user,
                                                      @PathVariable() UUID accountId,
                                                      @RequestBody AccountTransaction transaction) {
        BigInteger balance = null;
        switch (transaction.getType()) {
            case withdrawal -> {
                balance = this.accountService.withdraw(user.getId(), accountId, transaction.getParsedAmount());
            }
            case deposit -> {
                balance = this.accountService.deposit(user.getId(), accountId, transaction.getParsedAmount());
            }
        }

        return new OpenTransactionResponse(user.getId(), accountId, true, balance);
    }
}
