package pofeaa.original.base.servicestub;

import pofeaa.original.base.money.Money;

import java.math.BigDecimal;

public class TaxInfo {
    private final BigDecimal stateRate;
    private final Money stateAmount;

    public TaxInfo(BigDecimal stateRate, Money stateAmount) {
        this.stateRate = stateRate;
        this.stateAmount = stateAmount;
    }

    public BigDecimal getStateRate() {
        return stateRate;
    }

    public Money getStateAmount() {
        return stateAmount;
    }
}
