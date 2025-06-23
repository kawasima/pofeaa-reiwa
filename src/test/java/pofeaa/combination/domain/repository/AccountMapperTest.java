package pofeaa.combination.domain.repository;

import org.h2.jdbcx.JdbcDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import pofeaa.combination.DbSetup;
import pofeaa.combination.domain.model.*;
import pofeaa.original.base.money.Money;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.*;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

class AccountMapperTest {
    
    private DSLContext ctx;
    private DbSetup dbSetup;
    private AccountMapper accountMapper;
    private ActivityMapper activityMapper;
    
    @BeforeEach
    void setUp() {
        // Setup in-memory database
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        
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
    @DisplayName("Should find account with activities")
    void shouldFindAccountWithActivities() {
        // Given
        Long accountId = 1L;
        BigDecimal baselineBalance = new BigDecimal("1000.00");
        Currency usd = Currency.getInstance("USD");
        
        // Insert account
        ctx.insertInto(table("accounts"))
            .set(field("id"), accountId)
            .set(field("baseline_balance"), baselineBalance)
            .set(field("currency"), usd.getCurrencyCode())
            .execute();
            
        // Insert activities
        ctx.insertInto(table("activities"))
            .set(field("id"), 1L)
            .set(field("owner_account_id"), accountId)
            .set(field("source_account_id"), accountId)
            .set(field("target_account_id"), 2L)
            .set(field("timestamp"), LocalDateTime.now())
            .set(field("amount"), new BigDecimal("100.00"))
            .set(field("currency"), usd.getCurrencyCode())
            .execute();
            
        ctx.insertInto(table("activities"))
            .set(field("id"), 2L)
            .set(field("owner_account_id"), accountId)
            .set(field("source_account_id"), 3L)
            .set(field("target_account_id"), accountId)
            .set(field("timestamp"), LocalDateTime.now().plusHours(1))
            .set(field("amount"), new BigDecimal("50.00"))
            .set(field("currency"), usd.getCurrencyCode())
            .execute();
        
        // When
        Account account = accountMapper.find(accountId);
        
        // Then
        assertThat(account).isNotNull();
        assertThat(account.getId().asLong()).isEqualTo(accountId);
        assertThat(account.getBaselineBalance().amount()).isEqualTo(baselineBalance);
        assertThat(account.getActivityWindow().getActivities()).hasSize(2);
    }
    
    @Test
    @DisplayName("Should insert account with activities")
    void shouldInsertAccountWithActivities() {
        // Given
        Long accountId = 1L;
        Identity id = Identity.of(accountId);
        Money baselineBalance = new Money(500.00, Currency.getInstance(Locale.US));
        
        LocalDateTime now = LocalDateTime.now();
        Activity activity1 = Activity.of(
            Identity.of(1L),
            id,
            id,
            Identity.of(2L),
            now,
            new Money(100.00, Currency.getInstance(Locale.US))
        );
        
        Activity activity2 = Activity.of(
            Identity.of(2L),
            id,
            Identity.of(3L),
            id,
            now.plusHours(1),
            new Money(200.00, Currency.getInstance(Locale.US))
        );
        
        List<Activity> activities = List.of(activity1, activity2);
        ActivityWindow activityWindow = new ActivityWindow(activities);
        Account account = SavingAccount.of(id, baselineBalance, activityWindow);
        
        // When
        accountMapper.insert(account);
        
        // Then
        var savedAccount = ctx.select()
            .from(table("accounts"))
            .where(field("id").eq(accountId))
            .fetchOne();
            
        assertThat(savedAccount).isNotNull();
        assertThat(savedAccount.getValue("BASELINE_BALANCE", BigDecimal.class))
            .isEqualTo(new BigDecimal("500.00"));
        assertThat(savedAccount.getValue("CURRENCY", String.class))
            .isEqualTo("USD");
            
        var savedActivities = ctx.select()
            .from(table("activities"))
            .where(field("owner_account_id").eq(accountId))
            .fetch();
            
        assertThat(savedActivities).hasSize(2);
    }
    
    @Test
    @DisplayName("Should update account")
    void shouldUpdateAccount() {
        // Given
        Long accountId = 1L;
        BigDecimal initialBalance = new BigDecimal("1000.00");
        Currency usd = Currency.getInstance("USD");
        
        // Insert initial account
        ctx.insertInto(table("accounts"))
            .set(field("id"), accountId)
            .set(field("baseline_balance"), initialBalance)
            .set(field("currency"), usd.getCurrencyCode())
            .execute();
            
        // Create updated account
        Identity id = Identity.of(accountId);
        Money newBalance = new Money(2000.00, usd);
        ActivityWindow emptyWindow = new ActivityWindow();
        Account updatedAccount = SavingAccount.of(id, newBalance, emptyWindow);
        
        // When
        accountMapper.update(updatedAccount);
        
        // Then
        var savedAccount = ctx.select()
            .from(table("accounts"))
            .where(field("id").eq(accountId))
            .fetchOne();
            
        assertThat(savedAccount.getValue("BASELINE_BALANCE", BigDecimal.class))
            .isEqualTo(new BigDecimal("2000.00"));
    }
    
    @Test
    @DisplayName("Should delete account and its activities")
    void shouldDeleteAccountAndActivities() {
        // Given
        Long accountId = 1L;
        
        // Insert account and activity
        ctx.insertInto(table("accounts"))
            .set(field("id"), accountId)
            .set(field("baseline_balance"), new BigDecimal("1000.00"))
            .set(field("currency"), "USD")
            .execute();
            
        ctx.insertInto(table("activities"))
            .set(field("id"), 1L)
            .set(field("owner_account_id"), accountId)
            .set(field("source_account_id"), accountId)
            .set(field("target_account_id"), 2L)
            .set(field("timestamp"), LocalDateTime.now())
            .set(field("amount"), new BigDecimal("100.00"))
            .set(field("currency"), "USD")
            .execute();
            
        Identity id = Identity.of(accountId);
        Money balance = new Money(1000.00, Currency.getInstance("USD"));
        ActivityWindow window = new ActivityWindow();
        Account account = SavingAccount.of(id, balance, window);
        
        // When
        accountMapper.delete(account);
        
        // Then
        var accountCount = ctx.selectCount()
            .from(table("accounts"))
            .where(field("id").eq(accountId))
            .fetchOne()
            .value1();
            
        var activityCount = ctx.selectCount()
            .from(table("activities"))
            .where(field("owner_account_id").eq(accountId))
            .fetchOne()
            .value1();
            
        assertThat(accountCount).isEqualTo(0);
        assertThat(activityCount).isEqualTo(0);
    }
    
    @Test
    @DisplayName("Should return null when account not found")
    void shouldReturnNullWhenAccountNotFound() {
        // When
        Account account = accountMapper.find(999L);
        
        // Then
        assertThat(account).isNull();
    }
    
    @Test
    @DisplayName("Should throw exception for null account ID")
    void shouldThrowExceptionForNullAccountId() {
        // When & Then
        assertThatThrownBy(() -> accountMapper.find(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Account ID cannot be null");
    }
}