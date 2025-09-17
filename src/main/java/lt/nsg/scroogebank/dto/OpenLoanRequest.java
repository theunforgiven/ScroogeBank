package lt.nsg.scroogebank.dto;

import lombok.Data;

@Data
public class OpenLoanRequest {

    private String amount;

    public long getParsedAmount() {
        return MoneyAmount.parseMoneyAmount(amount);
    }
}