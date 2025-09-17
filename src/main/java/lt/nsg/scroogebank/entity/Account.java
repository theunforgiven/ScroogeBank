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

import java.util.UUID;

@Entity
public class Account {
    @Id
    @Getter
    @Setter
    @Column(nullable = false)
    private UUID id;
    @Getter
    @Setter
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_account_user_id"))
    private User user;
    @Getter
    @Setter
    @Column(nullable = false)
    private boolean active;
    @Getter
    @Setter
    @Column(nullable = false)
    private AccountType type;
}
