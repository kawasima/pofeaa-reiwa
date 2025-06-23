package pofeaa.combination.domain.repository;

import org.jooq.DSLContext;
import org.jooq.Record;
import pofeaa.combination.domain.model.*;
import pofeaa.original.base.money.Money;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class AccountMapper {
    private final DSLContext ctx;
    private final ActivityMapper activityMapper;
    
    public AccountMapper(DSLContext ctx, ActivityMapper activityMapper) {
        this.ctx = ctx;
        this.activityMapper = activityMapper;
    }
    
    public Account find(Long accountId) {
        if (accountId == null) {
            throw new IllegalArgumentException("Account ID cannot be null");
        }
        
        // Fetch account data
        Record accountRecord = ctx.select(
                field("id", Long.class),
                field("baseline_balance", BigDecimal.class),
                field("currency", String.class),
                field("account_type", String.class),
                field("annual_interest_rate", BigDecimal.class),
                field("overdraft_limit", BigDecimal.class),
                field("overdraft_interest_rate", BigDecimal.class)
            )
            .from(table("accounts"))
            .where(field("id").eq(accountId))
            .fetchOne();
            
        if (accountRecord == null) {
            return null;
        }
        
        // Create Money from baseline balance
        BigDecimal baselineAmount = accountRecord.getValue(field("baseline_balance", BigDecimal.class));
        String currencyCode = accountRecord.getValue(field("currency", String.class));
        String accountType = accountRecord.getValue(field("account_type", String.class));
        BigDecimal annualInterestRate = accountRecord.getValue(field("annual_interest_rate", BigDecimal.class));
        BigDecimal overdraftLimit = accountRecord.getValue(field("overdraft_limit", BigDecimal.class));
        BigDecimal overdraftInterestRate = accountRecord.getValue(field("overdraft_interest_rate", BigDecimal.class));
        Money baselineBalance = new Money(baselineAmount.doubleValue(), Currency.getInstance(currencyCode));
        
        // Fetch activities for this account
        List<Activity> activities = activityMapper.findByOwnerAccountId(accountId);
        
        // Create ActivityWindow
        ActivityWindow activityWindow = new ActivityWindow(activities);
        
        // Create and return appropriate Account type
        Identity id = Identity.of(accountId);
        
        // Create appropriate Account type based on database field
        if ("CHECKING".equals(accountType)) {
            Money overdraftLimitMoney = overdraftLimit != null ? 
                new Money(overdraftLimit.doubleValue(), Currency.getInstance(currencyCode)) : null;
            return CheckingAccount.of(id, baselineBalance, activityWindow,
                    overdraftLimitMoney, overdraftInterestRate);
        } else {
            // Default to SavingAccount for null, "SAVING", or unknown types
            return SavingAccount.of(id, baselineBalance, activityWindow, annualInterestRate);
        }
    }
    
    public void insert(Account account) {
        if (account.getId().isUndecided()) {
            throw new IllegalArgumentException("Cannot insert account with undecided ID");
        }
        
        Long accountId = account.getId().asLong();
        Money baselineBalance = account.getBaselineBalance();
        String accountType = getAccountType(account);
        
        var insertQuery = ctx.insertInto(table("accounts"))
            .set(field("id"), accountId)
            .set(field("baseline_balance"), baselineBalance.amount())
            .set(field("currency"), baselineBalance.currency().getCurrencyCode())
            .set(field("account_type"), accountType);
            
        // Set type-specific fields
        if (account instanceof SavingAccount savingAccount) {
            insertQuery.set(field("annual_interest_rate"), savingAccount.getAnnualInterestRate());
        } else if (account instanceof CheckingAccount checkingAccount) {
            insertQuery.set(field("overdraft_limit"), checkingAccount.getOverdraftLimit().amount());
            insertQuery.set(field("overdraft_interest_rate"), checkingAccount.getOverdraftInterestRate());
        }
        
        insertQuery.execute();
            
        // Insert activities with generated IDs
        assignActivityIds(account.getActivityWindow().getActivities());
        for (Activity activity : account.getActivityWindow().getActivities()) {
            activityMapper.insert(activity);
        }
    }
    
    public void update(Account account) {
        if (account.getId().isUndecided()) {
            throw new IllegalArgumentException("Cannot update account with undecided ID");
        }
        
        Long accountId = account.getId().asLong();
        Money baselineBalance = account.getBaselineBalance();
        String accountType = getAccountType(account);
        
        var updateQuery = ctx.update(table("accounts"))
            .set(field("baseline_balance"), baselineBalance.amount())
            .set(field("currency"), baselineBalance.currency().getCurrencyCode())
            .set(field("account_type"), accountType);
            
        // Set type-specific fields
        if (account instanceof SavingAccount savingAccount) {
            updateQuery.set(field("annual_interest_rate"), savingAccount.getAnnualInterestRate());
            updateQuery.setNull(field("overdraft_limit"));
            updateQuery.setNull(field("overdraft_interest_rate"));
        } else if (account instanceof CheckingAccount checkingAccount) {
            updateQuery.setNull(field("annual_interest_rate"));
            updateQuery.set(field("overdraft_limit"), checkingAccount.getOverdraftLimit().amount());
            updateQuery.set(field("overdraft_interest_rate"), checkingAccount.getOverdraftInterestRate());
        }
        
        updateQuery.where(field("id").eq(accountId)).execute();
            
        // For simplicity, we'll delete and re-insert activities
        // In a real system, you'd want a more sophisticated approach
        activityMapper.deleteByOwnerAccountId(accountId);
        
        // Assign IDs to activities with undecided IDs
        assignActivityIds(account.getActivityWindow().getActivities());
        for (Activity activity : account.getActivityWindow().getActivities()) {
            activityMapper.insert(activity);
        }
    }
    
    public void delete(Account account) {
        if (account.getId().isUndecided()) {
            throw new IllegalArgumentException("Cannot delete account with undecided ID");
        }
        
        Long accountId = account.getId().asLong();
        
        // Delete activities first (foreign key constraint)
        activityMapper.deleteByOwnerAccountId(accountId);
            
        // Delete account
        ctx.deleteFrom(table("accounts"))
            .where(field("id").eq(accountId))
            .execute();
    }
    
    public ActivityMapper getActivityMapper() {
        return activityMapper;
    }
    
    private String getAccountType(Account account) {
        if (account instanceof SavingAccount) {
            return "SAVING";
        } else if (account instanceof CheckingAccount) {
            return "CHECKING";
        }
        return "SAVING"; // Default to saving account
    }
    
    /**
     * Assigns sequential IDs to activities that have undecided IDs.
     */
    private void assignActivityIds(List<Activity> activities) {
        long nextId = getNextActivityId();
        for (Activity activity : activities) {
            if (activity.getId().isUndecided()) {
                activity.getId().decide(nextId++);
            }
        }
    }
    
    /**
     * Gets the next available activity ID from the database.
     */
    private long getNextActivityId() {
        Long maxId = ctx.select(field("id", Long.class))
                .from(table("activities"))
                .orderBy(field("id").desc())
                .limit(1)
                .fetchOne(field("id", Long.class));
        
        return maxId != null ? maxId + 1 : 1L;
    }
}
