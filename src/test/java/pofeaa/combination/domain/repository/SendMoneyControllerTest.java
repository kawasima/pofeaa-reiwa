package pofeaa.combination.domain.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pofeaa.combination.domain.model.Account;
import pofeaa.combination.domain.model.ActivityWindow;
import pofeaa.combination.domain.model.Identity;
import pofeaa.combination.domain.model.SavingAccount;
import pofeaa.original.base.money.Money;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendMoneyControllerTest {

    private static final Long SOURCE_ACCOUNT_ID = 1L;
    private static final Long TARGET_ACCOUNT_ID = 2L;

    @Mock
    private AccountRepository accountRepository;

    private SendMoneyController sendMoneyController;

    private Account sourceAccount;
    private Account targetAccount;
    private Identity sourceAccountId;
    private Identity targetAccountId;

    @BeforeEach
    void setUp() {
        sendMoneyController = new SendMoneyController(accountRepository);
        
        sourceAccountId = Identity.of(SOURCE_ACCOUNT_ID);
        targetAccountId = Identity.of(TARGET_ACCOUNT_ID);
        
        // Create accounts with sufficient balance
        Money sourceBalance = new Money(1000.00, Currency.getInstance(Locale.US));
        Money targetBalance = new Money(500.00, Currency.getInstance(Locale.US));
        
        sourceAccount = SavingAccount.of(sourceAccountId, sourceBalance, new ActivityWindow(new ArrayList<>()));
        targetAccount = SavingAccount.of(targetAccountId, targetBalance, new ActivityWindow(new ArrayList<>()));
    }

    @Nested
    @DisplayName("Successful Transfer Tests")
    class SuccessfulTransferTests {
        
        @Test
        @DisplayName("Should successfully transfer money between accounts")
        void shouldSuccessfullyTransferMoneyBetweenAccounts() {
            // Given
            Long amount = 100L;
            when(accountRepository.getAccount(any(Identity.class), any(LocalDateTime.class)))
                .thenAnswer(invocation -> {
                    Identity id = invocation.getArgument(0);
                    return id.asLong().equals(SOURCE_ACCOUNT_ID) ? sourceAccount : targetAccount;
                });

            // When
            boolean result = sendMoneyController.sendMoney(SOURCE_ACCOUNT_ID, TARGET_ACCOUNT_ID, amount);

            // Then
            assertThat(result).isTrue();
            
            // Verify repository interactions
            verify(accountRepository, times(2)).getAccount(any(Identity.class), any(LocalDateTime.class));
            verify(accountRepository, times(2)).lockAccount(any(Identity.class));
            verify(accountRepository, times(2)).releaseAccount(any(Identity.class));
        }

        @Test
        @DisplayName("Should handle small transfer amounts")
        void shouldHandleSmallTransferAmounts() {
            // Given
            Long amount = 1L;
            when(accountRepository.getAccount(any(Identity.class), any(LocalDateTime.class)))
                .thenAnswer(invocation -> {
                    Identity id = invocation.getArgument(0);
                    return id.asLong().equals(SOURCE_ACCOUNT_ID) ? sourceAccount : targetAccount;
                });

            // When
            boolean result = sendMoneyController.sendMoney(SOURCE_ACCOUNT_ID, TARGET_ACCOUNT_ID, amount);

            // Then
            assertThat(result).isTrue();
            verify(accountRepository, times(2)).releaseAccount(any(Identity.class));
        }

        @Test
        @DisplayName("Should handle large transfer amounts")
        void shouldHandleLargeTransferAmounts() {
            // Given
            Long amount = 500L;
            when(accountRepository.getAccount(any(Identity.class), any(LocalDateTime.class)))
                .thenAnswer(invocation -> {
                    Identity id = invocation.getArgument(0);
                    return id.asLong().equals(SOURCE_ACCOUNT_ID) ? sourceAccount : targetAccount;
                });

            // When
            boolean result = sendMoneyController.sendMoney(SOURCE_ACCOUNT_ID, TARGET_ACCOUNT_ID, amount);

            // Then
            assertThat(result).isTrue();
            verify(accountRepository, times(2)).releaseAccount(any(Identity.class));
        }
    }

    @Nested
    @DisplayName("Failed Transfer Tests")
    class FailedTransferTests {
        
        @Test
        @DisplayName("Should fail when source account has insufficient funds")
        void shouldFailWhenSourceAccountHasInsufficientFunds() {
            // Given
            Long amount = 2000L; // More than available balance
            Money lowBalance = new Money(50.00, Currency.getInstance(Locale.US));
            Account lowBalanceAccount = SavingAccount.of(sourceAccountId, lowBalance, new ActivityWindow(new ArrayList<>()));
            
            when(accountRepository.getAccount(any(Identity.class), any(LocalDateTime.class)))
                .thenAnswer(invocation -> {
                    Identity id = invocation.getArgument(0);
                    return id.asLong().equals(SOURCE_ACCOUNT_ID) ? lowBalanceAccount : targetAccount;
                });

            // When
            boolean result = sendMoneyController.sendMoney(SOURCE_ACCOUNT_ID, TARGET_ACCOUNT_ID, amount);

            // Then
            assertThat(result).isFalse();
            
            // Verify only source account was locked and released (target never locked)
            verify(accountRepository, times(1)).lockAccount(any(Identity.class));
            verify(accountRepository, times(1)).releaseAccount(any(Identity.class));
        }

        @Test
        @DisplayName("Should fail when deposit fails")
        void shouldFailWhenDepositFails() {
            // Given
            Long amount = 100L;
            Account mockTargetAccount = mock(Account.class);
            when(mockTargetAccount.getId()).thenReturn(targetAccountId);
            when(mockTargetAccount.deposit(any(Money.class), any(Identity.class))).thenReturn(false);
            
            when(accountRepository.getAccount(any(Identity.class), any(LocalDateTime.class)))
                .thenAnswer(invocation -> {
                    Identity id = invocation.getArgument(0);
                    return id.asLong().equals(SOURCE_ACCOUNT_ID) ? sourceAccount : mockTargetAccount;
                });

            // When
            boolean result = sendMoneyController.sendMoney(SOURCE_ACCOUNT_ID, TARGET_ACCOUNT_ID, amount);

            // Then
            assertThat(result).isFalse();
            
            // Verify both accounts were locked and released
            verify(accountRepository, times(2)).lockAccount(any(Identity.class));
            verify(accountRepository, times(2)).releaseAccount(any(Identity.class));
        }
    }

    @Nested
    @DisplayName("Account Locking Tests")
    class AccountLockingTests {
        
        @Test
        @DisplayName("Should properly lock and release accounts in correct order")
        void shouldProperlyLockAndReleaseAccountsInCorrectOrder() {
            // Given
            Long amount = 100L;
            when(accountRepository.getAccount(any(Identity.class), any(LocalDateTime.class)))
                .thenAnswer(invocation -> {
                    Identity id = invocation.getArgument(0);
                    return id.asLong().equals(SOURCE_ACCOUNT_ID) ? sourceAccount : targetAccount;
                });

            // When
            sendMoneyController.sendMoney(SOURCE_ACCOUNT_ID, TARGET_ACCOUNT_ID, amount);

            // Then - verify the order of operations
            var inOrder = inOrder(accountRepository);
            inOrder.verify(accountRepository, times(2)).getAccount(any(Identity.class), any(LocalDateTime.class));
            inOrder.verify(accountRepository, times(2)).lockAccount(any(Identity.class));
            inOrder.verify(accountRepository, times(2)).releaseAccount(any(Identity.class));
        }

        @Test
        @DisplayName("Should release source account lock when withdrawal fails")
        void shouldReleaseSourceAccountLockWhenWithdrawalFails() {
            // Given
            Long amount = 2000L; // Amount that will cause withdrawal to fail
            Money lowBalance = new Money(10.00, Currency.getInstance(Locale.US));
            Account lowBalanceAccount = SavingAccount.of(sourceAccountId, lowBalance, new ActivityWindow(new ArrayList<>()));
            
            when(accountRepository.getAccount(any(Identity.class), any(LocalDateTime.class)))
                .thenAnswer(invocation -> {
                    Identity id = invocation.getArgument(0);
                    return id.asLong().equals(SOURCE_ACCOUNT_ID) ? lowBalanceAccount : targetAccount;
                });

            // When
            sendMoneyController.sendMoney(SOURCE_ACCOUNT_ID, TARGET_ACCOUNT_ID, amount);

            // Then
            verify(accountRepository, times(1)).lockAccount(any(Identity.class));
            verify(accountRepository, times(1)).releaseAccount(any(Identity.class));
        }

        @Test
        @DisplayName("Should release both account locks when deposit fails")
        void shouldReleaseBothAccountLocksWhenDepositFails() {
            // Given
            Long amount = 100L;
            Account mockTargetAccount = mock(Account.class);
            when(mockTargetAccount.getId()).thenReturn(targetAccountId);
            when(mockTargetAccount.deposit(any(Money.class), any(Identity.class))).thenReturn(false);
            
            when(accountRepository.getAccount(any(Identity.class), any(LocalDateTime.class)))
                .thenAnswer(invocation -> {
                    Identity id = invocation.getArgument(0);
                    return id.asLong().equals(SOURCE_ACCOUNT_ID) ? sourceAccount : mockTargetAccount;
                });

            // When
            sendMoneyController.sendMoney(SOURCE_ACCOUNT_ID, TARGET_ACCOUNT_ID, amount);

            // Then
            verify(accountRepository, times(2)).lockAccount(any(Identity.class));
            verify(accountRepository, times(2)).releaseAccount(any(Identity.class));
        }
    }

    @Nested
    @DisplayName("Money Creation Tests")
    class MoneyCreationTests {
        
        @Test
        @DisplayName("Should create correct Money object from amount parameter")
        void shouldCreateCorrectMoneyObjectFromAmountParameter() {
            // Given
            Long amount = 250L;
            when(accountRepository.getAccount(any(Identity.class), any(LocalDateTime.class)))
                .thenAnswer(invocation -> {
                    Identity id = invocation.getArgument(0);
                    return id.asLong().equals(SOURCE_ACCOUNT_ID) ? sourceAccount : targetAccount;
                });

            // When
            boolean result = sendMoneyController.sendMoney(SOURCE_ACCOUNT_ID, TARGET_ACCOUNT_ID, amount);

            // Then
            assertThat(result).isTrue();
            // Verify that the transfer was processed (activities were added)
            assertThat(sourceAccount.getActivityWindow().getActivities()).isNotEmpty();
        }

        @Test
        @DisplayName("Should handle zero amount transfers")
        void shouldHandleZeroAmountTransfers() {
            // Given
            Long amount = 0L;
            when(accountRepository.getAccount(any(Identity.class), any(LocalDateTime.class)))
                .thenAnswer(invocation -> {
                    Identity id = invocation.getArgument(0);
                    return id.asLong().equals(SOURCE_ACCOUNT_ID) ? sourceAccount : targetAccount;
                });

            // When
            boolean result = sendMoneyController.sendMoney(SOURCE_ACCOUNT_ID, TARGET_ACCOUNT_ID, amount);

            // Then
            assertThat(result).isTrue();
            verify(accountRepository, times(2)).releaseAccount(any(Identity.class));
        }
    }

    @Nested
    @DisplayName("Baseline Date Tests")
    class BaselineDateTests {
        
        @Test
        @DisplayName("Should use baseline date 10 days in the past")
        void shouldUseBaselineDateTenDaysInThePast() {
            // Given
            Long amount = 100L;
            when(accountRepository.getAccount(any(Identity.class), any(LocalDateTime.class)))
                .thenAnswer(invocation -> {
                    Identity id = invocation.getArgument(0);
                    return id.asLong().equals(SOURCE_ACCOUNT_ID) ? sourceAccount : targetAccount;
                });

            // When
            sendMoneyController.sendMoney(SOURCE_ACCOUNT_ID, TARGET_ACCOUNT_ID, amount);

            // Then
            verify(accountRepository, times(2)).getAccount(any(Identity.class), argThat(date -> 
                date.isBefore(LocalDateTime.now().minusDays(9)) && 
                date.isAfter(LocalDateTime.now().minusDays(11))
            ));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle transfer to same account")
        void shouldHandleTransferToSameAccount() {
            // Given
            Long amount = 100L;
            Long sameAccountId = SOURCE_ACCOUNT_ID;
            when(accountRepository.getAccount(any(Identity.class), any(LocalDateTime.class)))
                .thenReturn(sourceAccount);

            // When
            boolean result = sendMoneyController.sendMoney(sameAccountId, sameAccountId, amount);

            // Then
            assertThat(result).isTrue();
            verify(accountRepository, times(2)).getAccount(any(Identity.class), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Should handle very large amounts")
        void shouldHandleVeryLargeAmounts() {
            // Given
            Long amount = 1000000L; // Large but manageable amount
            Money largeBalance = new Money(2000000.00, Currency.getInstance(Locale.US));
            Account richAccount = SavingAccount.of(sourceAccountId, largeBalance, new ActivityWindow(new ArrayList<>()));
            
            when(accountRepository.getAccount(any(Identity.class), any(LocalDateTime.class)))
                .thenAnswer(invocation -> {
                    Identity id = invocation.getArgument(0);
                    return id.asLong().equals(SOURCE_ACCOUNT_ID) ? richAccount : targetAccount;
                });

            // When & Then - Should not throw exception
            assertThatCode(() -> sendMoneyController.sendMoney(SOURCE_ACCOUNT_ID, TARGET_ACCOUNT_ID, amount))
                .doesNotThrowAnyException();
        }
    }
}