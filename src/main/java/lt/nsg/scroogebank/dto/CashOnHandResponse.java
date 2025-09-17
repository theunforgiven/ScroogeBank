package lt.nsg.scroogebank.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.math.BigInteger;

public class CashOnHandResponse {
    private final BigInteger cashOnHand;

    public CashOnHandResponse(BigInteger cashOnHand) {
        this.cashOnHand = cashOnHand;
    }

    @JsonSerialize(using = CustomBigIntegerSerializer.class)
    @JsonDeserialize(using = CustomBigIntegerDeserializer.class)
    public BigInteger getCashOnHand() {
        return cashOnHand;
    }
}
