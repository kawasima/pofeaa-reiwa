package pofeaa.combination.domain.mapper;

import org.jooq.DSLContext;
import org.jooq.Result;
import pofeaa.combination.domain.model.Activity;
import pofeaa.combination.domain.model.ActivityWindow;
import pofeaa.combination.domain.model.Identity;
import pofeaa.combination.transactionscript.generated.tables.records.ActivitiesRecord;
import pofeaa.original.base.money.Money;

import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;

import static pofeaa.combination.transactionscript.generated.Tables.ACTIVITIES;

/**
 * Data Mapper implementation for Activity domain objects using jOOQ Records.
 * 
 * This mapper follows the Data Mapper pattern from Martin Fowler's PoEAA,
 * where the mapper handles the mapping between domain objects and database records.
 * 
 * Key characteristics:
 * - Maps between domain objects (Activity) and jOOQ Records (ActivitiesRecord)
 * - Domain objects are unaware of persistence concerns
 * - Input/output uses jOOQ Record types as specified in package-info
 * - Separates domain behavior from database access
 */
public class ActivityMapper {
    
    private final DSLContext ctx;
    
    public ActivityMapper(DSLContext ctx) {
        this.ctx = ctx;
    }
    
    /**
     * Finds an activity by ID and returns it as a domain object.
     * 
     * @param activityId The activity ID
     * @return Activity domain object or null if not found
     */
    public Activity findById(Long activityId) {
        ActivitiesRecord record = ctx.selectFrom(ACTIVITIES)
                .where(ACTIVITIES.ID.eq(activityId))
                .fetchOne();
                
        if (record == null) {
            return null;
        }
        
        return toDomainObject(record);
    }
    
    /**
     * Finds an activity record by ID.
     * 
     * @param activityId The activity ID
     * @return ActivitiesRecord or null if not found
     */
    public ActivitiesRecord findRecordById(Long activityId) {
        return ctx.selectFrom(ACTIVITIES)
                .where(ACTIVITIES.ID.eq(activityId))
                .fetchOne();
    }
    
    /**
     * Finds all activity records for an owner account.
     * 
     * @param ownerAccountId The owner account ID
     * @return Result containing activity records
     */
    public Result<ActivitiesRecord> findRecordsByOwnerAccountId(Long ownerAccountId) {
        return ctx.selectFrom(ACTIVITIES)
                .where(ACTIVITIES.OWNER_ACCOUNT_ID.eq(ownerAccountId))
                .orderBy(ACTIVITIES.TIMESTAMP.desc())
                .fetch();
    }
    
    /**
     * Finds all activities for an owner account as domain objects.
     * 
     * @param ownerAccountId The owner account ID
     * @return List of Activity domain objects
     */
    public List<Activity> findByOwnerAccountId(Long ownerAccountId) {
        Result<ActivitiesRecord> records = findRecordsByOwnerAccountId(ownerAccountId);
        return records.stream()
                .map(this::toDomainObject)
                .collect(Collectors.toList());
    }
    
    /**
     * Finds an ActivityWindow for an owner account.
     * 
     * @param ownerAccountId The owner account ID
     * @return ActivityWindow containing all activities for the account
     */
    public ActivityWindow findActivityWindowByOwnerAccountId(Long ownerAccountId) {
        List<Activity> activities = findByOwnerAccountId(ownerAccountId);
        return new ActivityWindow(activities);
    }
    
    /**
     * Inserts a new activity using an ActivitiesRecord.
     * 
     * @param record The activity record to insert
     * @return Number of rows inserted
     */
    public int insert(ActivitiesRecord record) {
        if (record.getId() == null) {
            record.setId(getNextId());
        }
        return ctx.insertInto(ACTIVITIES)
                .set(record)
                .execute();
    }
    
    /**
     * Inserts a domain Activity object.
     * 
     * @param activity The domain activity to insert
     * @return The inserted ActivitiesRecord
     */
    public ActivitiesRecord insertDomainObject(Activity activity) {
        ActivitiesRecord record = toRecord(activity);
        ctx.insertInto(ACTIVITIES)
                .set(record)
                .execute();
        return record;
    }
    
    /**
     * Inserts multiple activities from a list.
     * 
     * @param activities List of activities to insert
     */
    public void insertActivities(List<Activity> activities) {
        for (Activity activity : activities) {
            // Assign ID if undecided
            if (activity.getId().isUndecided()) {
                activity.getId().decide(getNextId());
            }
            insertDomainObject(activity);
        }
    }
    
    /**
     * Updates an activity using an ActivitiesRecord.
     * 
     * @param record The activity record to update
     * @return Number of rows updated
     */
    public int update(ActivitiesRecord record) {
        return ctx.update(ACTIVITIES)
                .set(record)
                .where(ACTIVITIES.ID.eq(record.getId()))
                .execute();
    }
    
    /**
     * Updates a domain Activity object.
     * 
     * @param activity The domain activity to update
     * @return The updated ActivitiesRecord
     */
    public ActivitiesRecord updateDomainObject(Activity activity) {
        ActivitiesRecord record = toRecord(activity);
        ctx.update(ACTIVITIES)
                .set(record)
                .where(ACTIVITIES.ID.eq(record.getId()))
                .execute();
        return record;
    }
    
    /**
     * Deletes an activity by ID.
     * 
     * @param activityId The activity ID to delete
     * @return Number of rows deleted
     */
    public int deleteById(Long activityId) {
        return ctx.deleteFrom(ACTIVITIES)
                .where(ACTIVITIES.ID.eq(activityId))
                .execute();
    }
    
    /**
     * Deletes all activities for an owner account.
     * 
     * @param ownerAccountId The owner account ID
     * @return Number of rows deleted
     */
    public int deleteByOwnerAccountId(Long ownerAccountId) {
        return ctx.deleteFrom(ACTIVITIES)
                .where(ACTIVITIES.OWNER_ACCOUNT_ID.eq(ownerAccountId))
                .execute();
    }
    
    /**
     * Converts an ActivitiesRecord to a domain Activity object.
     * 
     * @param record The database record
     * @return Domain Activity object
     */
    public Activity toDomainObject(ActivitiesRecord record) {
        Identity id = Identity.of(record.getId());
        Identity ownerAccountId = Identity.of(record.getOwnerAccountId());
        Identity sourceAccountId = Identity.of(record.getSourceAccountId());
        Identity targetAccountId = Identity.of(record.getTargetAccountId());
        
        Money money = new Money(
            record.getAmount().doubleValue(),
            Currency.getInstance(record.getCurrency())
        );
        
        return Activity.of(id, ownerAccountId, sourceAccountId, targetAccountId,
                record.getTimestamp(), money);
    }
    
    /**
     * Converts a domain Activity object to an ActivitiesRecord.
     * 
     * @param activity The domain object
     * @return Database record
     */
    public ActivitiesRecord toRecord(Activity activity) {
        ActivitiesRecord record = ctx.newRecord(ACTIVITIES);
        
        record.setId(activity.getId().isUndecided() ? getNextId() : activity.getId().asLong());
        record.setOwnerAccountId(activity.getOwnerAccountId().asLong());
        record.setSourceAccountId(activity.getSourceAccountId().asLong());
        record.setTargetAccountId(activity.getTargetAccountId().asLong());
        record.setTimestamp(activity.getTimestamp());
        record.setAmount(activity.getMoney().amount());
        record.setCurrency(activity.getMoney().currency().getCurrencyCode());
        
        return record;
    }
    
    /**
     * Gets the next available ID for a new activity.
     * 
     * @return Next available ID
     */
    private Long getNextId() {
        Long maxId = ctx.select(org.jooq.impl.DSL.max(ACTIVITIES.ID))
                .from(ACTIVITIES)
                .fetchOne(0, Long.class);
        return maxId != null ? maxId + 1 : 1L;
    }
}