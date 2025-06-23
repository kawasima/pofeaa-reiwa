package pofeaa.original.base.money;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;

/**
 * Represents a monetary value with a specific currency.
 * This class provides immutable money objects with support for basic operations
 * like addition and allocation while maintaining currency safety.
 * 
 * <p>The Money class stores amounts internally as the smallest unit of the currency
 * (e.g., cents for USD) to avoid floating-point precision issues.</p>
 * 
 * @author Martin Fowler
 */
public class Money {
    public static final Money ZERO = new Money(0, Currency.getInstance(Locale.US), true);
    private final long amount;
    private final Currency currency;

    /**
     * Creates a Money object from a decimal amount and currency.
     * The amount is automatically converted to the smallest unit of the currency.
     * 
     * @param amount the monetary amount as a decimal value
     * @param currency the currency for this money
     */
    public Money(double amount, Currency currency) {
        this.currency = currency;
        this.amount = Math.round(amount * centFactor());
    }
    /**
     * Creates a Money object from a whole number amount and currency.
     * The amount is multiplied by the currency's cent factor.
     * 
     * @param amount the monetary amount as a whole number
     * @param currency the currency for this money
     */
    public Money(long amount, Currency currency) {
        this.currency = currency;
        this.amount = amount * centFactor();
    }

    /**
     * Private constructor for creating Money objects with raw cent values.
     * Used internally to avoid unnecessary conversions.
     * 
     * @param amount the amount in the smallest currency unit (e.g., cents)
     * @param currency the currency for this money
     * @param raw flag to indicate raw cent value (not used, exists for constructor overloading)
     */
    private Money(long amount, Currency currency, boolean raw) {
        this.amount = amount;
        this.currency = currency;
    }

    private static final int[] cents = { 1, 10, 100, 1000 };

    /**
     * Returns the factor to convert between major and minor currency units.
     * For example, returns 100 for USD (dollars to cents).
     * 
     * @return the conversion factor based on the currency's default fraction digits
     */
    private int centFactor() {
        return cents[currency.getDefaultFractionDigits()];
    }

    /**
     * Creates a Money object representing US dollars.
     * 
     * @param amount the amount in dollars
     * @return a Money object with USD currency
     */
    public static Money dollars(BigDecimal amount) {
        return new Money(amount.doubleValue(), Currency.getInstance(Locale.US));
    }

    /**
     * Returns the monetary amount as a BigDecimal.
     * 
     * @return the amount in the major currency unit (e.g., dollars, not cents)
     */
    public BigDecimal amount() {
        return BigDecimal.valueOf(amount, currency.getDefaultFractionDigits());
    }
    /**
     * Returns the currency of this Money object.
     * 
     * @return the currency
     */
    public Currency currency() {
        return currency;
    }

    /**
     * Adds another Money object to this one.
     * Both Money objects must have the same currency.
     * 
     * @param other the Money to add
     * @return a new Money object representing the sum
     * @throws IllegalArgumentException if the currencies don't match
     */
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add Money with different currencies");
        }
        return new Money(this.amount + other.amount, this.currency, true);
    }

    /**
     * Subtracts another Money object from this one.
     * Both Money objects must have the same currency.
     * 
     * @param other the Money to subtract
     * @return a new Money object representing the difference
     * @throws IllegalArgumentException if the currencies don't match
     */
    public Money subtract(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot subtract Money with different currencies");
        }
        return new Money(this.amount - other.amount, this.currency, true);
    }

    /**
     * Multiplies this Money amount by a BigDecimal factor.
     * 
     * @param other the factor to multiply by
     * @return a new Money object representing the product
     * @throws IllegalArgumentException if the factor is null
     */
    public Money multiply(BigDecimal other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot multiply Money by null");
        }
        
        // Multiply the amount in smallest units (e.g., cents) by the factor
        // Use BigDecimal arithmetic for precision, then round to nearest long
        BigDecimal result = BigDecimal.valueOf(this.amount).multiply(other);
        long newAmount = result.setScale(0, java.math.RoundingMode.HALF_UP).longValue();
        
        return new Money(newAmount, this.currency, true);
    }
    /**
     * Checks if this Money amount is positive or zero.
     * 
     * @return true if the amount is greater than or equal to zero, false otherwise
     */
    public boolean isPositiveOrZero() {
        return amount >= 0;
    }

    /**
     * Allocates this Money equally among n recipients.
     * Any remainder from the division is distributed one cent at a time
     * to the first recipients.
     * 
     * @param n the number of recipients
     * @return an array of Money objects representing the allocation
     */
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

    /**
     * Allocates this Money according to the given ratios.
     * The ratios represent the relative proportions for each recipient.
     * Any remainder is distributed one cent at a time to the first recipients.
     * 
     * @param ratios an array of ratios for proportional allocation
     * @return an array of Money objects representing the allocation
     */
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

    /**
     * Creates a new Money object with the same currency and the specified amount in cents.
     * 
     * @param amount the amount in the smallest currency unit
     * @return a new Money object
     */
    private Money newMoney(long amount) {
        return new Money(amount, this.currency, true);
    }

    /**
     * Checks if this Money object is equal to another object.
     * Two Money objects are equal if they have the same amount and currency.
     * 
     * @param o the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money money)) return false;
        return amount == money.amount && currency.equals(money.currency);
    }

    /**
     * Returns a hash code value for this Money object.
     * 
     * @return a hash code value
     */
    @Override
    public int hashCode() {
        int result = Long.hashCode(amount);
        result = 31 * result + currency.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s %s", amount(), currency.getSymbol());
    }

}
