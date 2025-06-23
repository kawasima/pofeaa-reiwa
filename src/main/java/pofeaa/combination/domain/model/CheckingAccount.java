package pofeaa.combination.domain.model;

import pofeaa.original.base.money.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Locale;

/**
 * A checking account implementation with different business rules from savings accounts.
 * 
 * <p>Checking accounts have the following characteristics:</p>
 * <ul>
 *   <li>No withdrawal limits (unlimited transactions)</li>
 *   <li>Lower or no minimum balance requirement</li>
 *   <li>Different overdraft fees and limits</li>
 *   <li>Transaction fees for excessive usage</li>
 * </ul>
 */
public class CheckingAccount extends Account {
    
    private static final Money MINIMUM_BALANCE = new Money(25.00, Currency.getInstance(Locale.US));
    private static final Money OVERDRAFT_FEE = new Money(30.00, Currency.getInstance(Locale.US));
    private static final Money TRANSACTION_FEE = new Money(2.50, Currency.getInstance(Locale.US));
    private static final int FREE_TRANSACTIONS_PER_MONTH = 20;
    private static final boolean OVERDRAFT_PROTECTION = true;
    private static final Money DEFAULT_OVERDRAFT_LIMIT = new Money(1000.00, Currency.getInstance(Locale.US));
    private static final BigDecimal DEFAULT_OVERDRAFT_INTEREST_RATE = new BigDecimal("0.18"); // 18% annually
    
    private final Money overdraftLimit;
    private final BigDecimal overdraftInterestRate;
    
    /**
     * Creates a checking account.
     */
    private CheckingAccount(Identity id, Money baselineBalance, ActivityWindow activityWindow,
                           Money overdraftLimit, BigDecimal overdraftInterestRate) {
        super(id, baselineBalance, activityWindow);
        this.overdraftLimit = overdraftLimit != null ? overdraftLimit : DEFAULT_OVERDRAFT_LIMIT;
        this.overdraftInterestRate = overdraftInterestRate != null ? overdraftInterestRate : DEFAULT_OVERDRAFT_INTEREST_RATE;
    }
    
    /**
     * Creates a checking account with default settings.
     */
    public static CheckingAccount of(Identity id, Money baselineBalance, ActivityWindow activityWindow) {
        return new CheckingAccount(id, baselineBalance, activityWindow, 
                                  DEFAULT_OVERDRAFT_LIMIT, DEFAULT_OVERDRAFT_INTEREST_RATE);
    }
    
    /**
     * Creates a checking account with custom overdraft settings.
     */
    public static CheckingAccount of(Identity id, Money baselineBalance, ActivityWindow activityWindow,
                                    Money overdraftLimit, BigDecimal overdraftInterestRate) {
        return new CheckingAccount(id, baselineBalance, activityWindow, 
                                  overdraftLimit, overdraftInterestRate);
    }
    
    @Override
    public boolean withdraw(Money money, Identity targetAccountId) {
        // Check if withdrawal is possible
        if (!canWithdraw(money)) {
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
                    Identity.of(-1L), // Fee paid to bank (external account)
                    LocalDateTime.now(),
                    OVERDRAFT_FEE);
            getActivityWindow().addActivity(overdraftFee);
        }
        
        // Check if transaction fee should be applied
        if (shouldChargeTransactionFee()) {
            Activity transactionFeeActivity = Activity.of(
                    Identity.undecided(),
                    getId(),
                    getId(),
                    Identity.of(-1L), // Fee paid to bank (external account)
                    LocalDateTime.now(),
                    TRANSACTION_FEE);
            getActivityWindow().addActivity(transactionFeeActivity);
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
    
    @Override
    public boolean deposit(Money money, Identity sourceAccountId) {
        // Check if transaction fee should be applied for deposits
        if (shouldChargeTransactionFee()) {
            Activity transactionFeeActivity = Activity.of(
                    Identity.undecided(),
                    getId(),
                    getId(),
                    Identity.of(-1L), // Fee paid to bank (external account)
                    LocalDateTime.now(),
                    TRANSACTION_FEE);
            getActivityWindow().addActivity(transactionFeeActivity);
        }
        
        // Perform the deposit
        Activity deposit = Activity.of(
                Identity.undecided(),
                getId(),
                sourceAccountId,
                getId(),
                LocalDateTime.now(),
                money);
        getActivityWindow().addActivity(deposit);
        return true;
    }
    
    /**
     * Checks if the account can withdraw the specified amount.
     */
    private boolean canWithdraw(Money money) {
        Money currentBalance = calculateBalance();
        Money balanceAfterWithdrawal = currentBalance.subtract(money);
        
        // If overdraft protection is enabled, allow overdrafts up to the configured limit
        if (OVERDRAFT_PROTECTION) {
            return balanceAfterWithdrawal.amount().compareTo(overdraftLimit.amount().negate()) >= 0;
        }
        
        // Without overdraft protection, must have sufficient funds
        return balanceAfterWithdrawal.isPositiveOrZero();
    }
    
    /**
     * Determines if a transaction fee should be charged based on monthly transaction count.
     */
    private boolean shouldChargeTransactionFee() {
        long transactionCount = getTransactionsThisMonth();
        return transactionCount >= FREE_TRANSACTIONS_PER_MONTH;
    }
    
    /**
     * Gets the number of transactions (withdrawals and deposits) made this month.
     */
    public long getTransactionsThisMonth() {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        
        return getActivityWindow().getActivities().stream()
                .filter(activity -> activity.getOwnerAccountId().equals(getId()))
                .filter(activity -> activity.getTimestamp().isAfter(oneMonthAgo))
                // Count both withdrawals (source = this account) and deposits (target = this account)
                .filter(activity -> 
                    activity.getSourceAccountId().equals(getId()) || 
                    activity.getTargetAccountId().equals(getId()))
                // Exclude fee transactions
                .filter(activity -> !isFeeTransaction(activity))
                .count();
    }
    
    /**
     * Checks if an activity represents a fee transaction.
     */
    private boolean isFeeTransaction(Activity activity) {
        // Fee transactions have source and owner pointing to this account, target to bank (-1L)
        return activity.getSourceAccountId().equals(getId()) &&
               activity.getTargetAccountId().equals(Identity.of(-1L)) &&
               activity.getOwnerAccountId().equals(getId());
    }
    
    /**
     * Calculates the available balance for withdrawals.
     */
    public Money getAvailableBalance() {
        Money currentBalance = calculateBalance();
        
        if (OVERDRAFT_PROTECTION) {
            // With overdraft protection, available balance includes overdraft limit
            return currentBalance.add(overdraftLimit);
        } else {
            // Without overdraft, available balance is limited by minimum balance
            Money availableBalance = currentBalance.subtract(MINIMUM_BALANCE);
            return availableBalance.isPositiveOrZero() ? availableBalance : 
                   new Money(0.00, currentBalance.currency());
        }
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
     * Checks if overdraft protection is enabled.
     */
    public boolean hasOverdraftProtection() {
        return OVERDRAFT_PROTECTION;
    }
    
    /**
     * Gets the number of free transactions allowed per month.
     */
    public int getFreeTransactionsPerMonth() {
        return FREE_TRANSACTIONS_PER_MONTH;
    }
    
    /**
     * Gets the transaction fee amount.
     */
    public Money getTransactionFee() {
        return TRANSACTION_FEE;
    }
    
    /**
     * Gets the total fees charged this month.
     */
    public Money getFeesThisMonth() {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        
        return getActivityWindow().getActivities().stream()
                .filter(activity -> activity.getOwnerAccountId().equals(getId()))
                .filter(activity -> activity.getTimestamp().isAfter(oneMonthAgo))
                .filter(this::isFeeTransaction)
                .map(Activity::getMoney)
                .reduce(new Money(0.00, Currency.getInstance(Locale.US)), Money::add);
    }
    
    /**
     * Gets the overdraft limit for this checking account.
     */
    public Money getOverdraftLimit() {
        return overdraftLimit;
    }
    
    /**
     * Gets the annual interest rate charged on overdrafts.
     */
    public BigDecimal getOverdraftInterestRate() {
        return overdraftInterestRate;
    }
    
    /**
     * Calculates the current overdraft amount (negative balance).
     */
    public Money getCurrentOverdraftAmount() {
        Money currentBalance = calculateBalance();
        if (currentBalance.amount().compareTo(BigDecimal.ZERO) < 0) {
            return new Money(currentBalance.amount().abs().doubleValue(), currentBalance.currency());
        }
        return new Money(0.00, currentBalance.currency());
    }
    
    /**
     * Calculates the daily interest on the current overdraft amount.
     */
    public Money calculateDailyOverdraftInterest() {
        Money overdraftAmount = getCurrentOverdraftAmount();
        if (overdraftAmount.amount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal dailyRate = overdraftInterestRate.divide(new BigDecimal("365"), 6, RoundingMode.HALF_UP);
            BigDecimal interestAmount = overdraftAmount.amount().multiply(dailyRate);
            return new Money(interestAmount.doubleValue(), overdraftAmount.currency());
        }
        return new Money(0.00, overdraftAmount.currency());
    }
    
    /**
     * Applies daily overdraft interest if the account is overdrawn.
     */
    public void applyDailyOverdraftInterest() {
        Money interest = calculateDailyOverdraftInterest();
        if (interest.amount().compareTo(BigDecimal.ZERO) > 0) {
            Activity interestActivity = Activity.of(
                    Identity.undecided(),
                    getId(),
                    getId(),
                    Identity.of(-1L), // Interest paid to bank (external account)
                    LocalDateTime.now(),
                    interest);
            getActivityWindow().addActivity(interestActivity);
        }
    }
}
