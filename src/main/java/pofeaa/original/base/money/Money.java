package pofeaa.original.base.money;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;

public class Money {
    private final long amount;
    private final Currency currency;

    public Money(double amount, Currency currency) {
        this.amount = Math.round(amount * centFactor());
        this.currency = currency;
    }
    public Money(long amount, Currency currency) {
        this.amount = amount * centFactor();
        this.currency = currency;
    }

    private Money(long amount, Currency currency, boolean raw) {
        this.amount = amount;
        this.currency = currency;
    }

    private static final int[] cents = { 1, 10, 100, 1000 };

    private int centFactor() {
        return cents[currency.getDefaultFractionDigits()];
    }

    public static Money dollars(BigDecimal amount) {
        return new Money(amount.doubleValue(), Currency.getInstance(Locale.US));
    }

    public BigDecimal amount() {
        return BigDecimal.valueOf(amount, currency.getDefaultFractionDigits());
    }
    public Currency currency() {
        return currency;
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add Money with different currencies");
        }
        return new Money(this.amount + other.amount, this.currency, true);
    }

    public Money[] allocate(int n) {
        Money lowResult = newMoney(amount / n);
        Money highResult = newMoney(lowResult.amount + 1);
        Money[] results = new Money[n];
        int remainder = (int) amount % n;
        
        for (int i = 0; i < remainder; i++) {
            results[i] = highResult;
        }
        for (int i = remainder; i < n; i++) {
            results[i] = lowResult;
        }
        return results;
    }

    public Money[] allocate(long[] ratios) {
        long total = 0;
        for (long ratio : ratios) {
            total += ratio;
        }
        
        long remainder = amount;
        Money[] results = new Money[ratios.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = newMoney(amount * ratios[i] / total);
            remainder -= results[i].amount;
        }
        
        for (int i = 0; i < remainder; i++) {
            results[i] = newMoney(results[i].amount + 1);
        }
        return results;
    }

    private Money newMoney(long amount) {
        return new Money(amount, this.currency, true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money money)) return false;
        return amount == money.amount && currency.equals(money.currency);
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(amount);
        result = 31 * result + currency.hashCode();
        return result;
    }
}
