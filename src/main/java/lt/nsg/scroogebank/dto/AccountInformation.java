package lt.nsg.scroogebank.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lt.nsg.scroogebank.entity.AccountType;

import java.math.BigInteger;
import java.util.UUID;

@Data
public class AccountInformation {
    private final UUID id;
    private final Boolean active;
    @JsonSerialize(using = CustomBigIntegerSerializer.class)
    @JsonDeserialize(using = CustomBigIntegerDeserializer.class)
    private final BigInteger balance;
    private final AccountType type;
}
