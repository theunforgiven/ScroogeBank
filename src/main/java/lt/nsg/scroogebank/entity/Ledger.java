package lt.nsg.scroogebank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
public class Ledger {
    @Id
    @Getter
    @Setter
    private UUID id;
    @Getter
    @Setter
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_ledger_account_id"))
    private Account account;
    @Getter
    @Setter
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_ledger_user_id"))
    private User user;
    @Getter
    @Setter
    @Column(nullable = false)
    private BigInteger transactionAmountCents;
    @Getter
    @Setter
    @Column(nullable = false)
    private ZonedDateTime timestamp;
}
