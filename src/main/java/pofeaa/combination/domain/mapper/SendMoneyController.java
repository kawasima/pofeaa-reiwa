package pofeaa.combination.domain.mapper;

import org.jooq.DSLContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import pofeaa.combination.domain.model.Activity;
import pofeaa.combination.domain.model.Account;
import pofeaa.combination.domain.model.Identity;
import pofeaa.combination.transactionscript.generated.tables.records.AccountsRecord;
import pofeaa.original.base.money.Money;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Controller implementing money transfer using Data Mapper pattern.
 * 
 * Key characteristics of this pattern implementation:
 * - Database access is performed directly through DataMappers in the controller
 * - Domain behavior is separated from database access
 * - Uses jOOQ Record types for data mapper input/output
 * - Domain objects are unaware of persistence concerns
 * - Controller orchestrates data access and domain logic
 */
@RestController
public class SendMoneyController {
    
    private final AccountMapper accountMapper;
    private final ActivityMapper activityMapper;
    
    public SendMoneyController(DSLContext ctx) {
        this.accountMapper = new AccountMapper(ctx);
        this.activityMapper = new ActivityMapper(ctx);
    }
    
    /**
     * Transfers money between two accounts using Data Mapper pattern.
     * 
     * This implementation demonstrates the key difference from Repository pattern:
     * - Controller directly uses DataMappers for database access
     * - Domain objects contain behavior but are unaware of persistence
     * - Data access logic is in the controller, not hidden behind repositories
     * 
     * @param sourceAccountId The ID of the account to withdraw from
     * @param targetAccountId The ID of the account to deposit to
     * @param amount The amount to transfer (in major currency units)
     * @return true if transfer was successful, false otherwise
     */
    @PostMapping("/mapper/send/{sourceAccountId}/{targetAccountId}/{amount}")
    @Transactional
    public boolean sendMoney(@PathVariable("sourceAccountId") Long sourceAccountId,
                             @PathVariable("targetAccountId") Long targetAccountId,
                             @PathVariable("amount") Long amount) {
        
        Money money = Money.dollars(BigDecimal.valueOf(amount));
        LocalDateTime baselineDate = LocalDateTime.now().minusDays(10);
        
        // Load source account using DataMapper - returns domain object
        Account sourceAccount = accountMapper.findById(sourceAccountId);
        if (sourceAccount == null) {
            return false;
        }
        
        // Load target account using DataMapper - returns domain object
        Account targetAccount = accountMapper.findById(targetAccountId);
        if (targetAccount == null) {
            return false;
        }
        
        // Use domain behavior for withdrawal - domain object handles business logic
        if (!sourceAccount.withdraw(money, targetAccount.getId())) {
            return false;
        }
        
        // Use domain behavior for deposit - domain object handles business logic
        if (!targetAccount.deposit(money, sourceAccount.getId())) {
            return false;
        }
        
        // Persist changes using DataMappers - controller handles data access
        accountMapper.updateDomainObject(sourceAccount);
        accountMapper.updateDomainObject(targetAccount);
        
        return true;
    }
    
    /**
     * Alternative implementation using jOOQ Records directly for data access.
     * This shows how the controller can work with Record types as input/output.
     * 
     * @param sourceAccountId The ID of the account to withdraw from
     * @param targetAccountId The ID of the account to deposit to
     * @param amount The amount to transfer
     * @return true if transfer was successful, false otherwise
     */
    @PostMapping("/mapper/send-records/{sourceAccountId}/{targetAccountId}/{amount}")
    @Transactional
    public boolean sendMoneyUsingRecords(@PathVariable("sourceAccountId") Long sourceAccountId,
                                        @PathVariable("targetAccountId") Long targetAccountId,
                                        @PathVariable("amount") Long amount) {
        
        Money money = Money.dollars(BigDecimal.valueOf(amount));
        
        // Load account records using DataMapper
        AccountsRecord sourceRecord = accountMapper.findRecordById(sourceAccountId);
        if (sourceRecord == null) {
            return false;
        }
        
        AccountsRecord targetRecord = accountMapper.findRecordById(targetAccountId);
        if (targetRecord == null) {
            return false;
        }
        
        // Convert to domain objects for business logic
        Account sourceAccount = accountMapper.toDomainObject(sourceRecord);
        Account targetAccount = accountMapper.toDomainObject(targetRecord);
        
        // Use domain behavior
        if (!sourceAccount.withdraw(money, targetAccount.getId())) {
            return false;
        }
        
        if (!targetAccount.deposit(money, sourceAccount.getId())) {
            return false;
        }
        
        // Convert back to records and update
        AccountsRecord updatedSourceRecord = accountMapper.toRecord(sourceAccount);
        AccountsRecord updatedTargetRecord = accountMapper.toRecord(targetAccount);
        
        accountMapper.update(updatedSourceRecord);
        accountMapper.update(updatedTargetRecord);
        
        // Insert activity records for the transfer
        insertTransferActivities(sourceAccountId, targetAccountId, money);
        
        return true;
    }
    
    /**
     * Helper method to insert transfer activities as records.
     * 
     * @param sourceAccountId Source account ID
     * @param targetAccountId Target account ID
     * @param money Transfer amount
     */
    private void insertTransferActivities(Long sourceAccountId, Long targetAccountId, Money money) {
        LocalDateTime now = LocalDateTime.now();
        
        // Create domain objects for activities
        Identity sourceId = Identity.of(sourceAccountId);
        Identity targetId = Identity.of(targetAccountId);
        
        Activity withdrawalActivity =
            Activity.of(
                Identity.undecided(), sourceId, sourceId, targetId, now, money);
                
        Activity depositActivity =
            Activity.of(
                Identity.undecided(), targetId, sourceId, targetId, now, money);
        
        // Insert using mapper
        activityMapper.insertDomainObject(withdrawalActivity);
        activityMapper.insertDomainObject(depositActivity);
    }
}