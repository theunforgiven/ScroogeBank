package lt.nsg.scroogebank.dto;

import lombok.Data;

@Data
public class AccountTransaction {

    private TransactionType type;
    private String amount;

    public long getParsedAmount() {
        return MoneyAmount.parseMoneyAmount(amount);
    }
}
