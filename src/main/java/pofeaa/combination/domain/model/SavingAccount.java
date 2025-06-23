package pofeaa.combination.domain.model;

import pofeaa.original.base.money.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Locale;

/**
 * A savings account implementation with special business rules.
 * 
 * <p>Savings accounts have the following characteristics:</p>
 * <ul>
 *   <li>Minimum balance requirement to avoid fees</li>
 *   <li>Maximum number of withdrawals per month</li>
 *   <li>Potential overdraft protection with fees</li>
 * </ul>
 */
public class SavingAccount extends Account {
    
    private static final Money MINIMUM_BALANCE = new Money(100.00, Currency.getInstance(Locale.US));
    private static final int MAX_WITHDRAWALS_PER_MONTH = 6;
    private static final Money OVERDRAFT_FEE = new Money(35.00, Currency.getInstance(Locale.US));
    private static final boolean OVERDRAFT_PROTECTION = true;
    private static final BigDecimal DEFAULT_ANNUAL_INTEREST_RATE = new BigDecimal("0.0125"); // 1.25%
    
    private final BigDecimal annualInterestRate;
    
    /**
     * Creates a savings account.
     */
    private SavingAccount(Identity id, Money baselineBalance, ActivityWindow activityWindow,
                         BigDecimal annualInterestRate) {
        super(id, baselineBalance, activityWindow);
        this.annualInterestRate = annualInterestRate != null ? annualInterestRate : DEFAULT_ANNUAL_INTEREST_RATE;
    }
    
    /**
     * Creates a savings account with default settings.
     */
    public static SavingAccount of(Identity id, Money baselineBalance, ActivityWindow activityWindow) {
        return new SavingAccount(id, baselineBalance, activityWindow, DEFAULT_ANNUAL_INTEREST_RATE);
    }
    
    /**
     * Creates a savings account with custom interest rate.
     */
    public static SavingAccount of(Identity id, Money baselineBalance, ActivityWindow activityWindow,
                                  BigDecimal annualInterestRate) {
        return new SavingAccount(id, baselineBalance, activityWindow, annualInterestRate);
    }
    
    @Override
    public boolean withdraw(Money money, Identity targetAccountId) {
        // Check withdrawal limits
        if (!canWithdraw(money)) {
            return false;
        }
        
        // Check monthly withdrawal limit
        if (hasExceededMonthlyWithdrawalLimit()) {
            return false;
        }
        
        Money currentBalance = calculateBalance();
        Money balanceAfterWithdrawal = currentBalance.subtract(money);
        
        // Check if withdrawal would go below minimum balance
        if (balanceAfterWithdrawal.amount().compareTo(MINIMUM_BALANCE.amount()) < 0) {
            if (!OVERDRAFT_PROTECTION) {
                return false;
            }
            
            // Apply overdraft fee if overdraft protection is enabled
            Activity overdraftFee = Activity.of(
                    Identity.undecided(),
                    getId(),
                    getId(),
                    getId(), // Fee charged to self
                    LocalDateTime.now(),
                    OVERDRAFT_FEE);
            getActivityWindow().addActivity(overdraftFee);
        }
        
        // Perform the withdrawal
        Activity withdrawal = Activity.of(
                Identity.undecided(),
                getId(),
                getId(),
                targetAccountId,
                LocalDateTime.now(),
                money);
        getActivityWindow().addActivity(withdrawal);
        return true;
    }
    
    /**
     * Checks if the account can withdraw the specified amount.
     */
    private boolean canWithdraw(Money money) {
        Money currentBalance = calculateBalance();
        Money balanceAfterWithdrawal = currentBalance.subtract(money);
        
        // If overdraft protection is enabled, allow overdrafts up to a reasonable limit
        if (OVERDRAFT_PROTECTION) {
            Money overdraftLimit = new Money(500.00, money.currency());
            return balanceAfterWithdrawal.amount().compareTo(overdraftLimit.amount().negate()) >= 0;
        }
        
        // Without overdraft protection, must have sufficient funds
        return balanceAfterWithdrawal.isPositiveOrZero();
    }
    
    /**
     * Checks if the account has exceeded the monthly withdrawal limit.
     */
    private boolean hasExceededMonthlyWithdrawalLimit() {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        
        long withdrawalCount = getActivityWindow().getActivities().stream()
                .filter(activity -> activity.getSourceAccountId().equals(getId()))
                .filter(activity -> activity.getTimestamp().isAfter(oneMonthAgo))
                .count();
        
        return withdrawalCount >= MAX_WITHDRAWALS_PER_MONTH;
    }
    
    /**
     * Calculates the effective balance considering minimum balance requirements.
     */
    public Money getAvailableBalance() {
        Money currentBalance = calculateBalance();
        Money availableBalance = currentBalance.subtract(MINIMUM_BALANCE);
        return availableBalance.isPositiveOrZero() ? availableBalance : 
               new Money(0.00, currentBalance.currency());
    }
    
    /**
     * Checks if the account is below the minimum balance.
     */
    public boolean isBelowMinimumBalance() {
        return calculateBalance().amount().compareTo(MINIMUM_BALANCE.amount()) < 0;
    }
    
    /**
     * Gets the minimum balance requirement for this account.
     */
    public Money getMinimumBalance() {
        return MINIMUM_BALANCE;
    }
    
    /**
     * Gets the maximum number of withdrawals allowed per month.
     */
    public int getMaxWithdrawalsPerMonth() {
        return MAX_WITHDRAWALS_PER_MONTH;
    }
    
    /**
     * Checks if overdraft protection is enabled.
     */
    public boolean hasOverdraftProtection() {
        return OVERDRAFT_PROTECTION;
    }
    
    /**
     * Gets the number of withdrawals made this month.
     */
    public long getWithdrawalsThisMonth() {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        
        return getActivityWindow().getActivities().stream()
                .filter(activity -> activity.getSourceAccountId().equals(getId()))
                .filter(activity -> activity.getTimestamp().isAfter(oneMonthAgo))
                .count();
    }
    
    /**
     * Gets the annual interest rate for this savings account.
     */
    public BigDecimal getAnnualInterestRate() {
        return annualInterestRate;
    }
    
    /**
     * Calculates the monthly interest earned on the current balance.
     */
    public Money calculateMonthlyInterest() {
        Money currentBalance = calculateBalance();
        if (currentBalance.isPositiveOrZero()) {
            BigDecimal monthlyRate = annualInterestRate.divide(new BigDecimal("12"), 6, RoundingMode.HALF_UP);
            BigDecimal interestAmount = currentBalance.amount().multiply(monthlyRate);
            return new Money(interestAmount.doubleValue(), currentBalance.currency());
        }
        return new Money(0.00, currentBalance.currency());
    }
    
    /**
     * Applies monthly interest to the account by creating an interest activity.
     */
    public void applyMonthlyInterest() {
        Money interest = calculateMonthlyInterest();
        if (interest.amount().compareTo(BigDecimal.ZERO) > 0) {
            Activity interestActivity = Activity.of(
                    Identity.undecided(),
                    getId(),
                    Identity.of(-2L), // Interest paid by bank (external source)
                    getId(),
                    LocalDateTime.now(),
                    interest);
            getActivityWindow().addActivity(interestActivity);
        }
    }
}
