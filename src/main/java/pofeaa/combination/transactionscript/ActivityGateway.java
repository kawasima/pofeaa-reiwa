package pofeaa.combination.transactionscript;

import org.jooq.DSLContext;
import org.jooq.Result;
import pofeaa.combination.transactionscript.generated.tables.Activities;
import pofeaa.combination.transactionscript.generated.tables.records.ActivitiesRecord;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static pofeaa.combination.transactionscript.generated.Tables.ACTIVITIES;

/**
 * Table Data Gateway implementation for Activity operations using jOOQ generated classes.
 * 
 * This gateway handles all database operations for the activities table,
 * following the Table Data Gateway pattern from Martin Fowler's PoEAA.
 * 
 * Key characteristics:
 * - One instance handles all rows in the activities table
 * - Methods return RecordSets (Result<ActivitiesRecord> in jOOQ)
 * - Simple CRUD operations with SQL encapsulation
 * - No business logic, only data access
 */
public class ActivityGateway {
    
    private final DSLContext ctx;
    private final Activities activitiesTable;
    
    public ActivityGateway(DSLContext ctx) {
        this.ctx = ctx;
        this.activitiesTable = ACTIVITIES;
    }
    
    /**
     * Finds an activity by ID.
     * Returns a RecordSet with zero or one record.
     * 
     * @param id The activity ID
     * @return Result containing the activity record
     */
    public Result<ActivitiesRecord> find(Long id) {
        return ctx.selectFrom(activitiesTable)
                .where(activitiesTable.ID.eq(id))
                .fetch();
    }
    
    /**
     * Finds all activities for an account owner.
     * 
     * @param ownerAccountId The owner account ID
     * @return Result containing matching activity records
     */
    public Result<ActivitiesRecord> findByOwnerAccount(Long ownerAccountId) {
        return ctx.selectFrom(activitiesTable)
                .where(activitiesTable.OWNER_ACCOUNT_ID.eq(ownerAccountId))
                .orderBy(activitiesTable.TIMESTAMP.desc())
                .fetch();
    }
    
    /**
     * Finds all activities for an account owner after a specific date.
     * 
     * @param ownerAccountId The owner account ID
     * @param afterDate Activities after this date
     * @return Result containing matching activity records
     */
    public Result<ActivitiesRecord> findByOwnerAccountAfterDate(Long ownerAccountId, LocalDateTime afterDate) {
        return ctx.selectFrom(activitiesTable)
                .where(activitiesTable.OWNER_ACCOUNT_ID.eq(ownerAccountId))
                .and(activitiesTable.TIMESTAMP.gt(afterDate))
                .orderBy(activitiesTable.TIMESTAMP.desc())
                .fetch();
    }
    
    /**
     * Inserts a new activity record.
     * 
     * @param ownerAccountId The owner account ID
     * @param sourceAccountId The source account ID
     * @param targetAccountId The target account ID
     * @param timestamp The activity timestamp
     * @param amount The money amount
     * @param currency The currency code
     * @return Number of rows inserted
     */
    public int insert(Long ownerAccountId, Long sourceAccountId, Long targetAccountId,
                     LocalDateTime timestamp, BigDecimal amount, String currency) {
        return ctx.insertInto(activitiesTable)
                .set(activitiesTable.ID, getNextId())
                .set(activitiesTable.OWNER_ACCOUNT_ID, ownerAccountId)
                .set(activitiesTable.SOURCE_ACCOUNT_ID, sourceAccountId)
                .set(activitiesTable.TARGET_ACCOUNT_ID, targetAccountId)
                .set(activitiesTable.TIMESTAMP, timestamp)
                .set(activitiesTable.AMOUNT, amount)
                .set(activitiesTable.CURRENCY, currency)
                .execute();
    }
    
    /**
     * Inserts a new activity record using a record object.
     * 
     * @param record The activity record to insert
     * @return Number of rows inserted
     */
    public int insert(ActivitiesRecord record) {
        if (record.getId() == null) {
            record.setId(getNextId());
        }
        return ctx.insertInto(activitiesTable)
                .set(record)
                .execute();
    }
    
    /**
     * Calculates the balance for activities owned by an account.
     * 
     * @param ownerAccountId The owner account ID
     * @return The calculated balance
     */
    public BigDecimal calculateBalance(Long ownerAccountId) {
        Result<ActivitiesRecord> activities = findByOwnerAccount(ownerAccountId);
        BigDecimal balance = BigDecimal.ZERO;
        
        for (ActivitiesRecord activity : activities) {
            if (activity.getOwnerAccountId().equals(activity.getTargetAccountId())) {
                // Money coming into the account (deposit)
                balance = balance.add(activity.getAmount());
            } else {
                // Money going out of the account (withdrawal)
                balance = balance.subtract(activity.getAmount());
            }
        }
        
        return balance;
    }
    
    /**
     * Calculates the balance for activities owned by an account after a specific date.
     * 
     * @param ownerAccountId The owner account ID
     * @param afterDate Calculate balance for activities after this date
     * @return The calculated balance
     */
    public BigDecimal calculateBalanceAfterDate(Long ownerAccountId, LocalDateTime afterDate) {
        Result<ActivitiesRecord> activities = findByOwnerAccountAfterDate(ownerAccountId, afterDate);
        BigDecimal balance = BigDecimal.ZERO;
        
        for (ActivitiesRecord activity : activities) {
            if (activity.getOwnerAccountId().equals(activity.getTargetAccountId())) {
                // Money coming into the account (deposit)
                balance = balance.add(activity.getAmount());
            } else {
                // Money going out of the account (withdrawal)
                balance = balance.subtract(activity.getAmount());
            }
        }
        
        return balance;
    }
    
    /**
     * Gets the next available ID for a new activity.
     * Helper method for ID generation.
     * 
     * @return Next available ID
     */
    public Long getNextId() {
        Long maxId = ctx.select(org.jooq.impl.DSL.max(activitiesTable.ID))
                .from(activitiesTable)
                .fetchOne(0, Long.class);
        return maxId != null ? maxId + 1 : 1L;
    }
}