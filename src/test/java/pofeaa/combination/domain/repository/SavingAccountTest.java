package pofeaa.combination.domain.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pofeaa.combination.domain.model.Activity;
import pofeaa.combination.domain.model.ActivityWindow;
import pofeaa.combination.domain.model.Identity;
import pofeaa.combination.domain.model.SavingAccount;
import pofeaa.original.base.money.Money;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.*;

class SavingAccountTest {

    private static final Currency USD = Currency.getInstance(Locale.US);
    private Identity accountId;
    private Money baselineBalance;
    private ActivityWindow activityWindow;

    @BeforeEach
    void setUp() {
        accountId = Identity.of(1L);
        baselineBalance = new Money(500.00, USD);
        activityWindow = new ActivityWindow(new ArrayList<>());
    }

    @Nested
    @DisplayName("Account Creation Tests")
    class AccountCreationTests {
        
        @Test
        @DisplayName("Should create saving account with default settings")
        void shouldCreateSavingAccountWithDefaultSettings() {
            SavingAccount account = SavingAccount.of(accountId, baselineBalance, activityWindow);
            
            assertThat(account.getId()).isEqualTo(accountId);
            assertThat(account.getBaselineBalance()).isEqualTo(baselineBalance);
            assertThat(account.getActivityWindow()).isEqualTo(activityWindow);
            assertThat(account.getMinimumBalance().amount()).isEqualTo(new Money(100.00, USD).amount());
            assertThat(account.getMaxWithdrawalsPerMonth()).isEqualTo(6);
            assertThat(account.hasOverdraftProtection()).isTrue();
        }

        @Test
        @DisplayName("Should create saving account with custom interest rate")
        void shouldCreateSavingAccountWithCustomInterestRate() {
            java.math.BigDecimal customInterestRate = new java.math.BigDecimal("0.02"); // 2%
            
            SavingAccount account = SavingAccount.of(accountId, baselineBalance, activityWindow,
                    customInterestRate);
            
            assertThat(account.getAnnualInterestRate()).isEqualTo(customInterestRate);
            // These are now constants
            assertThat(account.getMinimumBalance().amount()).isEqualTo(new Money(100.00, USD).amount());
            assertThat(account.getMaxWithdrawalsPerMonth()).isEqualTo(6);
            assertThat(account.hasOverdraftProtection()).isTrue();
        }
    }

    @Nested
    @DisplayName("Minimum Balance Tests")
    class MinimumBalanceTests {
        
        @Test
        @DisplayName("Should calculate available balance correctly")
        void shouldCalculateAvailableBalanceCorrectly() {
            SavingAccount account = SavingAccount.of(accountId, baselineBalance, activityWindow);
            
            // Available balance = 500 - 100 (minimum) = 400
            Money availableBalance = account.getAvailableBalance();
            assertThat(availableBalance.amount()).isEqualTo(new Money(400.00, USD).amount());
        }

        @Test
        @DisplayName("Should return zero available balance when below minimum")
        void shouldReturnZeroAvailableBalanceWhenBelowMinimum() {
            Money lowBalance = new Money(50.00, USD);
            SavingAccount account = SavingAccount.of(accountId, lowBalance, activityWindow);
            
            Money availableBalance = account.getAvailableBalance();
            assertThat(availableBalance.amount()).isEqualTo(new Money(0.00, USD).amount());
        }

        @Test
        @DisplayName("Should detect when account is below minimum balance")
        void shouldDetectWhenAccountIsBelowMinimumBalance() {
            Money lowBalance = new Money(50.00, USD);
            SavingAccount account = SavingAccount.of(accountId, lowBalance, activityWindow);
            
            assertThat(account.isBelowMinimumBalance()).isTrue();
        }

        @Test
        @DisplayName("Should detect when account is above minimum balance")
        void shouldDetectWhenAccountIsAboveMinimumBalance() {
            SavingAccount account = SavingAccount.of(accountId, baselineBalance, activityWindow);
            
            assertThat(account.isBelowMinimumBalance()).isFalse();
        }
    }

    @Nested
    @DisplayName("Withdrawal Limit Tests")
    class WithdrawalLimitTests {
        
        @Test
        @DisplayName("Should allow withdrawal when within limits")
        void shouldAllowWithdrawalWhenWithinLimits() {
            SavingAccount account = SavingAccount.of(accountId, baselineBalance, activityWindow);
            Money withdrawalAmount = new Money(50.00, USD);
            Identity targetAccountId = Identity.of(2L);
            
            boolean result = account.withdraw(withdrawalAmount, targetAccountId);
            
            assertThat(result).isTrue();
            assertThat(account.getWithdrawalsThisMonth()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should prevent withdrawal when monthly limit exceeded")
        void shouldPreventWithdrawalWhenMonthlyLimitExceeded() {
            // Create an account with activities representing 6 withdrawals this month
            List<Activity> activities = new ArrayList<>();
            LocalDateTime thisMonth = LocalDateTime.now().minusDays(15);
            
            for (int i = 0; i < 6; i++) {
                activities.add(Activity.of(
                    Identity.of((long) (i + 1)),
                    accountId,
                    accountId, // source = this account
                    Identity.of(2L), // target = different account
                    thisMonth.plusDays(i),
                    new Money(10.00, USD)
                ));
            }
            
            ActivityWindow windowWithWithdrawals = new ActivityWindow(activities);
            SavingAccount account = SavingAccount.of(accountId, baselineBalance, windowWithWithdrawals);
            
            assertThat(account.getWithdrawalsThisMonth()).isEqualTo(6);
            
            Money withdrawalAmount = new Money(50.00, USD);
            Identity targetAccountId = Identity.of(3L);
            
            boolean result = account.withdraw(withdrawalAmount, targetAccountId);
            
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should count only withdrawals from this month")
        void shouldCountOnlyWithdrawalsFromThisMonth() {
            List<Activity> activities = new ArrayList<>();
            LocalDateTime lastMonth = LocalDateTime.now().minusMonths(2);
            LocalDateTime thisMonth = LocalDateTime.now().minusDays(5);
            
            // Add old withdrawals (should not count)
            for (int i = 0; i < 5; i++) {
                activities.add(Activity.of(
                    Identity.of((long) (i + 1)),
                    accountId,
                    accountId,
                    Identity.of(2L),
                    lastMonth.plusDays(i),
                    new Money(10.00, USD)
                ));
            }
            
            // Add recent withdrawals (should count)
            for (int i = 0; i < 3; i++) {
                activities.add(Activity.of(
                    Identity.of((long) (i + 6)),
                    accountId,
                    accountId,
                    Identity.of(2L),
                    thisMonth.plusDays(i),
                    new Money(10.00, USD)
                ));
            }
            
            ActivityWindow windowWithActivities = new ActivityWindow(activities);
            SavingAccount account = SavingAccount.of(accountId, baselineBalance, windowWithActivities);
            
            assertThat(account.getWithdrawalsThisMonth()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Overdraft Protection Tests")
    class OverdraftProtectionTests {
        
        @Test
        @DisplayName("Should allow overdraft with protection and charge fee")
        void shouldAllowOverdraftWithProtectionAndChargeFee() {
            Money lowBalance = new Money(50.00, USD);
            SavingAccount account = SavingAccount.of(accountId, lowBalance, activityWindow);
            
            Money withdrawalAmount = new Money(100.00, USD); // More than balance
            Identity targetAccountId = Identity.of(2L);
            
            boolean result = account.withdraw(withdrawalAmount, targetAccountId);
            
            assertThat(result).isTrue();
            
            // Should have both withdrawal and overdraft fee activities
            List<Activity> activities = account.getActivityWindow().getActivities();
            assertThat(activities).hasSize(2);
            
            // Check that overdraft fee was applied
            boolean hasOverdraftFee = activities.stream()
                .anyMatch(activity -> activity.getMoney().amount().equals(new Money(35.00, USD).amount()));
            assertThat(hasOverdraftFee).isTrue();
        }

        @Test
        @DisplayName("Should allow overdraft with protection and charge fee")
        void shouldAllowOverdraftWithProtectionForLargeAmounts() {
            Money lowBalance = new Money(50.00, USD);
            
            // Create account with default settings (overdraft protection is always enabled)
            SavingAccount account = SavingAccount.of(accountId, lowBalance, activityWindow);
            
            Money withdrawalAmount = new Money(100.00, USD); // More than balance but within overdraft limit
            Identity targetAccountId = Identity.of(2L);
            
            boolean result = account.withdraw(withdrawalAmount, targetAccountId);
            
            assertThat(result).isTrue();
            assertThat(account.getActivityWindow().getActivities()).hasSize(2); // withdrawal + overdraft fee
        }

        @Test
        @DisplayName("Should respect overdraft limit even with protection")
        void shouldRespectOverdraftLimitEvenWithProtection() {
            Money lowBalance = new Money(50.00, USD);
            SavingAccount account = SavingAccount.of(accountId, lowBalance, activityWindow);
            
            // Try to withdraw way more than overdraft limit
            Money excessiveWithdrawal = new Money(1000.00, USD);
            Identity targetAccountId = Identity.of(2L);
            
            boolean result = account.withdraw(excessiveWithdrawal, targetAccountId);
            
            assertThat(result).isFalse();
            assertThat(account.getActivityWindow().getActivities()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Deposit Tests")
    class DepositTests {
        
        @Test
        @DisplayName("Should allow deposits without restrictions")
        void shouldAllowDepositsWithoutRestrictions() {
            SavingAccount account = SavingAccount.of(accountId, baselineBalance, activityWindow);
            Money depositAmount = new Money(200.00, USD);
            Identity sourceAccountId = Identity.of(2L);
            
            boolean result = account.deposit(depositAmount, sourceAccountId);
            
            assertThat(result).isTrue();
            assertThat(account.getActivityWindow().getActivities()).hasSize(1);
            
            Money newBalance = account.calculateBalance();
            assertThat(newBalance.amount()).isEqualTo(new Money(700.00, USD).amount());
        }
    }
}