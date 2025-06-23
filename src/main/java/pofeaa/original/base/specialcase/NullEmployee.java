package pofeaa.original.base.specialcase;

import java.math.BigDecimal;

public class NullEmployee implements Employee {
    @Override
    public String getName() {
        return "Null Employee";
    }

    @Override
    public BigDecimal getGrossToDate() {
        return BigDecimal.ZERO;
    }

    @Override
    public Contract getContract() {
        return Contract.NULL;
    }
}
