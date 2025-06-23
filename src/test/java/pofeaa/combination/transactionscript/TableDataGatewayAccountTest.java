package pofeaa.combination.transactionscript;

import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pofeaa.combination.DbSetup;
import pofeaa.combination.transactionscript.generated.tables.records.AccountsRecord;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for AccountGateway demonstrating the Table Data Gateway pattern.
 * 
 * Key characteristics of Table Data Gateway pattern:
 * - Works with RecordSets (Result<AccountsRecord> in jOOQ)
 * - One gateway instance handles all rows in a table
 * - Methods return multiple records as results
 * - No business logic in the gateway
 */
@DisplayName("Table Data Gateway Pattern - AccountGateway Tests")
class TableDataGatewayAccountTest {

    private DSLContext ctx;
    private AccountGateway accountGateway;
    private DbSetup dbSetup;

    @BeforeEach
    void setUp() {
        // Setup in-memory database with unique name for each test
        var ds = new org.h2.jdbcx.JdbcDataSource();
        ds.setURL("jdbc:h2:mem:tdgtest" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");
        
        ctx = DSL.using(ds, SQLDialect.H2);
        dbSetup = new DbSetup();
        
        // Create schema
        dbSetup.up(ctx);
        dbSetup.createIndexes(ctx);
        
        // Create gateway - single instance for all operations
        accountGateway = new AccountGateway(ctx);
        
        // Insert test data
        insertTestData();
    }

    private void insertTestData() {
        // Insert multiple accounts for testing
        accountGateway.insert(1L, new BigDecimal("1000.00"), "USD", "SAVING", 
                            new BigDecimal("0.025"), null, null);
        accountGateway.insert(2L, new BigDecimal("500.00"), "USD", "CHECKING", 
                            null, new BigDecimal("300.00"), new BigDecimal("0.18"));
        accountGateway.insert(3L, new BigDecimal("2500.00"), "EUR", "SAVING", 
                            new BigDecimal("0.02"), null, null);
        accountGateway.insert(4L, new BigDecimal("750.00"), "USD", "CHECKING", 
                            null, new BigDecimal("500.00"), new BigDecimal("0.15"));
        accountGateway.insert(5L, new BigDecimal("0.00"), "USD", "SAVING", 
                            new BigDecimal("0.025"), null, null);
    }

    @Test
    @DisplayName("Should find account by ID returning RecordSet")
    void shouldFindAccountByIdReturningRecordSet() {
        // When - Table Data Gateway returns Result (RecordSet)
        Result<AccountsRecord> result = accountGateway.find(1L);

        // Then - Result contains zero or one record
        assertThat(result).hasSize(1);
        AccountsRecord record = result.get(0);
        assertThat(record.getId()).isEqualTo(1L);
        assertThat(record.getBaselineBalance()).isEqualTo(new BigDecimal("1000.00"));
        assertThat(record.getAccountType()).isEqualTo("SAVING");
    }

    @Test
    @DisplayName("Should find all accounts returning RecordSet")
    void shouldFindAllAccountsReturningRecordSet() {
        // When - Table Data Gateway returns all records
        Result<AccountsRecord> result = accountGateway.findAll();

        // Then - Result contains all records
        assertThat(result).hasSize(5);
        assertThat(result).allMatch(record -> record.getId() != null);
    }

    @Test
    @DisplayName("Should find accounts by type returning RecordSet")
    void shouldFindAccountsByTypeReturningRecordSet() {
        // When - Query for specific account type
        Result<AccountsRecord> savingAccounts = accountGateway.findByAccountType("SAVING");
        Result<AccountsRecord> checkingAccounts = accountGateway.findByAccountType("CHECKING");

        // Then - Results are filtered by type
        assertThat(savingAccounts).hasSize(3);
        assertThat(savingAccounts).allMatch(r -> "SAVING".equals(r.getAccountType()));
        
        assertThat(checkingAccounts).hasSize(2);
        assertThat(checkingAccounts).allMatch(r -> "CHECKING".equals(r.getAccountType()));
    }

    @Test
    @DisplayName("Should find accounts by balance range")
    void shouldFindAccountsByBalanceRange() {
        // When - Query with balance criteria
        Result<AccountsRecord> result = accountGateway.findByBalanceBetween(
            new BigDecimal("500.00"), 
            new BigDecimal("1500.00")
        );

        // Then - Results match criteria
        assertThat(result).hasSize(3);
        assertThat(result).allMatch(r -> 
            r.getBaselineBalance().compareTo(new BigDecimal("500.00")) >= 0 &&
            r.getBaselineBalance().compareTo(new BigDecimal("1500.00")) <= 0
        );
    }

    @Test
    @DisplayName("Should perform batch update on multiple records")
    void shouldPerformBatchUpdateOnMultipleRecords() {
        // Given - New interest rate for all savings accounts
        BigDecimal newInterestRate = new BigDecimal("0.03");

        // When - Batch update operation
        int updated = accountGateway.updateInterestRateForAllSavings(newInterestRate);

        // Then - All savings accounts updated
        assertThat(updated).isEqualTo(3);
        
        Result<AccountsRecord> savingAccounts = accountGateway.findByAccountType("SAVING");
        assertThat(savingAccounts).allMatch(r -> 
            r.getAnnualInterestRate().compareTo(newInterestRate) == 0
        );
    }

    @Test
    @DisplayName("Should calculate aggregate values")
    void shouldCalculateAggregateValues() {
        // When - Calculate aggregates
        int totalCount = accountGateway.count();
        int savingCount = accountGateway.countByType("SAVING");
        BigDecimal usdTotal = accountGateway.sumBalancesByCurrency("USD");

        // Then - Aggregates are correct
        assertThat(totalCount).isEqualTo(5);
        assertThat(savingCount).isEqualTo(3);
        assertThat(usdTotal).isEqualTo(new BigDecimal("2250.00")); // 1000 + 500 + 750 + 0
    }

    @Test
    @DisplayName("Should handle empty result sets")
    void shouldHandleEmptyResultSets() {
        // When - Query for non-existent data
        Result<AccountsRecord> result = accountGateway.find(999L);

        // Then - Empty result set
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should insert using AccountsRecord")
    void shouldInsertUsingAccountsRecord() {
        // Given - Create a new record object
        AccountsRecord newRecord = ctx.newRecord(pofeaa.combination.transactionscript.generated.Tables.ACCOUNTS);
        newRecord.setId(6L);
        newRecord.setBaselineBalance(new BigDecimal("1500.00"));
        newRecord.setCurrency("GBP");
        newRecord.setAccountType("SAVING");
        newRecord.setAnnualInterestRate(new BigDecimal("0.015"));

        // When - Insert using record
        int inserted = accountGateway.insert(newRecord);

        // Then - Record is inserted
        assertThat(inserted).isEqualTo(1);
        Result<AccountsRecord> result = accountGateway.find(6L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCurrency()).isEqualTo("GBP");
    }

    @Test
    @DisplayName("Should delete accounts with zero balance")
    void shouldDeleteAccountsWithZeroBalance() {
        // When - Batch delete operation
        int deleted = accountGateway.deleteAccountsWithZeroBalance();

        // Then - Only zero balance accounts deleted
        assertThat(deleted).isEqualTo(1);
        assertThat(accountGateway.count()).isEqualTo(4);
        
        Result<AccountsRecord> result = accountGateway.find(5L);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should find checking accounts with overdraft")
    void shouldFindCheckingAccountsWithOverdraft() {
        // When - Complex query for specific criteria
        Result<AccountsRecord> result = accountGateway.findCheckingAccountsWithOverdraft();

        // Then - Results match complex criteria
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(r -> 
            "CHECKING".equals(r.getAccountType()) &&
            r.getOverdraftLimit() != null &&
            r.getOverdraftLimit().compareTo(BigDecimal.ZERO) > 0
        );
        
        // Results are ordered by overdraft limit descending
        assertThat(result.get(0).getOverdraftLimit())
            .isGreaterThan(result.get(1).getOverdraftLimit());
    }

    @Test
    @DisplayName("Should demonstrate working with RecordSet iteration")
    void shouldDemonstrateWorkingWithRecordSetIteration() {
        // When - Get all accounts
        Result<AccountsRecord> allAccounts = accountGateway.findAll();

        // Then - Can iterate and process records
        BigDecimal totalBalance = BigDecimal.ZERO;
        for (AccountsRecord record : allAccounts) {
            if ("USD".equals(record.getCurrency())) {
                totalBalance = totalBalance.add(record.getBaselineBalance());
            }
        }
        
        assertThat(totalBalance).isEqualTo(new BigDecimal("2250.00"));  // 1000 + 500 + 750 + 0
    }

    @Test
    @DisplayName("Should generate next ID for new accounts")
    void shouldGenerateNextIdForNewAccounts() {
        // When - Get next available ID
        Long nextId = accountGateway.getNextId();

        // Then - Next ID is correct
        assertThat(nextId).isEqualTo(6L);
    }
}