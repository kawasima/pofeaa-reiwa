package pofeaa.original.base.servicestub;

import pofeaa.original.base.money.Money;

public class Charge {
    private final Money amount;
    private final boolean isTaxable;

    public Charge(Money amount, boolean isTaxable) {
        this.amount = amount;
        this.isTaxable = isTaxable;
    }

    public Money getAmount() {
        return amount;
    }

    public boolean isTaxable() {
        return isTaxable;
    }
}
