package lt.nsg.scroogebank.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.math.BigInteger;
import java.util.UUID;

@Data
public class OpenTransactionResponse {
    private final UUID userId;
    private final UUID accountId;
    private final boolean isActive;
    @JsonSerialize(using = CustomBigIntegerSerializer.class)
    @JsonDeserialize(using = CustomBigIntegerDeserializer.class)
    private final BigInteger balance;
}
