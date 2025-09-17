package lt.nsg.scroogebank.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

@Entity(name = "Users")
@NoArgsConstructor()
@AllArgsConstructor()
public class User {
    @Id
    @Getter
    @Setter
    @Column(nullable = false)
    private UUID id;
    @Getter
    @Setter
    @Column(nullable = false)
    private String apiKeyHash;
    @Getter
    @Setter
    @Column(nullable = false)
    private UserRoles role;

    @Transient
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAuthorities();
    }
}
