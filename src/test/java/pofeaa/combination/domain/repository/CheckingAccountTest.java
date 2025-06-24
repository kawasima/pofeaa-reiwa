package pofeaa.combination.domain.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pofeaa.combination.domain.model.*;
import pofeaa.original.base.money.Money;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.*;

class CheckingAccountTest {

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
        @DisplayName("Should create checking account with default settings")
        void shouldCreateCheckingAccountWithDefaultSettings() {
            CheckingAccount account = CheckingAccount.of(accountId, baselineBalance, activityWindow);
            
            assertThat(account.getId()).isEqualTo(accountId);
            assertThat(account.getBaselineBalance()).isEqualTo(baselineBalance);
            assertThat(account.getActivityWindow()).isEqualTo(activityWindow);
            assertThat(account.getMinimumBalance().amount()).isEqualTo(new Money(25.00, USD).amount());
            assertThat(account.getFreeTransactionsPerMonth()).isEqualTo(20);
            assertThat(account.hasOverdraftProtection()).isTrue();
            assertThat(account.getTransactionFee().amount()).isEqualTo(new Money(2.50, USD).amount());
        }

        @Test
        @DisplayName("Should create checking account with custom overdraft settings")
        void shouldCreateCheckingAccountWithCustomOverdraftSettings() {
            Money customOverdraftLimit = new Money(500.00, USD);
            java.math.BigDecimal customOverdraftRate = new java.math.BigDecimal("0.15");
            
            CheckingAccount account = CheckingAccount.of(accountId, baselineBalance, activityWindow,
                    customOverdraftLimit, customOverdraftRate);
            
            assertThat(account.getOverdraftLimit()).isEqualTo(customOverdraftLimit);
            assertThat(account.getOverdraftInterestRate()).isEqualTo(customOverdraftRate);
            // These are now constants
            assertThat(account.getMinimumBalance().amount()).isEqualTo(new Money(25.00, USD).amount());
            assertThat(account.hasOverdraftProtection()).isTrue();
            assertThat(account.getFreeTransactionsPerMonth()).isEqualTo(20);
            assertThat(account.getTransactionFee().amount()).isEqualTo(new Money(2.50, USD).amount());
        }
    }

    @Nested
    @DisplayName("Transaction Fee Tests")
    class TransactionFeeTests {
        
        @Test
        @DisplayName("Should not charge transaction fee within free limit")
        void shouldNotChargeTransactionFeeWithinFreeLimit() {
            CheckingAccount account = CheckingAccount.of(accountId, baselineBalance, activityWindow);
            Money withdrawalAmount = new Money(50.00, USD);
            Identity targetAccountId = Identity.of(2L);
            
            boolean result = account.withdraw(withdrawalAmount, targetAccountId);
            
            assertThat(result).isTrue();
            
            // Should only have withdrawal activity, no transaction fee
            List<Activity> activities = account.getActivityWindow().getActivities();
            assertThat(activities).hasSize(1);
            assertThat(activities.getFirst().getMoney()).isEqualTo(withdrawalAmount);
        }

        @Test
        @DisplayName("Should charge transaction fee after exceeding free limit")
        void shouldChargeTransactionFeeAfterExceedingFreeLimit() {
            // Create an account with activities representing 20 transactions this month
            List<Activity> activities = new ArrayList<>();
            LocalDateTime thisMonth = LocalDateTime.now().minusDays(15);
            
            for (int i = 0; i < 20; i++) {
                activities.add(Activity.of(
                    Identity.of((long) (i + 1)),
                    accountId,
                    accountId, // source = this account (withdrawal)
                    Identity.of(2L), // target = different account
                    thisMonth.plusDays(i / 2), // spread over half month
                    new Money(10.00, USD)
                ));
            }
            
            ActivityWindow windowWithTransactions = new ActivityWindow(activities);
            CheckingAccount account = CheckingAccount.of(accountId, baselineBalance, windowWithTransactions);
            
            assertThat(account.getTransactionsThisMonth()).isEqualTo(20);
            
            Money withdrawalAmount = new Money(50.00, USD);
            Identity targetAccountId = Identity.of(3L);
            
            boolean result = account.withdraw(withdrawalAmount, targetAccountId);
            
            assertThat(result).isTrue();
            
            // Should have both withdrawal and transaction fee
            List<Activity> newActivities = account.getActivityWindow().getActivities();
            assertThat(newActivities).hasSize(22); // 20 existing + withdrawal + fee
            
            // Check that transaction fee was applied
            boolean hasTransactionFee = newActivities.stream()
                .anyMatch(activity -> activity.getMoney().amount().equals(new Money(2.50, USD).amount()));
            assertThat(hasTransactionFee).isTrue();
        }

        @Test
        @DisplayName("Should charge transaction fee for deposits after limit")
        void shouldChargeTransactionFeeForDepositsAfterLimit() {
            // Create an account with 20 deposit transactions this month
            List<Activity> activities = new ArrayList<>();
            LocalDateTime thisMonth = LocalDateTime.now().minusDays(10);
            
            for (int i = 0; i < 20; i++) {
                activities.add(Activity.of(
                    Identity.of((long) (i + 1)),
                    accountId,
                    Identity.of(2L), // source = different account
                    accountId, // target = this account (deposit)
                    thisMonth.plusDays(i / 4),
                    new Money(25.00, USD)
                ));
            }
            
            ActivityWindow windowWithDeposits = new ActivityWindow(activities);
            CheckingAccount account = CheckingAccount.of(accountId, baselineBalance, windowWithDeposits);
            
            Money depositAmount = new Money(100.00, USD);
            Identity sourceAccountId = Identity.of(3L);
            
            boolean result = account.deposit(depositAmount, sourceAccountId);
            
            assertThat(result).isTrue();
            
            // Should have deposit and transaction fee
            List<Activity> newActivities = account.getActivityWindow().getActivities();
            assertThat(newActivities).hasSize(22); // 20 existing + deposit + fee
        }

        @Test
        @DisplayName("Should calculate fees correctly for current month")
        void shouldCalculateFeesCorrectlyForCurrentMonth() {
            CheckingAccount account = CheckingAccount.of(accountId, baselineBalance, activityWindow);
            
            // Perform 21 withdrawals to trigger fee on the last one
            for (int i = 0; i < 21; i++) {
                account.withdraw(new Money(10.00, USD), Identity.of(2L));
            }
            
            Money feesThisMonth = account.getFeesThisMonth();
            assertThat(feesThisMonth.amount()).isEqualTo(new Money(2.50, USD).amount()); // One transaction fee
        }
    }

    @Nested
    @DisplayName("Overdraft Tests")
    class OverdraftTests {
        
        @Test
        @DisplayName("Should allow larger overdraft than savings account")
        void shouldAllowLargerOverdraftThanSavingsAccount() {
            Money lowBalance = new Money(50.00, USD);
            CheckingAccount account = CheckingAccount.of(accountId, lowBalance, activityWindow);
            
            // Try to withdraw more than balance but within checking overdraft limit
            Money withdrawalAmount = new Money(800.00, USD); // Total overdraft would be $750
            Identity targetAccountId = Identity.of(2L);
            
            boolean result = account.withdraw(withdrawalAmount, targetAccountId);
            
            assertThat(result).isTrue();
            
            // Should have withdrawal and overdraft fee
            List<Activity> activities = account.getActivityWindow().getActivities();
            assertThat(activities).hasSize(2);
            
            // Check that overdraft fee was applied ($30 for checking vs $35 for savings)
            boolean hasOverdraftFee = activities.stream()
                .anyMatch(activity -> activity.getMoney().amount().equals(new Money(30.00, USD).amount()));
            assertThat(hasOverdraftFee).isTrue();
        }

        @Test
        @DisplayName("Should prevent excessive overdraft even with protection")
        void shouldPreventExcessiveOverdraftEvenWithProtection() {
            Money lowBalance = new Money(50.00, USD);
            CheckingAccount account = CheckingAccount.of(accountId, lowBalance, activityWindow);
            
            // Try to withdraw way more than overdraft limit ($1000)
            Money excessiveWithdrawal = new Money(1500.00, USD);
            Identity targetAccountId = Identity.of(2L);
            
            boolean result = account.withdraw(excessiveWithdrawal, targetAccountId);
            
            assertThat(result).isFalse();
            assertThat(account.getActivityWindow().getActivities()).isEmpty();
        }

        @Test
        @DisplayName("Should handle large withdrawals with overdraft protection")
        void shouldHandleLargeWithdrawalsWithOverdraftProtection() {
            Money lowBalance = new Money(50.00, USD);
            
            // Create account with default settings (overdraft protection is always enabled)
            CheckingAccount account = CheckingAccount.of(accountId, lowBalance, activityWindow);
            
            Money withdrawalAmount = new Money(100.00, USD); // More than balance but within overdraft
            Identity targetAccountId = Identity.of(2L);
            
            boolean result = account.withdraw(withdrawalAmount, targetAccountId);
            
            // Should succeed with overdraft protection
            assertThat(result).isTrue();
            assertThat(account.getActivityWindow().getActivities()).hasSize(2); // withdrawal + overdraft fee
        }
    }

    @Nested
    @DisplayName("Available Balance Tests")
    class AvailableBalanceTests {
        
        @Test
        @DisplayName("Should include overdraft limit in available balance with protection")
        void shouldIncludeOverdraftLimitInAvailableBalanceWithProtection() {
            CheckingAccount account = CheckingAccount.of(accountId, baselineBalance, activityWindow);
            
            Money availableBalance = account.getAvailableBalance();
            // $500 balance + $1000 overdraft limit = $1500 available
            assertThat(availableBalance.amount()).isEqualTo(new Money(1500.00, USD).amount());
        }

        @Test
        @DisplayName("Should include overdraft limit in available balance with protection")
        void shouldIncludeOverdraftLimitInAvailableBalanceByDefault() {
            CheckingAccount account = CheckingAccount.of(accountId, baselineBalance, activityWindow);
            
            Money availableBalance = account.getAvailableBalance();
            // $500 balance + $1000 overdraft limit = $1500 available (default overdraft protection)
            assertThat(availableBalance.amount()).isEqualTo(new Money(1500.00, USD).amount());
        }
    }

    @Nested
    @DisplayName("Transaction Counting Tests")
    class TransactionCountingTests {
        
        @Test
        @DisplayName("Should count both withdrawals and deposits as transactions")
        void shouldCountBothWithdrawalsAndDepositsAsTransactions() {
            CheckingAccount account = CheckingAccount.of(accountId, baselineBalance, activityWindow);
            
            // Perform some withdrawals and deposits
            account.withdraw(new Money(50.00, USD), Identity.of(2L));
            account.deposit(new Money(25.00, USD), Identity.of(3L));
            account.withdraw(new Money(30.00, USD), Identity.of(4L));
            
            assertThat(account.getTransactionsThisMonth()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should not count fee transactions in transaction count")
        void shouldNotCountFeeTransactionsInTransactionCount() {
            CheckingAccount account = CheckingAccount.of(accountId, baselineBalance, activityWindow);
            
            // Perform enough transactions to trigger fees
            for (int i = 0; i < 21; i++) {
                account.withdraw(new Money(10.00, USD), Identity.of(2L));
            }
            
            // Should count only actual transactions, not the fee
            assertThat(account.getTransactionsThisMonth()).isEqualTo(21);
            
            // But should have more activities due to the fee
            List<Activity> activities = account.getActivityWindow().getActivities();
            assertThat(activities.size()).isGreaterThan(21);
        }

        @Test
        @DisplayName("Should only count transactions from current month")
        void shouldOnlyCountTransactionsFromCurrentMonth() {
            List<Activity> activities = new ArrayList<>();
            LocalDateTime lastMonth = LocalDateTime.now().minusMonths(2);
            LocalDateTime thisMonth = LocalDateTime.now().minusDays(5);
            
            // Add old transactions (should not count)
            for (int i = 0; i < 15; i++) {
                activities.add(Activity.of(
                    Identity.of((long) (i + 1)),
                    accountId,
                    accountId,
                    Identity.of(2L),
                    lastMonth.plusDays(i),
                    new Money(10.00, USD)
                ));
            }
            
            // Add recent transactions (should count)
            for (int i = 0; i < 5; i++) {
                activities.add(Activity.of(
                    Identity.of((long) (i + 16)),
                    accountId,
                    accountId,
                    Identity.of(2L),
                    thisMonth.plusDays(i),
                    new Money(10.00, USD)
                ));
            }
            
            ActivityWindow windowWithActivities = new ActivityWindow(activities);
            CheckingAccount account = CheckingAccount.of(accountId, baselineBalance, windowWithActivities);
            
            assertThat(account.getTransactionsThisMonth()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Business Rules Comparison")
    class BusinessRulesComparison {
        
        @Test
        @DisplayName("Should have different minimum balance than savings account")
        void shouldHaveDifferentMinimumBalanceThanSavingsAccount() {
            CheckingAccount checkingAccount = CheckingAccount.of(accountId, baselineBalance, activityWindow);
            SavingAccount savingAccount = SavingAccount.of(accountId, baselineBalance, activityWindow);
            
            // Checking account should have lower minimum balance
            assertThat(checkingAccount.getMinimumBalance().amount())
                .isLessThan(savingAccount.getMinimumBalance().amount());
        }

        @Test
        @DisplayName("Should have no withdrawal limits unlike savings account")
        void shouldHaveNoWithdrawalLimitsUnlikeSavingsAccount() {
            CheckingAccount checkingAccount = CheckingAccount.of(accountId, baselineBalance, activityWindow);
            
            // Checking accounts don't have monthly withdrawal limits like savings accounts
            // Perform many withdrawals in a month
            for (int i = 0; i < 10; i++) {
                boolean result = checkingAccount.withdraw(new Money(10.00, USD), Identity.of(2L));
                assertThat(result).isTrue();
            }
            
            // All withdrawals should succeed (savings would limit to 6)
            assertThat(checkingAccount.getTransactionsThisMonth()).isEqualTo(10);
        }

        @Test
        @DisplayName("Should have different overdraft fee than savings account")
        void shouldHaveDifferentOverdraftFeeThanSavingsAccount() {
            Money lowBalance = new Money(10.00, USD);
            CheckingAccount checkingAccount = CheckingAccount.of(accountId, lowBalance, activityWindow);
            SavingAccount savingAccount = SavingAccount.of(accountId, lowBalance, new ActivityWindow(new ArrayList<>()));
            
            // Trigger overdraft on both accounts
            checkingAccount.withdraw(new Money(50.00, USD), Identity.of(2L));
            savingAccount.withdraw(new Money(50.00, USD), Identity.of(3L));
            
            // Check overdraft fees are different ($30 vs $35)
            Money checkingFees = checkingAccount.getFeesThisMonth();
            Money savingFees = savingAccount.getActivityWindow().getActivities().stream()
                .filter(activity -> activity.getSourceAccountId().equals(accountId) &&
                                  activity.getTargetAccountId().equals(accountId))
                .map(Activity::getMoney)
                .reduce(new Money(0.00, USD), Money::add);
                
            assertThat(checkingFees.amount()).isNotEqualTo(savingFees.amount());
        }
    }
}