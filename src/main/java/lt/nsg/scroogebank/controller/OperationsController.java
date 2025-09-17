package lt.nsg.scroogebank.controller;

import jakarta.transaction.Transactional;
import lt.nsg.scroogebank.dto.CashOnHandResponse;
import lt.nsg.scroogebank.entity.UserRole;
import lt.nsg.scroogebank.service.AccountService;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;

@RestController()
@RequestMapping("/operations")
@Transactional(Transactional.TxType.REQUIRED)
public class OperationsController {
    private final AccountService accountService;

    public OperationsController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Secured(UserRole.ROLE_OPERATOR)
    @GetMapping("/cash")
    public CashOnHandResponse listAccounts() {
        return new CashOnHandResponse(this.accountService.getCashOnHand());
    }
}
