package pofeaa.combination.transactionscript;

import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.impl.DSL;
import pofeaa.combination.transactionscript.generated.tables.Accounts;
import pofeaa.combination.transactionscript.generated.tables.records.AccountsRecord;

import java.math.BigDecimal;

import static pofeaa.combination.transactionscript.generated.Tables.ACCOUNTS;

/**
 * Table Data Gateway implementation for Account operations using jOOQ generated classes.
 * <br/>
 * This gateway provides methods to perform database operations on the accounts table,
 * following the Table Data Gateway pattern from Martin Fowler's PoEAA.
 * <br/>
 * Key characteristics:
 * - One instance handles all rows in the accounts table
 * - Methods return RecordSets (Result<AccountsRecord> in jOOQ)
 * - Simple CRUD operations with SQL encapsulation
 * - No business logic, only data access
 */
public class AccountGateway {
    
    private final DSLContext ctx;
    private final Accounts accountsTable;
    
    public AccountGateway(DSLContext ctx) {
        this.ctx = ctx;
        this.accountsTable = ACCOUNTS;
    }
    
    /**
     * Finds an account by ID.
     * Returns a RecordSet with zero or one record.
     * 
     * @param id The account ID
     * @return Result containing the account record
     */
    public Result<AccountsRecord> find(Long id) {
        return ctx.selectFrom(accountsTable)
                .where(accountsTable.ID.eq(id))
                .fetch();
    }
    
    /**
     * Finds all accounts.
     * 
     * @return Result containing all account records
     */
    public Result<AccountsRecord> findAll() {
        return ctx.selectFrom(accountsTable)
                .orderBy(accountsTable.ID)
                .fetch();
    }
    
    /**
     * Finds all accounts by account type.
     * 
     * @param accountType The account type (SAVING or CHECKING)
     * @return Result containing matching account records
     */
    public Result<AccountsRecord> findByAccountType(String accountType) {
        return ctx.selectFrom(accountsTable)
                .where(accountsTable.ACCOUNT_TYPE.eq(accountType))
                .orderBy(accountsTable.ID)
                .fetch();
    }
    
    /**
     * Finds all accounts with baseline balance greater than the specified amount.
     * 
     * @param amount The minimum balance threshold
     * @return Result containing matching account records
     */
    public Result<AccountsRecord> findByBalanceGreaterThan(BigDecimal amount) {
        return ctx.selectFrom(accountsTable)
                .where(accountsTable.BASELINE_BALANCE.gt(amount))
                .orderBy(accountsTable.BASELINE_BALANCE.desc())
                .fetch();
    }
    
    /**
     * Finds all accounts with baseline balance between the specified amounts.
     * 
     * @param minAmount The minimum balance (inclusive)
     * @param maxAmount The maximum balance (inclusive)
     * @return Result containing matching account records
     */
    public Result<AccountsRecord> findByBalanceBetween(BigDecimal minAmount, BigDecimal maxAmount) {
        return ctx.selectFrom(accountsTable)
                .where(accountsTable.BASELINE_BALANCE.between(minAmount, maxAmount))
                .orderBy(accountsTable.BASELINE_BALANCE)
                .fetch();
    }
    
    /**
     * Finds all accounts by currency.
     * 
     * @param currency The currency code (e.g., "USD", "EUR")
     * @return Result containing matching account records
     */
    public Result<AccountsRecord> findByCurrency(String currency) {
        return ctx.selectFrom(accountsTable)
                .where(accountsTable.CURRENCY.eq(currency))
                .orderBy(accountsTable.ID)
                .fetch();
    }
    
    /**
     * Inserts a new account record.
     * 
     * @param id The account ID
     * @param baselineBalance The initial balance
     * @param currency The currency code
     * @param accountType The account type
     * @param annualInterestRate The annual interest rate (for savings accounts)
     * @param overdraftLimit The overdraft limit (for checking accounts)
     * @param overdraftInterestRate The overdraft interest rate (for checking accounts)
     * @return Number of rows inserted
     */
    public int insert(Long id, BigDecimal baselineBalance, String currency, String accountType,
                     BigDecimal annualInterestRate, BigDecimal overdraftLimit, 
                     BigDecimal overdraftInterestRate) {
        return ctx.insertInto(accountsTable)
                .set(accountsTable.ID, id)
                .set(accountsTable.BASELINE_BALANCE, baselineBalance)
                .set(accountsTable.CURRENCY, currency)
                .set(accountsTable.ACCOUNT_TYPE, accountType)
                .set(accountsTable.ANNUAL_INTEREST_RATE, annualInterestRate)
                .set(accountsTable.OVERDRAFT_LIMIT, overdraftLimit)
                .set(accountsTable.OVERDRAFT_INTEREST_RATE, overdraftInterestRate)
                .execute();
    }
    
    /**
     * Inserts a new account record using a record object.
     * 
     * @param record The account record to insert
     * @return Number of rows inserted
     */
    public int insert(AccountsRecord record) {
        return ctx.insertInto(accountsTable)
                .set(record)
                .execute();
    }
    
    /**
     * Updates an account's baseline balance.
     * 
     * @param id The account ID
     * @param newBalance The new balance
     * @return Number of rows updated
     */
    public int updateBalance(Long id, BigDecimal newBalance) {
        return ctx.update(accountsTable)
                .set(accountsTable.BASELINE_BALANCE, newBalance)
                .where(accountsTable.ID.eq(id))
                .execute();
    }
    
    /**
     * Updates an account record.
     * 
     * @param id The account ID to update
     * @param baselineBalance The new balance
     * @param currency The currency code
     * @param accountType The account type
     * @param annualInterestRate The annual interest rate
     * @param overdraftLimit The overdraft limit
     * @param overdraftInterestRate The overdraft interest rate
     * @return Number of rows updated
     */
    public int update(Long id, BigDecimal baselineBalance, String currency, String accountType,
                     BigDecimal annualInterestRate, BigDecimal overdraftLimit, 
                     BigDecimal overdraftInterestRate) {
        return ctx.update(accountsTable)
                .set(accountsTable.BASELINE_BALANCE, baselineBalance)
                .set(accountsTable.CURRENCY, currency)
                .set(accountsTable.ACCOUNT_TYPE, accountType)
                .set(accountsTable.ANNUAL_INTEREST_RATE, annualInterestRate)
                .set(accountsTable.OVERDRAFT_LIMIT, overdraftLimit)
                .set(accountsTable.OVERDRAFT_INTEREST_RATE, overdraftInterestRate)
                .where(accountsTable.ID.eq(id))
                .execute();
    }
    
    /**
     * Updates interest rates for all savings accounts.
     * Batch operation demonstrating Table Data Gateway pattern.
     * 
     * @param newRate The new annual interest rate
     * @return Number of rows updated
     */
    public int updateInterestRateForAllSavings(BigDecimal newRate) {
        return ctx.update(accountsTable)
                .set(accountsTable.ANNUAL_INTEREST_RATE, newRate)
                .where(accountsTable.ACCOUNT_TYPE.eq("SAVING"))
                .execute();
    }
    
    /**
     * Deletes an account by ID.
     * 
     * @param id The account ID
     * @return Number of rows deleted
     */
    public int delete(Long id) {
        return ctx.deleteFrom(accountsTable)
                .where(accountsTable.ID.eq(id))
                .execute();
    }
    
    /**
     * Deletes all accounts with zero balance.
     * Batch operation demonstrating Table Data Gateway pattern.
     * 
     * @return Number of rows deleted
     */
    public int deleteAccountsWithZeroBalance() {
        return ctx.deleteFrom(accountsTable)
                .where(accountsTable.BASELINE_BALANCE.eq(BigDecimal.ZERO))
                .execute();
    }
    
    /**
     * Counts total number of accounts.
     * 
     * @return Total count
     */
    public int count() {
        Integer result = ctx.selectCount()
                .from(accountsTable)
                .fetchOne(0, Integer.class);
        return result != null ? result : 0;
    }
    
    /**
     * Counts accounts by type.
     * 
     * @param accountType The account type
     * @return Count for the specified type
     */
    public int countByType(String accountType) {
        Integer result = ctx.selectCount()
                .from(accountsTable)
                .where(accountsTable.ACCOUNT_TYPE.eq(accountType))
                .fetchOne(0, Integer.class);
        return result != null ? result : 0;
    }
    
    /**
     * Calculates the sum of all baseline balances for a specific currency.
     * 
     * @param currency The currency code
     * @return Sum of balances
     */
    public BigDecimal sumBalancesByCurrency(String currency) {
        BigDecimal sum = ctx.select(DSL.sum(accountsTable.BASELINE_BALANCE))
                .from(accountsTable)
                .where(accountsTable.CURRENCY.eq(currency))
                .fetchOne(0, BigDecimal.class);
        return sum != null ? sum : BigDecimal.ZERO;
    }
    
    /**
     * Finds accounts with overdraft (checking accounts with overdraft limit).
     * 
     * @return Result containing checking accounts with overdraft
     */
    public Result<AccountsRecord> findCheckingAccountsWithOverdraft() {
        return ctx.selectFrom(accountsTable)
                .where(accountsTable.ACCOUNT_TYPE.eq("CHECKING"))
                .and(accountsTable.OVERDRAFT_LIMIT.isNotNull())
                .and(accountsTable.OVERDRAFT_LIMIT.gt(BigDecimal.ZERO))
                .orderBy(accountsTable.OVERDRAFT_LIMIT.desc())
                .fetch();
    }
    
    /**
     * Gets the next available ID for a new account.
     * Helper method for ID generation.
     * 
     * @return Next available ID
     */
    public Long getNextId() {
        Long maxId = ctx.select(DSL.max(accountsTable.ID))
                .from(accountsTable)
                .fetchOne(0, Long.class);
        return maxId != null ? maxId + 1 : 1L;
    }
}