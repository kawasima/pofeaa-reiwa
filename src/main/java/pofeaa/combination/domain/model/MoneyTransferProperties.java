package pofeaa.combination.domain.model;

import pofeaa.original.base.money.Money;

import java.math.BigDecimal;

public class MoneyTransferProperties {
    private final Money maximumTransferThreshold = Money.dollars(new BigDecimal(1_000_000L));

    public Money getMaximumTransferThreshold() {
        return maximumTransferThreshold;
    }
}
