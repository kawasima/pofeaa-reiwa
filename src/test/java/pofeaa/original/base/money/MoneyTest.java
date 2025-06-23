package pofeaa.original.base.money;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;

import static org.assertj.core.api.Assertions.*;

class MoneyTest {

    private static final Currency USD = Currency.getInstance(Locale.US);
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency JPY = Currency.getInstance("JPY");

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create Money from double amount")
        void shouldCreateMoneyFromDoubleAmount() {
            Money money = new Money(10.50, USD);
            assertThat(money.amount()).isEqualTo(new BigDecimal("10.50"));
            assertThat(money.currency()).isEqualTo(USD);
        }

        @Test
        @DisplayName("Should create Money from long amount")
        void shouldCreateMoneyFromLongAmount() {
            Money money = new Money(10L, USD);
            assertThat(money.amount()).isEqualTo(new BigDecimal("10.00"));
            assertThat(money.currency()).isEqualTo(USD);
        }

        @Test
        @DisplayName("Should handle different currency fraction digits")
        void shouldHandleDifferentCurrencyFractionDigits() {
            Money usdMoney = new Money(10.50, USD); // 2 decimal places
            Money jpyMoney = new Money(1000, JPY);  // 0 decimal places
            
            assertThat(usdMoney.amount()).isEqualTo(new BigDecimal("10.50"));
            assertThat(jpyMoney.amount()).isEqualTo(new BigDecimal("1000"));
        }

        @Test
        @DisplayName("Should round amounts correctly")
        void shouldRoundAmountsCorrectly() {
            Money money1 = new Money(10.555, USD);
            Money money2 = new Money(10.554, USD);
            
            assertThat(money1.amount()).isEqualTo(new BigDecimal("10.56"));
            assertThat(money2.amount()).isEqualTo(new BigDecimal("10.55"));
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {
        
        @Test
        @DisplayName("Should create USD Money using dollars factory method")
        void shouldCreateUsdMoneyUsingDollarsFactory() {
            Money money = Money.dollars(new BigDecimal("25.99"));
            assertThat(money.amount()).isEqualTo(new BigDecimal("25.99"));
            assertThat(money.currency()).isEqualTo(USD);
        }
    }

    @Nested
    @DisplayName("Addition Tests")
    class AdditionTests {
        
        @Test
        @DisplayName("Should add two Money objects with same currency")
        void shouldAddTwoMoneyObjectsWithSameCurrency() {
            Money money1 = new Money(10.50, USD);
            Money money2 = new Money(5.25, USD);
            
            Money result = money1.add(money2);
            
            assertThat(result.amount()).isEqualTo(new BigDecimal("15.75"));
            assertThat(result.currency()).isEqualTo(USD);
        }

        @Test
        @DisplayName("Should throw exception when adding different currencies")
        void shouldThrowExceptionWhenAddingDifferentCurrencies() {
            Money usdMoney = new Money(10.00, USD);
            Money eurMoney = new Money(10.00, EUR);
            
            assertThatThrownBy(() -> usdMoney.add(eurMoney))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot add Money with different currencies");
        }

        @Test
        @DisplayName("Should maintain immutability during addition")
        void shouldMaintainImmutabilityDuringAddition() {
            Money money1 = new Money(10.00, USD);
            Money money2 = new Money(5.00, USD);
            
            Money result = money1.add(money2);
            
            assertThat(money1.amount()).isEqualTo(new BigDecimal("10.00"));
            assertThat(money2.amount()).isEqualTo(new BigDecimal("5.00"));
            assertThat(result.amount()).isEqualTo(new BigDecimal("15.00"));
        }
    }

    @Nested
    @DisplayName("Subtraction Tests")
    class SubtractionTests {
        
        @Test
        @DisplayName("Should subtract two Money objects with same currency")
        void shouldSubtractTwoMoneyObjectsWithSameCurrency() {
            Money money1 = new Money(10.50, USD);
            Money money2 = new Money(5.25, USD);
            
            Money result = money1.subtract(money2);
            
            assertThat(result.amount()).isEqualTo(new BigDecimal("5.25"));
            assertThat(result.currency()).isEqualTo(USD);
        }

        @Test
        @DisplayName("Should throw exception when subtracting different currencies")
        void shouldThrowExceptionWhenSubtractingDifferentCurrencies() {
            Money usdMoney = new Money(10.00, USD);
            Money eurMoney = new Money(10.00, EUR);
            
            assertThatThrownBy(() -> usdMoney.subtract(eurMoney))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot subtract Money with different currencies");
        }

        @Test
        @DisplayName("Should handle negative results")
        void shouldHandleNegativeResults() {
            Money money1 = new Money(5.00, USD);
            Money money2 = new Money(10.00, USD);
            
            Money result = money1.subtract(money2);
            
            assertThat(result.amount()).isEqualTo(new BigDecimal("-5.00"));
            assertThat(result.currency()).isEqualTo(USD);
        }

        @Test
        @DisplayName("Should maintain immutability during subtraction")
        void shouldMaintainImmutabilityDuringSubtraction() {
            Money money1 = new Money(10.00, USD);
            Money money2 = new Money(5.00, USD);
            
            Money result = money1.subtract(money2);
            
            assertThat(money1.amount()).isEqualTo(new BigDecimal("10.00"));
            assertThat(money2.amount()).isEqualTo(new BigDecimal("5.00"));
            assertThat(result.amount()).isEqualTo(new BigDecimal("5.00"));
        }
    }

    @Nested
    @DisplayName("IsPositiveOrZero Tests")
    class IsPositiveOrZeroTests {
        
        @Test
        @DisplayName("Should return true for positive amounts")
        void shouldReturnTrueForPositiveAmounts() {
            Money money = new Money(100.00, USD);
            assertThat(money.isPositiveOrZero()).isTrue();
        }

        @Test
        @DisplayName("Should return true for zero amount")
        void shouldReturnTrueForZeroAmount() {
            Money money = new Money(0.00, USD);
            assertThat(money.isPositiveOrZero()).isTrue();
        }

        @Test
        @DisplayName("Should return false for negative amounts")
        void shouldReturnFalseForNegativeAmounts() {
            Money money = new Money(-50.00, USD);
            assertThat(money.isPositiveOrZero()).isFalse();
        }

        @Test
        @DisplayName("Should work with very small positive amounts")
        void shouldWorkWithVerySmallPositiveAmounts() {
            Money money = new Money(0.01, USD);
            assertThat(money.isPositiveOrZero()).isTrue();
        }

        @Test
        @DisplayName("Should work with very small negative amounts")
        void shouldWorkWithVerySmallNegativeAmounts() {
            Money money = new Money(-0.01, USD);
            assertThat(money.isPositiveOrZero()).isFalse();
        }
    }

    @Nested
    @DisplayName("Allocation Tests")
    class AllocationTests {
        
        @Test
        @DisplayName("Should allocate money equally among recipients")
        void shouldAllocateMoneyEquallyAmongRecipients() {
            Money money = new Money(10.00, USD);
            Money[] results = money.allocate(3);
            
            assertThat(results).hasSize(3);
            assertThat(results[0].amount()).isEqualTo(new BigDecimal("3.34"));
            assertThat(results[1].amount()).isEqualTo(new BigDecimal("3.33"));
            assertThat(results[2].amount()).isEqualTo(new BigDecimal("3.33"));
            
            // Verify total equals original amount
            Money total = results[0].add(results[1]).add(results[2]);
            assertThat(total.amount()).isEqualTo(new BigDecimal("10.00"));
        }

        @Test
        @DisplayName("Should allocate money with no remainder")
        void shouldAllocateMoneyWithNoRemainder() {
            Money money = new Money(9.00, USD);
            Money[] results = money.allocate(3);
            
            assertThat(results).hasSize(3);
            assertThat(results[0].amount()).isEqualTo(new BigDecimal("3.00"));
            assertThat(results[1].amount()).isEqualTo(new BigDecimal("3.00"));
            assertThat(results[2].amount()).isEqualTo(new BigDecimal("3.00"));
        }

        @Test
        @DisplayName("Should allocate money by ratios")
        void shouldAllocateMoneyByRatios() {
            Money money = new Money(100.00, USD);
            long[] ratios = {3, 7}; // 30% and 70%
            Money[] results = money.allocate(ratios);
            
            assertThat(results).hasSize(2);
            assertThat(results[0].amount()).isEqualTo(new BigDecimal("30.00"));
            assertThat(results[1].amount()).isEqualTo(new BigDecimal("70.00"));
        }

        @Test
        @DisplayName("Should handle ratio allocation with remainder")
        void shouldHandleRatioAllocationWithRemainder() {
            Money money = new Money(10.00, USD);
            long[] ratios = {1, 1, 1}; // Equal split
            Money[] results = money.allocate(ratios);
            
            assertThat(results).hasSize(3);
            assertThat(results[0].amount()).isEqualTo(new BigDecimal("3.34"));
            assertThat(results[1].amount()).isEqualTo(new BigDecimal("3.33"));
            assertThat(results[2].amount()).isEqualTo(new BigDecimal("3.33"));
            
            // Verify total equals original amount
            Money total = results[0].add(results[1]).add(results[2]);
            assertThat(total.amount()).isEqualTo(new BigDecimal("10.00"));
        }

        @Test
        @DisplayName("Should allocate single recipient")
        void shouldAllocateSingleRecipient() {
            Money money = new Money(10.00, USD);
            Money[] results = money.allocate(1);
            
            assertThat(results).hasSize(1);
            assertThat(results[0].amount()).isEqualTo(new BigDecimal("10.00"));
        }
    }

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {
        
        @Test
        @DisplayName("Should be equal for same amount and currency")
        void shouldBeEqualForSameAmountAndCurrency() {
            Money money1 = new Money(10.00, USD);
            Money money2 = new Money(10.00, USD);
            
            assertThat(money1).isEqualTo(money2);
            assertThat(money1.hashCode()).isEqualTo(money2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal for different amounts")
        void shouldNotBeEqualForDifferentAmounts() {
            Money money1 = new Money(10.00, USD);
            Money money2 = new Money(20.00, USD);
            
            assertThat(money1).isNotEqualTo(money2);
        }

        @Test
        @DisplayName("Should not be equal for different currencies")
        void shouldNotBeEqualForDifferentCurrencies() {
            Money money1 = new Money(10.00, USD);
            Money money2 = new Money(10.00, EUR);
            
            assertThat(money1).isNotEqualTo(money2);
        }

        @Test
        @DisplayName("Should handle null and different types")
        void shouldHandleNullAndDifferentTypes() {
            Money money = new Money(10.00, USD);
            
            assertThat(money).isNotEqualTo(null);
            assertThat(money).isNotEqualTo("10.00 USD");
            assertThat(money).isEqualTo(money); // Same instance
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle zero amounts")
        void shouldHandleZeroAmounts() {
            Money money = new Money(0.00, USD);
            assertThat(money.amount()).isEqualTo(new BigDecimal("0.00"));
            
            Money[] allocated = money.allocate(3);
            assertThat(allocated[0].amount()).isEqualTo(new BigDecimal("0.00"));
            assertThat(allocated[1].amount()).isEqualTo(new BigDecimal("0.00"));
            assertThat(allocated[2].amount()).isEqualTo(new BigDecimal("0.00"));
        }

        @Test
        @DisplayName("Should handle negative amounts")
        void shouldHandleNegativeAmounts() {
            Money money = new Money(-10.50, USD);
            assertThat(money.amount()).isEqualTo(new BigDecimal("-10.50"));
            
            Money positive = new Money(5.00, USD);
            Money result = money.add(positive);
            assertThat(result.amount()).isEqualTo(new BigDecimal("-5.50"));
        }

        @Test
        @DisplayName("Should handle very small amounts")
        void shouldHandleVerySmallAmounts() {
            Money money = new Money(0.01, USD);
            Money[] allocated = money.allocate(3);
            
            assertThat(allocated[0].amount()).isEqualTo(new BigDecimal("0.01"));
            assertThat(allocated[1].amount()).isEqualTo(new BigDecimal("0.00"));
            assertThat(allocated[2].amount()).isEqualTo(new BigDecimal("0.00"));
        }

        @Test
        @DisplayName("Should handle currency with no fraction digits")
        void shouldHandleCurrencyWithNoFractionDigits() {
            Money money = new Money(1234, JPY);
            assertThat(money.amount()).isEqualTo(new BigDecimal("1234"));
            
            Money[] allocated = money.allocate(3);
            assertThat(allocated[0].amount()).isEqualTo(new BigDecimal("412"));
            assertThat(allocated[1].amount()).isEqualTo(new BigDecimal("411"));
            assertThat(allocated[2].amount()).isEqualTo(new BigDecimal("411"));
        }
    }
}