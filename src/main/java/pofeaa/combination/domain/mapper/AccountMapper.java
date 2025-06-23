package pofeaa.combination.domain.mapper;

import org.jooq.DSLContext;
import pofeaa.combination.domain.model.Account;
import pofeaa.combination.domain.model.ActivityWindow;
import pofeaa.combination.domain.model.CheckingAccount;
import pofeaa.combination.domain.model.Identity;
import pofeaa.combination.domain.model.SavingAccount;
import pofeaa.combination.transactionscript.generated.tables.records.AccountsRecord;
import pofeaa.original.base.money.Money;

import java.util.Currency;

import static pofeaa.combination.transactionscript.generated.Tables.ACCOUNTS;

/**
 * Data Mapper implementation for Account domain objects using jOOQ Records.
 * 
 * This mapper follows the Data Mapper pattern from Martin Fowler's PoEAA,
 * where the mapper handles the mapping between domain objects and database records.
 * 
 * Key characteristics:
 * - Maps between domain objects (Account) and jOOQ Records (AccountsRecord)
 * - Domain objects are unaware of persistence concerns
 * - Input/output uses jOOQ Record types as specified in package-info
 * - Separates domain behavior from database access
 */
public class AccountMapper {
    
    private final DSLContext ctx;
    private final ActivityMapper activityMapper;
    
    public AccountMapper(DSLContext ctx) {
        this.ctx = ctx;
        this.activityMapper = new ActivityMapper(ctx);
    }
    
    /**
     * Finds an account by ID and returns it as a domain object.
     * 
     * @param accountId The account ID
     * @return Account domain object or null if not found
     */
    public Account findById(Long accountId) {
        AccountsRecord record = ctx.selectFrom(ACCOUNTS)
                .where(ACCOUNTS.ID.eq(accountId))
                .fetchOne();
                
        if (record == null) {
            return null;
        }
        
        return toDomainObject(record);
    }
    
    /**
     * Finds an account record by ID.
     * 
     * @param accountId The account ID
     * @return AccountsRecord or null if not found
     */
    public AccountsRecord findRecordById(Long accountId) {
        return ctx.selectFrom(ACCOUNTS)
                .where(ACCOUNTS.ID.eq(accountId))
                .fetchOne();
    }
    
    /**
     * Inserts a new account using an AccountsRecord.
     * 
     * @param record The account record to insert
     * @return Number of rows inserted
     */
    public int insert(AccountsRecord record) {
        return ctx.insertInto(ACCOUNTS)
                .set(record)
                .execute();
    }
    
    /**
     * Inserts a domain Account object.
     * 
     * @param account The domain account to insert
     * @return The inserted AccountsRecord
     */
    public AccountsRecord insertDomainObject(Account account) {
        AccountsRecord record = toRecord(account);
        ctx.insertInto(ACCOUNTS)
                .set(record)
                .execute();
                
        // Insert activities
        activityMapper.insertActivities(account.getActivityWindow().getActivities());
        
        return record;
    }
    
    /**
     * Updates an account using an AccountsRecord.
     * 
     * @param record The account record to update
     * @return Number of rows updated
     */
    public int update(AccountsRecord record) {
        return ctx.update(ACCOUNTS)
                .set(record)
                .where(ACCOUNTS.ID.eq(record.getId()))
                .execute();
    }
    
    /**
     * Updates a domain Account object.
     * 
     * @param account The domain account to update
     * @return The updated AccountsRecord
     */
    public AccountsRecord updateDomainObject(Account account) {
        AccountsRecord record = toRecord(account);
        ctx.update(ACCOUNTS)
                .set(record)
                .where(ACCOUNTS.ID.eq(record.getId()))
                .execute();
                
        // For simplicity, delete and re-insert activities
        activityMapper.deleteByOwnerAccountId(account.getId().asLong());
        activityMapper.insertActivities(account.getActivityWindow().getActivities());
        
        return record;
    }
    
    /**
     * Deletes an account by ID.
     * 
     * @param accountId The account ID to delete
     * @return Number of rows deleted
     */
    public int deleteById(Long accountId) {
        // Delete activities first (foreign key constraint)
        activityMapper.deleteByOwnerAccountId(accountId);
        
        return ctx.deleteFrom(ACCOUNTS)
                .where(ACCOUNTS.ID.eq(accountId))
                .execute();
    }
    
    /**
     * Converts an AccountsRecord to a domain Account object.
     * 
     * @param record The database record
     * @return Domain Account object
     */
    public Account toDomainObject(AccountsRecord record) {
        // Create Identity
        Identity id = Identity.of(record.getId());
        
        // Create Money from baseline balance
        Money baselineBalance = new Money(
            record.getBaselineBalance().doubleValue(),
            Currency.getInstance(record.getCurrency())
        );
        
        // Fetch activities and create ActivityWindow
        ActivityWindow activityWindow = activityMapper.findActivityWindowByOwnerAccountId(record.getId());
        
        // Create appropriate Account type based on database field
        if ("CHECKING".equals(record.getAccountType())) {
            Money overdraftLimit = record.getOverdraftLimit() != null ? 
                new Money(record.getOverdraftLimit().doubleValue(), Currency.getInstance(record.getCurrency())) : 
                Money.ZERO;
            return CheckingAccount.of(id, baselineBalance, activityWindow,
                    overdraftLimit, record.getOverdraftInterestRate());
        } else {
            // Default to SavingAccount for "SAVING" or unknown types
            return SavingAccount.of(id, baselineBalance, activityWindow, 
                    record.getAnnualInterestRate());
        }
    }
    
    /**
     * Converts a domain Account object to an AccountsRecord.
     * 
     * @param account The domain object
     * @return Database record
     */
    public AccountsRecord toRecord(Account account) {
        AccountsRecord record = ctx.newRecord(ACCOUNTS);
        
        record.setId(account.getId().asLong());
        record.setBaselineBalance(account.getBaselineBalance().amount());
        record.setCurrency(account.getBaselineBalance().currency().getCurrencyCode());
        
        if (account instanceof CheckingAccount checkingAccount) {
            record.setAccountType("CHECKING");
            record.setOverdraftLimit(checkingAccount.getOverdraftLimit().amount());
            record.setOverdraftInterestRate(checkingAccount.getOverdraftInterestRate());
            record.setAnnualInterestRate(null);
        } else if (account instanceof SavingAccount savingAccount) {
            record.setAccountType("SAVING");
            record.setAnnualInterestRate(savingAccount.getAnnualInterestRate());
            record.setOverdraftLimit(null);
            record.setOverdraftInterestRate(null);
        }
        
        return record;
    }
    
    /**
     * Gets the ActivityMapper for accessing activity data.
     * 
     * @return ActivityMapper instance
     */
    public ActivityMapper getActivityMapper() {
        return activityMapper;
    }
}