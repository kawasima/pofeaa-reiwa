package pofeaa.combination.domain.repository;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import pofeaa.combination.domain.model.Activity;
import pofeaa.combination.domain.model.Identity;
import pofeaa.original.base.money.Money;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class ActivityMapper {
    private final DSLContext ctx;
    
    public ActivityMapper(DSLContext ctx) {
        this.ctx = ctx;
    }
    
    public List<Activity> findByOwnerAccountId(Long ownerAccountId) {
        if (ownerAccountId == null) {
            throw new IllegalArgumentException("Owner account ID cannot be null");
        }
        
        Result<? extends Record> activityRecords = ctx.select(
                field("id", Long.class),
                field("owner_account_id", Long.class),
                field("source_account_id", Long.class),
                field("target_account_id", Long.class),
                field("timestamp", LocalDateTime.class),
                field("amount", BigDecimal.class),
                field("currency", String.class)
            )
            .from(table("activities"))
            .where(field("owner_account_id").eq(ownerAccountId))
            .orderBy(field("timestamp"))
            .fetch();
            
        List<Activity> activities = new ArrayList<>();
        for (Record activityRecord : activityRecords) {
            Activity activity = mapActivity(activityRecord);
            activities.add(activity);
        }
        
        return activities;
    }
    
    public void insert(Activity activity) {
        if (activity.getId().isUndecided()) {
            throw new IllegalArgumentException("Cannot insert activity with undecided ID");
        }
        
        ctx.insertInto(table("activities"))
            .set(field("id"), activity.getId().asLong())
            .set(field("owner_account_id"), activity.getOwnerAccountId().asLong())
            .set(field("source_account_id"), activity.getSourceAccountId().asLong())
            .set(field("target_account_id"), activity.getTargetAccountId().asLong())
            .set(field("timestamp"), activity.getTimestamp())
            .set(field("amount"), activity.getMoney().amount())
            .set(field("currency"), activity.getMoney().currency().getCurrencyCode())
            .execute();
    }
    
    public void update(Activity activity) {
        if (activity.getId().isUndecided()) {
            throw new IllegalArgumentException("Cannot update activity with undecided ID");
        }
        
        ctx.update(table("activities"))
            .set(field("owner_account_id"), activity.getOwnerAccountId().asLong())
            .set(field("source_account_id"), activity.getSourceAccountId().asLong())
            .set(field("target_account_id"), activity.getTargetAccountId().asLong())
            .set(field("timestamp"), activity.getTimestamp())
            .set(field("amount"), activity.getMoney().amount())
            .set(field("currency"), activity.getMoney().currency().getCurrencyCode())
            .where(field("id").eq(activity.getId().asLong()))
            .execute();
    }
    
    public void delete(Activity activity) {
        if (activity.getId().isUndecided()) {
            throw new IllegalArgumentException("Cannot delete activity with undecided ID");
        }
        
        ctx.deleteFrom(table("activities"))
            .where(field("id").eq(activity.getId().asLong()))
            .execute();
    }
    
    public void deleteByOwnerAccountId(Long ownerAccountId) {
        if (ownerAccountId == null) {
            throw new IllegalArgumentException("Owner account ID cannot be null");
        }
        
        ctx.deleteFrom(table("activities"))
            .where(field("owner_account_id").eq(ownerAccountId))
            .execute();
    }
    
    private Activity mapActivity(Record record) {
        Long activityId = record.getValue(field("id", Long.class));
        Long ownerAccountId = record.getValue(field("owner_account_id", Long.class));
        Long sourceAccountId = record.getValue(field("source_account_id", Long.class));
        Long targetAccountId = record.getValue(field("target_account_id", Long.class));
        LocalDateTime timestamp = record.getValue(field("timestamp", LocalDateTime.class));
        BigDecimal amount = record.getValue(field("amount", BigDecimal.class));
        String currencyCode = record.getValue(field("currency", String.class));
        
        Money money = new Money(amount.doubleValue(), Currency.getInstance(currencyCode));
        
        return Activity.of(
            Identity.of(activityId),
            Identity.of(ownerAccountId),
            Identity.of(sourceAccountId),
            Identity.of(targetAccountId),
            timestamp,
            money
        );
    }
}