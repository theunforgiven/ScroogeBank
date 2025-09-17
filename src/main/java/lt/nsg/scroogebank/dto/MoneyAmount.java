package lt.nsg.scroogebank.dto;

public class MoneyAmount {
    public static long parseMoneyAmount(String amount) {
        if (amount.contains("-")) {
            throw new ScroogeMoneyException("Money amounts must be positive.");
        }
        if (amount.charAt(0) == '$') {
            amount = amount.substring(1);
        }
        var matches = amount.trim().split("\\.");
        if (matches.length < 1 || matches.length > 2) {
            throw new ScroogeMoneyException("Invalid money amount format.");
        }
        if (matches.length == 1) {
            return Long.parseLong(matches[0]) * 100;
        }
        if (matches[1].length() > 2) {
            throw new ScroogeMoneyException("Invalid money amount format.");
        } else if (matches[1].length() == 1) {
            matches[1] = matches[1] + "0";
        }
        var dollars = matches[0].isBlank() ? 0 : Long.parseLong(matches[0]);
        var cents = matches[1].isBlank() ? 0 : Long.parseLong(matches[1]);

        return (dollars * 100) + cents;
    }
}
