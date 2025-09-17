package lt.nsg.scroogebank.service;

import jakarta.persistence.EntityManager;
import lt.nsg.scroogebank.dto.AccountInformation;
import lt.nsg.scroogebank.dto.AccountListResponse;
import lt.nsg.scroogebank.entity.Account;
import lt.nsg.scroogebank.entity.AccountType;
import lt.nsg.scroogebank.entity.Ledger;
import lt.nsg.scroogebank.entity.User;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class AccountService {
    private final EntityManager entityManager;
    private final BigInteger maxNegativeInCents;
    private final BigDecimal cashUsePercent;

    public AccountService(EntityManager entityManager,
                          @Value("${bank.max.negative}") BigInteger maxNegative,
                          @Value("${bank.cash.use-percent}") BigInteger cashUsePercent) {
        this.entityManager = entityManager;
        this.maxNegativeInCents = maxNegative.multiply(BigInteger.valueOf(100));
        this.cashUsePercent = new BigDecimal(cashUsePercent);
    }
    public UUID openLoan(UUID userId, long loanAmount) {
        BigInteger transactionAmountCents = BigInteger.valueOf(loanAmount);
        var currentLoanAmount = this.getCashByType(AccountType.loan);
        var currentCashOnHand = this.getCashByType(AccountType.cash);
        var perDepositCashAvailable = new BigDecimal(currentCashOnHand).multiply(cashUsePercent.divide(BigDecimal.valueOf(100)));
        BigInteger totalFreeCash = currentLoanAmount.add(maxNegativeInCents).add(perDepositCashAvailable.toBigInteger());
        int i = totalFreeCash.compareTo(transactionAmountCents);
        if (i < 0) {
            throw new ScroogeServiceException("Bank does not enough cash reserve to cover the loan " + totalFreeCash + " < " + transactionAmountCents);
        }
        var accountId = this.openAccountInternal(userId, AccountType.loan);
        this.updateLedger(userId, accountId, transactionAmountCents.negate(), AccountType.loan);
        return accountId;
    }
    public UUID openAccount(UUID userId) {
        return openAccountInternal(userId, AccountType.cash);
    }

    private UUID openAccountInternal(UUID userId, AccountType accountType) {
        var user = this.entityManager.getReference(User.class, userId);
        var currentlyActiveAccounts = this.entityManager
                .createQuery("select count(*) from Account a where a.user = :user and a.active = true and a.type = :type", long.class)
                .setParameter("user", user)
                .setParameter("type", accountType)
                .getSingleResult();
        if (currentlyActiveAccounts > 0) {
            throw new ScroogeServiceException("Only one account per type can be opened per user.");
        }
        var account = new Account();
        account.setId(UUID.randomUUID());
        account.setUser(user);
        account.setActive(true);
        account.setType(accountType);
        this.entityManager.persist(account);
        return account.getId();
    }

    public void closeAccount(UUID userId, UUID accountId) {
        var account = loadActiveAccount(userId, accountId, null);
        if (account.getType() == AccountType.loan) {
            if(!Objects.equals(getAccountBalance(userId, accountId), BigInteger.ZERO)) {
                throw new  ScroogeServiceException("Bank loan must be paid off before it can be closed.");
            }
        }
        account.setActive(false);
        this.entityManager.persist(account);
    }

    public BigInteger deposit(UUID userId, UUID accountId, long depositCents) {
        BigInteger transactionAmountCents = BigInteger.valueOf(depositCents);
        this.updateLedger(userId, accountId, transactionAmountCents, null);
        return getAndAssertBalance(userId, accountId);
    }

    public BigInteger withdraw(UUID userId, UUID accountId, long depositCents) {
        BigInteger transactionAmountCents = BigInteger.valueOf(depositCents).negate();
        this.updateLedger(userId, accountId, transactionAmountCents, AccountType.cash);
        return getAndAssertBalance(userId, accountId);
    }

    private BigInteger getAndAssertBalance(UUID userId, UUID accountId) {
        var account = loadActiveAccount(userId, accountId, null);
        BigInteger accountBalance = this.getAccountBalance(userId, accountId);
        if (accountBalance.signum() == 0) {
            return accountBalance;
        }
        if (account.getType() == AccountType.loan && accountBalance.signum() > 0) {
            throw new ScroogeServiceException("Loan over balance payment is not possible.");
        } else if (account.getType() == AccountType.cash && accountBalance.signum() < 0) {
            throw new ScroogeServiceException("Account overdraft balance is not possible.");
        }
        return account.getType() == AccountType.loan ? accountBalance.negate() : accountBalance;
    }

    private BigInteger getAccountBalance(UUID userId, UUID accountId) {
        return this.entityManager
                .createQuery("select sum(l.transactionAmountCents) from Ledger l where l.user.id = :userId and l.account.id = :accountId", BigInteger.class)
                .setParameter("userId", userId)
                .setParameter("accountId", accountId)
                .getSingleResult();
    }

    private void updateLedger(UUID userId, UUID accountId, BigInteger transactionAmountCents, AccountType accountType) {
        var account = loadActiveAccount(userId, accountId, accountType);
        var ledgerEntry = new Ledger();
        ledgerEntry.setId(UUID.randomUUID());
        ledgerEntry.setAccount(account);
        ledgerEntry.setUser(account.getUser());
        ledgerEntry.setTransactionAmountCents(transactionAmountCents);
        ledgerEntry.setTimestamp(ZonedDateTime.now(ZoneId.of("UTC")));
        this.entityManager.persist(ledgerEntry);
    }

    private @NonNull Account loadActiveAccount(UUID userId, UUID accountId, AccountType accountType) {
        var user = this.entityManager.getReference(User.class, userId);
        var account = this.entityManager.find(Account.class, accountId);
        if (account == null) {
            throw new ScroogeServiceException("Account not found.");
        }
        if (!account.isActive()) {
            throw new ScroogeServiceException("Account is not active.");
        }
        if (account.getUser() != user) {
            throw new ScroogeServiceException("Can only close accounts that you own.");
        }
        if (accountType != null && account.getType() != accountType) {
            throw new ScroogeServiceException("Unexpected account type "  + account.getType() + " != " + accountType);
        }
        return account;
    }

    public AccountListResponse getAccountList(UUID userId) {
        List<AccountInformation> accountInfo = this.entityManager
                .createQuery("""
                        select a.id, a.active, ifnull(abs(sum(l.transactionAmountCents)), 0), a.type
                        from Account a left join Ledger l on a.id = l.account.id
                        where a.user.id = :userId
                        group by a.user.id, a.id, a.type, a.active""", AccountInformation.class)
                .setParameter("userId", userId)
                .getResultList();
        return new AccountListResponse(accountInfo);
    }

    public BigInteger getCashOnHand() {
        return this.entityManager
                .createQuery("select ifnull(sum(l.transactionAmountCents), 0) from Ledger l", BigInteger.class)
                .getSingleResult()
                .add(maxNegativeInCents);
    }

    public BigInteger getCashByType(AccountType accountType) {
        return this.entityManager
                .createQuery("select ifnull(sum(l.transactionAmountCents), 0) from Ledger l where l.account.type = :accountType", BigInteger.class)
                .setParameter("accountType", accountType)
                .getSingleResult();
    }
}
