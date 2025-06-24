package pofeaa.combination.domain.mapper;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pofeaa.combination.DbSetup;
import pofeaa.combination.domain.model.Account;
import pofeaa.combination.domain.model.Activity;
import pofeaa.combination.domain.model.ActivityWindow;
import pofeaa.combination.domain.model.CheckingAccount;
import pofeaa.combination.domain.model.Identity;
import pofeaa.combination.domain.model.SavingAccount;
import pofeaa.combination.transactionscript.generated.tables.records.AccountsRecord;
import pofeaa.combination.transactionscript.generated.tables.records.ActivitiesRecord;
import pofeaa.original.base.money.Money;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for Data Mapper pattern implementation.
 * 
 * Tests verify that:
 * - Data Mappers correctly map between domain objects and jOOQ Records
 * - Domain objects are unaware of persistence concerns
 * - Controller uses DataMappers directly for database access
 * - Input/output uses jOOQ Record types as specified
 */
@DisplayName("Data Mapper Pattern Tests")
class DataMapperPatternTest {

    private DSLContext ctx;
    private AccountMapper accountMapper;
    private ActivityMapper activityMapper;
    private SendMoneyController controller;
    private DbSetup dbSetup;

    @BeforeEach
    void setUp() {
        // Setup in-memory database with unique name for each test
        var ds = new org.h2.jdbcx.JdbcDataSource();
        ds.setURL("jdbc:h2:mem:datamappertest" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");
        
        ctx = DSL.using(ds, SQLDialect.H2);
        dbSetup = new DbSetup();
        
        // Create schema
        dbSetup.up(ctx);
        dbSetup.createIndexes(ctx);
        
        // Create mappers and controller
        accountMapper = new AccountMapper(ctx);
        activityMapper = new ActivityMapper(ctx);
        controller = new SendMoneyController(ctx);
        
        // Insert test data
        insertTestData();
    }

    private void insertTestData() {
        // Create test accounts using records
        AccountsRecord savingRecord = ctx.newRecord(pofeaa.combination.transactionscript.generated.Tables.ACCOUNTS);
        savingRecord.setId(1L);
        savingRecord.setBaselineBalance(new BigDecimal("1000.00"));
        savingRecord.setCurrency("USD");
        savingRecord.setAccountType("SAVING");
        savingRecord.setAnnualInterestRate(new BigDecimal("0.025"));
        accountMapper.insert(savingRecord);
        
        AccountsRecord checkingRecord = ctx.newRecord(pofeaa.combination.transactionscript.generated.Tables.ACCOUNTS);
        checkingRecord.setId(2L);
        checkingRecord.setBaselineBalance(new BigDecimal("500.00"));
        checkingRecord.setCurrency("USD");
        checkingRecord.setAccountType("CHECKING");
        checkingRecord.setOverdraftLimit(new BigDecimal("200.00"));
        checkingRecord.setOverdraftInterestRate(new BigDecimal("0.18"));
        accountMapper.insert(checkingRecord);
    }

    @Test
    @DisplayName("Should map AccountsRecord to domain Account object")
    void shouldMapAccountsRecordToDomainAccount() {
        // When - Find account using mapper
        Account account = accountMapper.findById(1L);

        // Then - Should return correct domain object
        assertThat(account).isNotNull();
        assertThat(account).isInstanceOf(SavingAccount.class);
        assertThat(account.getId().asLong()).isEqualTo(1L);
        assertThat(account.getBaselineBalance().amount()).isEqualTo(new BigDecimal("1000.00"));
        
        SavingAccount savingAccount = (SavingAccount) account;
        assertThat(savingAccount.getAnnualInterestRate()).isEqualByComparingTo(new BigDecimal("0.025"));
    }

    @Test
    @DisplayName("Should map domain Account object to AccountsRecord")
    void shouldMapDomainAccountToAccountsRecord() {
        // Given - Create domain object
        Identity id = Identity.of(3L);
        Money baselineBalance = Money.dollars(new BigDecimal("1500.00"));
        ActivityWindow activityWindow = new ActivityWindow(List.of());
        Money overdraftLimit = Money.dollars(new BigDecimal("300.00"));
        CheckingAccount account = CheckingAccount.of(id, baselineBalance, activityWindow,
                overdraftLimit, new BigDecimal("0.15"));

        // When - Convert to record
        AccountsRecord record = accountMapper.toRecord(account);

        // Then - Should create correct record
        assertThat(record.getId()).isEqualTo(3L);
        assertThat(record.getBaselineBalance()).isEqualTo(new BigDecimal("1500.00"));
        assertThat(record.getCurrency()).isEqualTo("USD");
        assertThat(record.getAccountType()).isEqualTo("CHECKING");
        assertThat(record.getOverdraftLimit()).isEqualTo(new BigDecimal("300.00"));
        assertThat(record.getOverdraftInterestRate()).isEqualTo(new BigDecimal("0.15"));
    }

    @Test
    @DisplayName("Should find activity records using mapper")
    void shouldFindActivityRecordsUsingMapper() {
        // Given - Insert an activity record
        ActivitiesRecord activityRecord = ctx.newRecord(pofeaa.combination.transactionscript.generated.Tables.ACTIVITIES);
        activityRecord.setId(1L);
        activityRecord.setOwnerAccountId(1L);
        activityRecord.setSourceAccountId(1L);
        activityRecord.setTargetAccountId(2L);
        activityRecord.setTimestamp(LocalDateTime.now());
        activityRecord.setAmount(new BigDecimal("100.00"));
        activityRecord.setCurrency("USD");
        activityMapper.insert(activityRecord);

        // When - Find activities using mapper
        List<Activity> activities = activityMapper.findByOwnerAccountId(1L);

        // Then - Should return domain objects
        assertThat(activities).hasSize(1);
        Activity activity = activities.getFirst();
        assertThat(activity.getOwnerAccountId().asLong()).isEqualTo(1L);
        assertThat(activity.getSourceAccountId().asLong()).isEqualTo(1L);
        assertThat(activity.getTargetAccountId().asLong()).isEqualTo(2L);
        assertThat(activity.getMoney().amount()).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Should successfully transfer money using data mapper pattern")
    void shouldSuccessfullyTransferMoneyUsingDataMapperPattern() {
        // When - Transfer money using controller
        boolean result = controller.sendMoney(1L, 2L, 100L);

        // Then - Transfer should succeed
        assertThat(result).isTrue();
        
        // Verify accounts were updated through domain behavior
        Account sourceAccount = accountMapper.findById(1L);
        Account targetAccount = accountMapper.findById(2L);
        
        // Source account should have activities reflecting withdrawal
        assertThat(sourceAccount.getActivityWindow().getActivities()).isNotEmpty();
        
        // Target account should have activities reflecting deposit
        assertThat(targetAccount.getActivityWindow().getActivities()).isNotEmpty();
        
        // Check activities were created properly
        List<Activity> sourceActivities = activityMapper.findByOwnerAccountId(1L);
        List<Activity> targetActivities = activityMapper.findByOwnerAccountId(2L);
        
        assertThat(sourceActivities).isNotEmpty();
        assertThat(targetActivities).isNotEmpty();
    }

    @Test
    @DisplayName("Should use jOOQ Records for data mapper input/output")
    void shouldUseJOOQRecordsForDataMapperInputOutput() {
        // When - Use record-based operations
        AccountsRecord sourceRecord = accountMapper.findRecordById(1L);
        
        // Then - Should return jOOQ Record
        assertThat(sourceRecord).isNotNull();
        assertThat(sourceRecord).isInstanceOf(AccountsRecord.class);
        assertThat(sourceRecord.getId()).isEqualTo(1L);
        assertThat(sourceRecord.getAccountType()).isEqualTo("SAVING");
        
        // When - Find activity records
        org.jooq.Result<ActivitiesRecord> activityRecords = activityMapper.findRecordsByOwnerAccountId(1L);
        
        // Then - Should return jOOQ Result of Records
        assertThat(activityRecords).isNotNull();
        assertThat(activityRecords).isInstanceOf(org.jooq.Result.class);
    }

    @Test
    @DisplayName("Should demonstrate controller direct access to data mappers")
    void shouldDemonstrateControllerDirectAccessToDataMappers() {
        // This test demonstrates that the controller directly uses DataMappers
        // rather than hiding database access behind repositories
        
        // When - Transfer using record-based endpoint
        boolean result = controller.sendMoneyUsingRecords(1L, 2L, 50L);
        
        // Then - Transfer should succeed
        assertThat(result).isTrue();
        
        // Verify the controller directly accessed data through mappers
        // by checking that activities were created
        List<Activity> activities = activityMapper.findByOwnerAccountId(1L);
        assertThat(activities).isNotEmpty();
        
        // The key difference from Repository pattern is that the controller
        // directly orchestrates data access using mappers, rather than
        // delegating to repositories that hide the data access
    }

    @Test
    @DisplayName("Should fail transfer when account does not exist")
    void shouldFailTransferWhenAccountDoesNotExist() {
        // When - Try to transfer from non-existent account
        boolean result = controller.sendMoney(999L, 2L, 100L);

        // Then - Transfer should fail
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should handle domain object mapping correctly")
    void shouldHandleDomainObjectMappingCorrectly() {
        // Given - Load account as domain object
        Account account = accountMapper.findById(2L);
        assertThat(account).isInstanceOf(CheckingAccount.class);
        
        CheckingAccount checkingAccount = (CheckingAccount) account;
        
        // When - Modify and save back
        // The domain object handles business logic
        Money transferAmount = Money.dollars(new BigDecimal("50.00"));
        boolean canWithdraw = checkingAccount.withdraw(transferAmount, Identity.of(1L));
        assertThat(canWithdraw).isTrue();
        
        // Save using mapper
        AccountsRecord updatedRecord = accountMapper.updateDomainObject(checkingAccount);
        
        // Then - Record should reflect changes
        assertThat(updatedRecord).isNotNull();
        assertThat(updatedRecord.getAccountType()).isEqualTo("CHECKING");
        
        // Verify activities were persisted
        List<Activity> activities = activityMapper.findByOwnerAccountId(2L);
        assertThat(activities).hasSize(1);
    }
}