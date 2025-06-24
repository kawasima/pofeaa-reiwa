package pofeaa.combination.transactionscript;

import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pofeaa.combination.DbSetup;
import pofeaa.combination.transactionscript.generated.tables.records.ActivitiesRecord;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for SendMoneyController demonstrating the Transaction Script pattern.
 * 
 * Tests verify that the money transfer logic works correctly using
 * Table Data Gateway pattern for data access.
 */
@DisplayName("Transaction Script Pattern - SendMoneyController Tests")
class SendMoneyControllerTest {

    private DSLContext ctx;
    private SendMoneyController controller;
    private AccountGateway accountGateway;
    private ActivityGateway activityGateway;
    private DbSetup dbSetup;

    @BeforeEach
    void setUp() {
        // Setup in-memory database with unique name for each test
        var ds = new org.h2.jdbcx.JdbcDataSource();
        ds.setURL("jdbc:h2:mem:sendmoneytest" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");
        
        ctx = DSL.using(ds, SQLDialect.H2);
        dbSetup = new DbSetup();
        
        // Create schema
        dbSetup.up(ctx);
        dbSetup.createIndexes(ctx);
        
        // Create gateways and controller
        accountGateway = new AccountGateway(ctx);
        activityGateway = new ActivityGateway(ctx);
        controller = new SendMoneyController(ctx);
        
        // Insert test data
        insertTestData();
    }

    private void insertTestData() {
        // Create two test accounts
        accountGateway.insert(1L, new BigDecimal("1000.00"), "USD", "SAVING", 
                            new BigDecimal("0.025"), null, null);
        accountGateway.insert(2L, new BigDecimal("500.00"), "USD", "CHECKING", 
                            null, new BigDecimal("200.00"), new BigDecimal("0.18"));
    }

    @Test
    @DisplayName("Should successfully transfer money between accounts")
    void shouldSuccessfullyTransferMoneyBetweenAccounts() {
        // When - Transfer $100 from account 1 to account 2
        boolean result = controller.sendMoney(1L, 2L, 100L);

        // Then - Transfer should succeed
        assertThat(result).isTrue();
        
        // Verify activities were created
        Result<ActivitiesRecord> sourceActivities = activityGateway.findByOwnerAccount(1L);
        Result<ActivitiesRecord> targetActivities = activityGateway.findByOwnerAccount(2L);
        
        assertThat(sourceActivities).hasSize(1);
        assertThat(targetActivities).hasSize(1);
        
        // Verify withdrawal activity
        ActivitiesRecord withdrawal = sourceActivities.getFirst();
        assertThat(withdrawal.getOwnerAccountId()).isEqualTo(1L);
        assertThat(withdrawal.getSourceAccountId()).isEqualTo(1L);
        assertThat(withdrawal.getTargetAccountId()).isEqualTo(2L);
        assertThat(withdrawal.getAmount()).isEqualTo(new BigDecimal("100.00"));
        
        // Verify deposit activity
        ActivitiesRecord deposit = targetActivities.getFirst();
        assertThat(deposit.getOwnerAccountId()).isEqualTo(2L);
        assertThat(deposit.getSourceAccountId()).isEqualTo(1L);
        assertThat(deposit.getTargetAccountId()).isEqualTo(2L);
        assertThat(deposit.getAmount()).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Should fail when source account has insufficient funds")
    void shouldFailWhenSourceAccountHasInsufficientFunds() {
        // When - Try to transfer $1500 from account 1 (only has $1000)
        boolean result = controller.sendMoney(1L, 2L, 1500L);

        // Then - Transfer should fail
        assertThat(result).isFalse();
        
        // Verify no activities were created
        Result<ActivitiesRecord> sourceActivities = activityGateway.findByOwnerAccount(1L);
        Result<ActivitiesRecord> targetActivities = activityGateway.findByOwnerAccount(2L);
        
        assertThat(sourceActivities).isEmpty();
        assertThat(targetActivities).isEmpty();
    }

    @Test
    @DisplayName("Should fail when source account does not exist")
    void shouldFailWhenSourceAccountDoesNotExist() {
        // When - Try to transfer from non-existent account
        boolean result = controller.sendMoney(999L, 2L, 100L);

        // Then - Transfer should fail
        assertThat(result).isFalse();
        
        // Verify no activities were created
        Result<ActivitiesRecord> targetActivities = activityGateway.findByOwnerAccount(2L);
        assertThat(targetActivities).isEmpty();
    }

    @Test
    @DisplayName("Should fail when target account does not exist")
    void shouldFailWhenTargetAccountDoesNotExist() {
        // When - Try to transfer to non-existent account
        boolean result = controller.sendMoney(1L, 999L, 100L);

        // Then - Transfer should fail
        assertThat(result).isFalse();
        
        // Verify no activities were created
        Result<ActivitiesRecord> sourceActivities = activityGateway.findByOwnerAccount(1L);
        assertThat(sourceActivities).isEmpty();
    }

    @Test
    @DisplayName("Should allow overdraft for checking accounts within limit")
    void shouldAllowOverdraftForCheckingAccountsWithinLimit() {
        // When - Transfer $650 from checking account (has $500 + $200 overdraft = $700 total)
        boolean result = controller.sendMoney(2L, 1L, 650L);

        // Then - Transfer should succeed
        assertThat(result).isTrue();
        
        // Verify activities were created
        Result<ActivitiesRecord> sourceActivities = activityGateway.findByOwnerAccount(2L);
        assertThat(sourceActivities).hasSize(1);
    }

    @Test
    @DisplayName("Should reject overdraft beyond limit for checking accounts")
    void shouldRejectOverdraftBeyondLimitForCheckingAccounts() {
        // When - Try to transfer $800 from checking account (exceeds $500 + $200 overdraft)
        boolean result = controller.sendMoney(2L, 1L, 800L);

        // Then - Transfer should fail
        assertThat(result).isFalse();
        
        // Verify no activities were created
        Result<ActivitiesRecord> sourceActivities = activityGateway.findByOwnerAccount(2L);
        assertThat(sourceActivities).isEmpty();
    }
}