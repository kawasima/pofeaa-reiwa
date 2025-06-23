package pofeaa.combination.domain.repository;

import org.h2.jdbcx.JdbcDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pofeaa.combination.DbSetup;
import pofeaa.combination.domain.model.*;
import pofeaa.original.base.money.Money;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CheckingAccount Integration Tests with DbSetup")
class CheckingAccountIntegrationTest {
    
    private DSLContext ctx;
    private DbSetup dbSetup;
    private AccountMapper accountMapper;
    private ActivityMapper activityMapper;
    
    @BeforeEach
    void setUp() {
        // Setup in-memory database
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:checking_test;DB_CLOSE_DELAY=-1");
        
        ctx = DSL.using(ds, SQLDialect.H2);
        dbSetup = new DbSetup();
        
        // Create schema using DbSetup
        dbSetup.up(ctx);
        dbSetup.createIndexes(ctx);
        
        // Initialize mappers
        activityMapper = new ActivityMapper(ctx);
        accountMapper = new AccountMapper(ctx, activityMapper);
    }
    
    @AfterEach
    void tearDown() {
        if (dbSetup != null && ctx != null) {
            dbSetup.down(ctx);
        }
    }
    
    @Test
    @DisplayName("Should persist and retrieve CheckingAccount correctly")
    void shouldPersistAndRetrieveCheckingAccountCorrectly() {
        // Given
        Identity accountId = Identity.of(100L);
        Money initialBalance = new Money(750.00, Currency.getInstance(Locale.US));
        ActivityWindow emptyWindow = new ActivityWindow();
        
        CheckingAccount originalAccount = CheckingAccount.of(accountId, initialBalance, emptyWindow);
        
        // When
        accountMapper.insert(originalAccount);
        Account retrievedAccount = accountMapper.find(100L);
        
        // Then
        assertThat(retrievedAccount).isNotNull();
        assertThat(retrievedAccount).isInstanceOf(CheckingAccount.class);
        
        CheckingAccount retrievedChecking = (CheckingAccount) retrievedAccount;
        assertThat(retrievedChecking.getId().asLong()).isEqualTo(100L);
        assertThat(retrievedChecking.getBaselineBalance().amount()).isEqualTo(initialBalance.amount());
        assertThat(retrievedChecking.getMinimumBalance().amount()).isEqualTo(new Money(25.00, Currency.getInstance(Locale.US)).amount());
        assertThat(retrievedChecking.getFreeTransactionsPerMonth()).isEqualTo(20);
    }
    
    @Test
    @DisplayName("Should persist CheckingAccount with activities")
    void shouldPersistCheckingAccountWithActivities() {
        // Given
        Identity accountId = Identity.of(101L);
        Money initialBalance = new Money(500.00, Currency.getInstance(Locale.US));
        CheckingAccount account = CheckingAccount.of(accountId, initialBalance, new ActivityWindow());
        
        // Perform some transactions
        Identity targetAccountId = Identity.of(102L);
        account.withdraw(new Money(100.00, Currency.getInstance(Locale.US)), targetAccountId);
        account.deposit(new Money(50.00, Currency.getInstance(Locale.US)), Identity.of(103L));
        
        // When
        accountMapper.insert(account);
        Account retrievedAccount = accountMapper.find(101L);
        
        // Then
        assertThat(retrievedAccount).isInstanceOf(CheckingAccount.class);
        CheckingAccount retrievedChecking = (CheckingAccount) retrievedAccount;
        
        assertThat(retrievedChecking.getActivityWindow().getActivities()).hasSize(2);
        assertThat(retrievedChecking.getTransactionsThisMonth()).isEqualTo(2);
        
        // Check calculated balance
        Money expectedBalance = initialBalance
            .subtract(new Money(100.00, Currency.getInstance(Locale.US)))
            .add(new Money(50.00, Currency.getInstance(Locale.US)));
        assertThat(retrievedChecking.calculateBalance().amount()).isEqualTo(expectedBalance.amount());
    }
    
    @Test
    @DisplayName("Should handle transaction fees in database operations")
    void shouldHandleTransactionFeesInDatabaseOperations() {
        // Given
        Identity accountId = Identity.of(103L);
        Money initialBalance = new Money(1000.00, Currency.getInstance(Locale.US));
        CheckingAccount account = CheckingAccount.of(accountId, initialBalance, new ActivityWindow());
        
        // Perform enough transactions to trigger fees (21 transactions)
        for (int i = 0; i < 21; i++) {
            account.withdraw(new Money(10.00, Currency.getInstance(Locale.US)), Identity.of(200L + i));
        }
        
        // When
        accountMapper.insert(account);
        Account retrievedAccount = accountMapper.find(103L);
        
        // Then
        assertThat(retrievedAccount).isInstanceOf(CheckingAccount.class);
        CheckingAccount retrievedChecking = (CheckingAccount) retrievedAccount;
        
        // Should have 21 withdrawals + 1 transaction fee = 22 activities
        assertThat(retrievedChecking.getActivityWindow().getActivities()).hasSize(22);
        assertThat(retrievedChecking.getTransactionsThisMonth()).isEqualTo(21); // Fees don't count as transactions
        
        Money expectedFees = new Money(2.50, Currency.getInstance(Locale.US));
        assertThat(retrievedChecking.getFeesThisMonth().amount()).isEqualTo(expectedFees.amount());
    }
    
    @Test
    @DisplayName("Should update CheckingAccount correctly")
    void shouldUpdateCheckingAccountCorrectly() {
        // Given - Create and insert initial account
        Identity accountId = Identity.of(104L);
        Money initialBalance = new Money(300.00, Currency.getInstance(Locale.US));
        CheckingAccount account = CheckingAccount.of(accountId, initialBalance, new ActivityWindow());
        accountMapper.insert(account);
        
        // When - Retrieve, modify, and update
        Account retrievedAccount = accountMapper.find(104L);
        CheckingAccount checkingAccount = (CheckingAccount) retrievedAccount;
        checkingAccount.deposit(new Money(200.00, Currency.getInstance(Locale.US)), Identity.of(105L));
        
        accountMapper.update(checkingAccount);
        Account updatedAccount = accountMapper.find(104L);
        
        // Then
        assertThat(updatedAccount).isInstanceOf(CheckingAccount.class);
        CheckingAccount updatedChecking = (CheckingAccount) updatedAccount;
        
        Money expectedBalance = initialBalance.add(new Money(200.00, Currency.getInstance(Locale.US)));
        assertThat(updatedChecking.calculateBalance().amount()).isEqualTo(expectedBalance.amount());
        assertThat(updatedChecking.getActivityWindow().getActivities()).hasSize(1);
    }
    
    @Test
    @DisplayName("Should delete CheckingAccount and all activities")
    void shouldDeleteCheckingAccountAndAllActivities() {
        // Given
        Identity accountId = Identity.of(105L);
        Money initialBalance = new Money(400.00, Currency.getInstance(Locale.US));
        CheckingAccount account = CheckingAccount.of(accountId, initialBalance, new ActivityWindow());
        
        // Add some activities
        account.withdraw(new Money(50.00, Currency.getInstance(Locale.US)), Identity.of(106L));
        account.deposit(new Money(25.00, Currency.getInstance(Locale.US)), Identity.of(107L));
        
        accountMapper.insert(account);
        
        // Verify account exists
        Account existingAccount = accountMapper.find(105L);
        assertThat(existingAccount).isNotNull();
        assertThat(existingAccount.getActivityWindow().getActivities()).hasSize(2);
        
        // When
        accountMapper.delete(existingAccount);
        
        // Then
        Account deletedAccount = accountMapper.find(105L);
        assertThat(deletedAccount).isNull();
        
        // Verify activities are also deleted (due to foreign key cascade)
        assertThat(activityMapper.findByOwnerAccountId(105L)).isEmpty();
    }
    
    @Test
    @DisplayName("Should distinguish between CheckingAccount and SavingAccount in database")
    void shouldDistinguishBetweenCheckingAccountAndSavingAccountInDatabase() {
        // Given
        Identity checkingId = Identity.of(200L);
        Identity savingId = Identity.of(201L);
        Money balance = new Money(500.00, Currency.getInstance(Locale.US));
        
        CheckingAccount checkingAccount = CheckingAccount.of(checkingId, balance, new ActivityWindow());
        SavingAccount savingAccount = SavingAccount.of(savingId, balance, new ActivityWindow());
        
        // When
        accountMapper.insert(checkingAccount);
        accountMapper.insert(savingAccount);
        
        Account retrievedChecking = accountMapper.find(200L);
        Account retrievedSaving = accountMapper.find(201L);
        
        // Then
        assertThat(retrievedChecking).isInstanceOf(CheckingAccount.class);
        assertThat(retrievedSaving).isInstanceOf(SavingAccount.class);
        
        CheckingAccount checking = (CheckingAccount) retrievedChecking;
        SavingAccount saving = (SavingAccount) retrievedSaving;
        
        // Verify different business rules
        assertThat(checking.getMinimumBalance().amount()).isLessThan(saving.getMinimumBalance().amount());
        assertThat(checking.getFreeTransactionsPerMonth()).isGreaterThan(0);
        assertThat(saving.getMaxWithdrawalsPerMonth()).isEqualTo(6);
    }
    
    @Test
    @DisplayName("Should handle overdraft scenarios correctly in database")
    void shouldHandleOverdraftScenariosCorrectlyInDatabase() {
        // Given
        Identity accountId = Identity.of(106L);
        Money lowBalance = new Money(30.00, Currency.getInstance(Locale.US));
        CheckingAccount account = CheckingAccount.of(accountId, lowBalance, new ActivityWindow());
        
        // Trigger overdraft
        account.withdraw(new Money(100.00, Currency.getInstance(Locale.US)), Identity.of(107L));
        
        // When
        accountMapper.insert(account);
        Account retrievedAccount = accountMapper.find(106L);
        
        // Then
        assertThat(retrievedAccount).isInstanceOf(CheckingAccount.class);
        CheckingAccount retrievedChecking = (CheckingAccount) retrievedAccount;
        
        // Should have withdrawal + overdraft fee
        assertThat(retrievedChecking.getActivityWindow().getActivities()).hasSize(2);
        
        // Verify negative balance
        Money expectedBalance = lowBalance
            .subtract(new Money(100.00, Currency.getInstance(Locale.US)))
            .subtract(new Money(30.00, Currency.getInstance(Locale.US))); // overdraft fee
        assertThat(retrievedChecking.calculateBalance().amount()).isEqualTo(expectedBalance.amount());
        
        // Verify fees
        Money expectedFees = new Money(30.00, Currency.getInstance(Locale.US));
        assertThat(retrievedChecking.getFeesThisMonth().amount()).isEqualTo(expectedFees.amount());
    }
    
    @Test
    @DisplayName("Should work with DbSetup sample data")
    void shouldWorkWithDbSetupSampleData() {
        // Given - Insert sample data using DbSetup
        dbSetup.insertSampleData(ctx);
        
        // When - Retrieve the sample checking account
        Account account = accountMapper.find(2L); // Sample checking account has ID 2
        
        // Then
        assertThat(account).isNotNull();
        assertThat(account).isInstanceOf(CheckingAccount.class);
        
        CheckingAccount checkingAccount = (CheckingAccount) account;
        assertThat(checkingAccount.getBaselineBalance().amount()).isEqualTo(new BigDecimal("500.00"));
        assertThat(checkingAccount.calculateBalance().amount()).isEqualTo(new BigDecimal("500.00"));
    }
}