package lt.nsg.scroogebank.service;

import lt.nsg.scroogebank.BaseDataTest;
import lt.nsg.scroogebank.dto.AccountInformation;
import lt.nsg.scroogebank.entity.AccountType;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Import({AccountService.class})
class AccountServiceTest extends BaseDataTest {
    @Value("${bank.max.negative}") BigInteger maxNegative;
    @Autowired
    private AccountService accountService;

    @Test
    void canOpenAccount() {
        this.accountService.openAccount(testUser.getId());
    }

    @Test
    void canOnlyOpenOneAccount() {
        this.accountService.openAccount(testUser.getId());
        Assertions.assertThrowsExactly(ScroogeServiceException.class,
                () -> this.accountService.openAccount(testUser.getId()));
    }

    @Test
    void canCloseOpenAccounts() {
        UUID accountId = this.accountService.openAccount(testUser.getId());
        this.accountService.closeAccount(testUser.getId(), accountId);
        this.accountService.openAccount(testUser.getId());
    }

    @Test
    void canNotCloseAccountThatIsClosed() {
        UUID accountId = this.accountService.openAccount(testUser.getId());
        this.accountService.closeAccount(testUser.getId(), accountId);
        var ex = Assertions.assertThrowsExactly(ScroogeServiceException.class,
                () -> this.accountService.closeAccount(testUser.getId(), accountId));
        assertThat(ex.getMessage()).contains("not active");
    }

    @Test
    void canNotCloseAccountThatIsNotFound() {
        var ex = Assertions.assertThrowsExactly(ScroogeServiceException.class,
                () -> this.accountService.closeAccount(testUser.getId(), UUID.randomUUID()));
        assertThat(ex.getMessage()).contains("not found");
    }

    @Test
    void canOnlyCloseAccountYouOwn() {
        var otherUser = createUser();
        UUID accountId = this.accountService.openAccount(testUser.getId());
        var ex = Assertions.assertThrowsExactly(ScroogeServiceException.class,
                () -> this.accountService.closeAccount(otherUser.getId(), accountId));
        assertThat(ex.getMessage()).contains("accounts that you own");
    }

    @Test
    void canDepositAndWithdrawFromAccount() {
        UUID accountId = this.accountService.openAccount(testUser.getId());
        BigInteger depositedBalance = this.accountService.deposit(testUser.getId(), accountId, 500L);
        assertThat(depositedBalance).isEqualTo(new BigInteger("500"));
        BigInteger withdrawalBalance = this.accountService.withdraw(testUser.getId(), accountId, 100L);
        assertThat(withdrawalBalance).isEqualTo(new BigInteger("400"));
    }

    @Test
    void canDepositAndWithdrawToZero() {
        UUID accountId = this.accountService.openAccount(testUser.getId());
        BigInteger depositedBalance = this.accountService.deposit(testUser.getId(), accountId, 500L);
        assertThat(depositedBalance).isEqualTo(new BigInteger("500"));
        BigInteger withdrawalBalance = this.accountService.withdraw(testUser.getId(), accountId, 500L);
        assertThat(withdrawalBalance).isEqualTo(BigInteger.ZERO);
    }

    @Test
    void canPayOffLoan() {
        UUID loanId = this.accountService.openLoan(testUser.getId(), 10000);
        BigInteger depositedBalance = this.accountService.deposit(testUser.getId(), loanId, 9500L);
        assertThat(depositedBalance).isEqualTo(new BigInteger("500"));
        var ex = Assertions.assertThrows(ScroogeServiceException.class, () -> this.accountService.closeAccount(testUser.getId(), loanId));
        assertThat(ex.getMessage()).contains("must be paid off");
        BigInteger withdrawalBalance = this.accountService.deposit(testUser.getId(), loanId, 500L);
        assertThat(withdrawalBalance).isEqualTo(new BigInteger("0"));
        this.accountService.closeAccount(testUser.getId(), loanId);
    }

    @Test
    void canOnlyTakeOutALoanUpToMaximums() {
        var ex = Assertions.assertThrows(ScroogeServiceException.class, () -> this.accountService.openLoan(testUser.getId(), (250_001) * 100));
        assertThat(ex.getMessage()).contains("not enough cash reserve");
        this.accountService.openLoan(testUser.getId(), (250_000) * 100);
    }

    @Test
    void canOnlyTakeOutALoanUpToMaximumsIncludingCashReserves() {
        var secondUser = createUser();
        UUID secondUserAccountId = this.accountService.openAccount(secondUser.getId());
        this.accountService.deposit(secondUser.getId(), secondUserAccountId, 350L);
        this.accountService.withdraw(secondUser.getId(), secondUserAccountId, 100L);
        this.accountService.withdraw(secondUser.getId(), secondUserAccountId, 50L);
        // 200 total * 25% = 50
        var percentExtraCash = (350L - 100L - 50L) / 4;
        var percentOfDepositsPlusCashReserve = 250_000;
        var loanId = this.accountService.openLoan(testUser.getId(), (percentOfDepositsPlusCashReserve * 100) + percentExtraCash);
        this.accountService.deposit(testUser.getId(), loanId, percentExtraCash);
        this.accountService.openLoan(secondUser.getId(), percentExtraCash);
    }

    @Test
    void canOnlyTakeOutALoanUpToMaximumsIncludingCashReserves2() {
        var secondUser = createUser();
        UUID secondUserAccountId = this.accountService.openAccount(secondUser.getId());
        this.accountService.deposit(secondUser.getId(), secondUserAccountId, 50_000L * 100);
        this.accountService.withdraw(secondUser.getId(), secondUserAccountId, 5_000L * 100);
        this.accountService.withdraw(secondUser.getId(), secondUserAccountId, 5_000L * 100);
        //40_000
        //4_000_000
        var percentOfDepositsPlusCashReserve = 260_000;
        this.accountService.openLoan(testUser.getId(), percentOfDepositsPlusCashReserve * 100);
    }

    @Test
    void canNotWithdrawMoreThanAvailableFunds() {
        UUID accountId = this.accountService.openAccount(testUser.getId());
        this.accountService.deposit(testUser.getId(), accountId, 500L);
        var ex = Assertions.assertThrows(ScroogeServiceException.class,
                () -> this.accountService.withdraw(testUser.getId(), accountId, 600L));
        assertThat(ex.getMessage()).contains("overdraft");
    }

    @Test
    void canListAllAccounts() {
        UUID accountId = this.accountService.openAccount(testUser.getId());
        this.accountService.closeAccount(testUser.getId(), accountId);

        UUID accountId2 = this.accountService.openAccount(testUser.getId());
        this.accountService.deposit(testUser.getId(), accountId2, 300L);
        this.accountService.withdraw(testUser.getId(), accountId2, 100L);
        this.accountService.closeAccount(testUser.getId(), accountId2);

        UUID accountId3 = this.accountService.openAccount(testUser.getId());
        this.accountService.deposit(testUser.getId(), accountId3, 300L);
        this.accountService.withdraw(testUser.getId(), accountId3, 100L);
        this.accountService.deposit(testUser.getId(), accountId3, 200L);

        UUID accountId4 = this.accountService.openLoan(testUser.getId(), 800L);
        this.accountService.deposit(testUser.getId(), accountId4, 300L);
        this.accountService.deposit(testUser.getId(), accountId4, 200L);

        List<AccountInformation> accounts = this.accountService.getAccountList(testUser.getId()).getAccounts();
        assertThat(accounts).hasSize(4);

        assertThat(extractAccountById(accounts, accountId).getActive()).isFalse();
        assertThat(extractAccountById(accounts, accountId).getBalance()).isEqualTo(BigInteger.valueOf(0L));
        assertThat(extractAccountById(accounts, accountId).getType()).isEqualTo(AccountType.cash);
        assertThat(extractAccountById(accounts, accountId2).getActive()).isFalse();
        assertThat(extractAccountById(accounts, accountId2).getBalance()).isEqualTo(BigInteger.valueOf(200L));
        assertThat(extractAccountById(accounts, accountId2).getType()).isEqualTo(AccountType.cash);
        assertThat(extractAccountById(accounts, accountId3).getActive()).isTrue();
        assertThat(extractAccountById(accounts, accountId3).getBalance()).isEqualTo(BigInteger.valueOf(400L));
        assertThat(extractAccountById(accounts, accountId3).getType()).isEqualTo(AccountType.cash);
        assertThat(extractAccountById(accounts, accountId4).getActive()).isTrue();
        assertThat(extractAccountById(accounts, accountId4).getType()).isEqualTo(AccountType.loan);
        assertThat(extractAccountById(accounts, accountId4).getBalance()).isEqualTo(BigInteger.valueOf(300L));
    }

    @Test
    void canGetCashOnHand() {
        UUID firstUserAccountId = this.accountService.openAccount(testUser.getId());
        this.accountService.deposit(testUser.getId(), firstUserAccountId, 500L);
        this.accountService.withdraw(testUser.getId(), firstUserAccountId, 200L);
        UUID firstUserLoanId = this.accountService.openLoan(testUser.getId(), 10000L);
        this.accountService.deposit(testUser.getId(), firstUserLoanId, 500L);

        var secondUser = createUser();
        UUID secondUserAccountId = this.accountService.openAccount(secondUser.getId());
        this.accountService.deposit(secondUser.getId(), secondUserAccountId, 300L);
        this.accountService.withdraw(secondUser.getId(), secondUserAccountId, 100L);
        this.accountService.withdraw(secondUser.getId(), secondUserAccountId, 50L);
        var thirdUser = createUser();
        UUID thirdUserAccountId = this.accountService.openAccount(thirdUser.getId());
        this.accountService.deposit(thirdUser.getId(), thirdUserAccountId, 250);
        this.accountService.withdraw(thirdUser.getId(), thirdUserAccountId, 75L);
        this.accountService.deposit(thirdUser.getId(), thirdUserAccountId, 50L);
        this.accountService.deposit(thirdUser.getId(), thirdUserAccountId, 25L);

        var cashOnHand = this.accountService.getCashOnHand();
        assertThat(cashOnHand).isEqualTo(BigInteger.valueOf(-8800L).add(maxNegative.multiply(BigInteger.valueOf(100L))));
    }

    @Test
    void cashOnHandWithEmptyDatabaseIsMaxNegative() {
        var cashOnHand = this.accountService.getCashOnHand();
        assertThat(cashOnHand).isEqualTo(maxNegative.multiply(BigInteger.valueOf(100L)));
    }

    @Test
    void canOpenLoan() {
        UUID firstUserAccountId = this.accountService.openAccount(testUser.getId());
        this.accountService.deposit(testUser.getId(), firstUserAccountId, 500L);
        this.accountService.withdraw(testUser.getId(), firstUserAccountId, 200L);

        UUID firstUserLoanId = this.accountService.openLoan(testUser.getId(), 10000L);
        var ex = Assertions.assertThrows(ScroogeServiceException.class,
                () -> this.accountService.withdraw(testUser.getId(), firstUserLoanId, 200L));
        assertThat(ex.getMessage()).contains("loan != cash");
        var loanRemaining = this.accountService.deposit(testUser.getId(), firstUserLoanId, 500L);
        assertThat(loanRemaining).isEqualTo(BigInteger.valueOf(9500));
    }

    private static @NonNull AccountInformation extractAccountById(List<AccountInformation> accounts, UUID accountId) {
        return accounts.stream().filter(account -> account.getId().equals(accountId)).findFirst().get();
    }
}