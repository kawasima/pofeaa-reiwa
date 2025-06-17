package pofeaa.original.domainlogic.domainmodel;

import pofeaa.original.base.money.Money;

import java.time.LocalDate;

public class RevenueRecognition {
    private final Money amount;
    private final LocalDate date;

    public RevenueRecognition(Money amount, LocalDate date) {
        this.amount = amount;
        this.date = date;
    }

    public Money getAmount() {
        return amount;
    }

    public boolean isRecognizableBy(LocalDate asOf) {
        return asOf.isAfter(date) || asOf.isEqual(date);
    }
}
