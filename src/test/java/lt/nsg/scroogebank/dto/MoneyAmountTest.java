package lt.nsg.scroogebank.dto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyAmountTest {
    @Test
    void canParseMoneyAmounts() {
        assertThat(MoneyAmount.parseMoneyAmount("$5.00")).isEqualTo(500L);
        assertThat(MoneyAmount.parseMoneyAmount("5.00")).isEqualTo(500L);
        assertThat(MoneyAmount.parseMoneyAmount(".25")).isEqualTo(25L);
        assertThat(MoneyAmount.parseMoneyAmount(".2")).isEqualTo(20L);
        assertThat(MoneyAmount.parseMoneyAmount("1")).isEqualTo(100L);
        assertThat(MoneyAmount.parseMoneyAmount("$1")).isEqualTo(100L);
        assertThat(MoneyAmount.parseMoneyAmount("$1.")).isEqualTo(100L);
        assertThat(MoneyAmount.parseMoneyAmount("1.")).isEqualTo(100L);

        Assertions.assertThrowsExactly(ScroogeMoneyException.class, () -> MoneyAmount.parseMoneyAmount(".441"));
        Assertions.assertThrowsExactly(ScroogeMoneyException.class, () -> MoneyAmount.parseMoneyAmount(".4414"));
        Assertions.assertThrowsExactly(ScroogeMoneyException.class, () -> MoneyAmount.parseMoneyAmount("-$1.44"));
        Assertions.assertThrowsExactly(ScroogeMoneyException.class, () -> MoneyAmount.parseMoneyAmount("-1.44"));
        Assertions.assertThrowsExactly(ScroogeMoneyException.class, () -> MoneyAmount.parseMoneyAmount("$-1.44"));
    }
}