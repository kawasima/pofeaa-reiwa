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
    
    // Test constants
    private static final String CANNOT_ADD_DIFFERENT_CURRENCIES = "Cannot add Money with different currencies";
    private static final String CANNOT_SUBTRACT_DIFFERENT_CURRENCIES = "Cannot subtract Money with different currencies";
    private static final String USD_STRING = "10.00 USD";
    
    // Amount constants
    private static final double TEN_FIFTY = 10.50;
    private static final double TEN_DOLLARS = 10.00;
    private static final double FIVE_DOLLARS = 5.00;
    private static final double FIVE_TWENTY_FIVE = 5.25;
    private static final double MINUS_TEN_FIFTY = -10.50;
    private static final double TWENTY_DOLLARS = 20.00;
    private static final double FIFTY_DOLLARS = 50.00;
    private static final double HUNDRED_DOLLARS = 100.00;
    private static final double NINE_DOLLARS = 9.00;
    private static final double ZERO_DOLLARS = 0.00;
    private static final double ONE_CENT = 0.01;
    private static final double MINUS_ONE_CENT = -0.01;
    private static final double TEN_FIVE_FIVE_FIVE = 10.555;
    private static final double TEN_FIVE_FIVE_FOUR = 10.554;
    private static final int THOUSAND_YEN = 1000;
    private static final int TWELVE_THIRTY_FOUR_YEN = 1234;
    private static final long TEN_LONG = 10L;
    
    // BigDecimal constants
    private static final BigDecimal BD_TEN_FIFTY = new BigDecimal("10.50");
    private static final BigDecimal BD_TEN_DOLLARS = new BigDecimal("10.00");
    private static final BigDecimal BD_FIVE_TWENTY_FIVE = new BigDecimal("5.25");
    private static final BigDecimal BD_FIFTEEN_SEVENTY_FIVE = new BigDecimal("15.75");
    private static final BigDecimal BD_FIFTEEN_DOLLARS = new BigDecimal("15.00");
    private static final BigDecimal BD_FIVE_DOLLARS = new BigDecimal("5.00");
    private static final BigDecimal BD_MINUS_FIVE_DOLLARS = new BigDecimal("-5.00");
    private static final BigDecimal BD_MINUS_FIVE_FIFTY = new BigDecimal("-5.50");
    private static final BigDecimal BD_MINUS_TEN_FIFTY = new BigDecimal("-10.50");
    private static final BigDecimal BD_TWENTY_FIVE_NINETY_NINE = new BigDecimal("25.99");
    private static final BigDecimal BD_ZERO_DOLLARS = new BigDecimal("0.00");
    private static final BigDecimal BD_ONE_CENT = new BigDecimal("0.01");
    private static final BigDecimal BD_TEN_FIFTY_SIX = new BigDecimal("10.56");
    private static final BigDecimal BD_TEN_FIFTY_FIVE = new BigDecimal("10.55");
    private static final BigDecimal BD_THOUSAND = new BigDecimal("1000");
    private static final BigDecimal BD_TWELVE_THIRTY_FOUR = new BigDecimal("1234");
    private static final BigDecimal BD_THREE_THIRTY_FOUR = new BigDecimal("3.34");
    private static final BigDecimal BD_THREE_THIRTY_THREE = new BigDecimal("3.33");
    private static final BigDecimal BD_THREE_DOLLARS = new BigDecimal("3.00");
    private static final BigDecimal BD_THIRTY_DOLLARS = new BigDecimal("30.00");
    private static final BigDecimal BD_SEVENTY_DOLLARS = new BigDecimal("70.00");
    private static final BigDecimal BD_FOUR_TWELVE = new BigDecimal("412");
    private static final BigDecimal BD_FOUR_ELEVEN = new BigDecimal("411");
    
    // Numeric constants
    private static final int THREE = 3;
    private static final int ONE = 1;
    private static final int TWO = 2;
    private static final long THREE_RATIO = 3L;
    private static final long SEVEN_RATIO = 7L;
    private static final long ONE_RATIO = 1L;

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create Money from double amount")
        void shouldCreateMoneyFromDoubleAmount() {
            Money money = new Money(TEN_FIFTY, USD);
            assertThat(money.amount()).isEqualTo(BD_TEN_FIFTY);
            assertThat(money.currency()).isEqualTo(USD);
        }

        @Test
        @DisplayName("Should create Money from long amount")
        void shouldCreateMoneyFromLongAmount() {
            Money money = new Money(TEN_LONG, USD);
            assertThat(money.amount()).isEqualTo(BD_TEN_DOLLARS);
            assertThat(money.currency()).isEqualTo(USD);
        }

        @Test
        @DisplayName("Should handle different currency fraction digits")
        void shouldHandleDifferentCurrencyFractionDigits() {
            Money usdMoney = new Money(TEN_FIFTY, USD); // 2 decimal places
            Money jpyMoney = new Money(THOUSAND_YEN, JPY);  // 0 decimal places
            
            assertThat(usdMoney.amount()).isEqualTo(BD_TEN_FIFTY);
            assertThat(jpyMoney.amount()).isEqualTo(BD_THOUSAND);
        }

        @Test
        @DisplayName("Should round amounts correctly")
        void shouldRoundAmountsCorrectly() {
            Money money1 = new Money(TEN_FIVE_FIVE_FIVE, USD);
            Money money2 = new Money(TEN_FIVE_FIVE_FOUR, USD);
            
            assertThat(money1.amount()).isEqualTo(BD_TEN_FIFTY_SIX);
            assertThat(money2.amount()).isEqualTo(BD_TEN_FIFTY_FIVE);
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {
        
        @Test
        @DisplayName("Should create USD Money using dollars factory method")
        void shouldCreateUsdMoneyUsingDollarsFactory() {
            Money money = Money.dollars(BD_TWENTY_FIVE_NINETY_NINE);
            assertThat(money.amount()).isEqualTo(BD_TWENTY_FIVE_NINETY_NINE);
            assertThat(money.currency()).isEqualTo(USD);
        }
    }

    @Nested
    @DisplayName("Addition Tests")
    class AdditionTests {
        
        @Test
        @DisplayName("Should add two Money objects with same currency")
        void shouldAddTwoMoneyObjectsWithSameCurrency() {
            Money money1 = new Money(TEN_FIFTY, USD);
            Money money2 = new Money(FIVE_TWENTY_FIVE, USD);
            
            Money result = money1.add(money2);
            
            assertThat(result.amount()).isEqualTo(BD_FIFTEEN_SEVENTY_FIVE);
            assertThat(result.currency()).isEqualTo(USD);
        }

        @Test
        @DisplayName("Should throw exception when adding different currencies")
        void shouldThrowExceptionWhenAddingDifferentCurrencies() {
            Money usdMoney = new Money(TEN_DOLLARS, USD);
            Money eurMoney = new Money(TEN_DOLLARS, EUR);
            
            assertThatThrownBy(() -> usdMoney.add(eurMoney))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(CANNOT_ADD_DIFFERENT_CURRENCIES);
        }

        @Test
        @DisplayName("Should maintain immutability during addition")
        void shouldMaintainImmutabilityDuringAddition() {
            Money money1 = new Money(TEN_DOLLARS, USD);
            Money money2 = new Money(FIVE_DOLLARS, USD);
            
            Money result = money1.add(money2);
            
            assertThat(money1.amount()).isEqualTo(BD_TEN_DOLLARS);
            assertThat(money2.amount()).isEqualTo(BD_FIVE_DOLLARS);
            assertThat(result.amount()).isEqualTo(BD_FIFTEEN_DOLLARS);
        }
    }

    @Nested
    @DisplayName("Subtraction Tests")
    class SubtractionTests {
        
        @Test
        @DisplayName("Should subtract two Money objects with same currency")
        void shouldSubtractTwoMoneyObjectsWithSameCurrency() {
            Money money1 = new Money(TEN_FIFTY, USD);
            Money money2 = new Money(FIVE_TWENTY_FIVE, USD);
            
            Money result = money1.subtract(money2);
            
            assertThat(result.amount()).isEqualTo(BD_FIVE_TWENTY_FIVE);
            assertThat(result.currency()).isEqualTo(USD);
        }

        @Test
        @DisplayName("Should throw exception when subtracting different currencies")
        void shouldThrowExceptionWhenSubtractingDifferentCurrencies() {
            Money usdMoney = new Money(TEN_DOLLARS, USD);
            Money eurMoney = new Money(TEN_DOLLARS, EUR);
            
            assertThatThrownBy(() -> usdMoney.subtract(eurMoney))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(CANNOT_SUBTRACT_DIFFERENT_CURRENCIES);
        }

        @Test
        @DisplayName("Should handle negative results")
        void shouldHandleNegativeResults() {
            Money money1 = new Money(FIVE_DOLLARS, USD);
            Money money2 = new Money(TEN_DOLLARS, USD);
            
            Money result = money1.subtract(money2);
            
            assertThat(result.amount()).isEqualTo(BD_MINUS_FIVE_DOLLARS);
            assertThat(result.currency()).isEqualTo(USD);
        }

        @Test
        @DisplayName("Should maintain immutability during subtraction")
        void shouldMaintainImmutabilityDuringSubtraction() {
            Money money1 = new Money(TEN_DOLLARS, USD);
            Money money2 = new Money(FIVE_DOLLARS, USD);
            
            Money result = money1.subtract(money2);
            
            assertThat(money1.amount()).isEqualTo(BD_TEN_DOLLARS);
            assertThat(money2.amount()).isEqualTo(BD_FIVE_DOLLARS);
            assertThat(result.amount()).isEqualTo(BD_FIVE_DOLLARS);
        }
    }

    @Nested
    @DisplayName("IsPositiveOrZero Tests")
    class IsPositiveOrZeroTests {
        
        @Test
        @DisplayName("Should return true for positive amounts")
        void shouldReturnTrueForPositiveAmounts() {
            Money money = new Money(HUNDRED_DOLLARS, USD);
            assertThat(money.isPositiveOrZero()).isTrue();
        }

        @Test
        @DisplayName("Should return true for zero amount")
        void shouldReturnTrueForZeroAmount() {
            Money money = new Money(ZERO_DOLLARS, USD);
            assertThat(money.isPositiveOrZero()).isTrue();
        }

        @Test
        @DisplayName("Should return false for negative amounts")
        void shouldReturnFalseForNegativeAmounts() {
            Money money = new Money(-FIFTY_DOLLARS, USD);
            assertThat(money.isPositiveOrZero()).isFalse();
        }

        @Test
        @DisplayName("Should work with very small positive amounts")
        void shouldWorkWithVerySmallPositiveAmounts() {
            Money money = new Money(ONE_CENT, USD);
            assertThat(money.isPositiveOrZero()).isTrue();
        }

        @Test
        @DisplayName("Should work with very small negative amounts")
        void shouldWorkWithVerySmallNegativeAmounts() {
            Money money = new Money(MINUS_ONE_CENT, USD);
            assertThat(money.isPositiveOrZero()).isFalse();
        }
    }

    @Nested
    @DisplayName("Allocation Tests")
    class AllocationTests {
        
        @Test
        @DisplayName("Should allocate money equally among recipients")
        void shouldAllocateMoneyEquallyAmongRecipients() {
            Money money = new Money(TEN_DOLLARS, USD);
            Money[] results = money.allocate(THREE);
            
            assertThat(results).hasSize(THREE);
            assertThat(results[0].amount()).isEqualTo(BD_THREE_THIRTY_FOUR);
            assertThat(results[1].amount()).isEqualTo(BD_THREE_THIRTY_THREE);
            assertThat(results[TWO].amount()).isEqualTo(BD_THREE_THIRTY_THREE);
            
            // Verify total equals original amount
            Money total = results[0].add(results[1]).add(results[TWO]);
            assertThat(total.amount()).isEqualTo(BD_TEN_DOLLARS);
        }

        @Test
        @DisplayName("Should allocate money with no remainder")
        void shouldAllocateMoneyWithNoRemainder() {
            Money money = new Money(NINE_DOLLARS, USD);
            Money[] results = money.allocate(THREE);
            
            assertThat(results).hasSize(THREE);
            assertThat(results[0].amount()).isEqualTo(BD_THREE_DOLLARS);
            assertThat(results[1].amount()).isEqualTo(BD_THREE_DOLLARS);
            assertThat(results[TWO].amount()).isEqualTo(BD_THREE_DOLLARS);
        }

        @Test
        @DisplayName("Should allocate money by ratios")
        void shouldAllocateMoneyByRatios() {
            Money money = new Money(HUNDRED_DOLLARS, USD);
            long[] ratios = {THREE_RATIO, SEVEN_RATIO}; // 30% and 70%
            Money[] results = money.allocate(ratios);
            
            assertThat(results).hasSize(TWO);
            assertThat(results[0].amount()).isEqualTo(BD_THIRTY_DOLLARS);
            assertThat(results[1].amount()).isEqualTo(BD_SEVENTY_DOLLARS);
        }

        @Test
        @DisplayName("Should handle ratio allocation with remainder")
        void shouldHandleRatioAllocationWithRemainder() {
            Money money = new Money(TEN_DOLLARS, USD);
            long[] ratios = {ONE_RATIO, ONE_RATIO, ONE_RATIO}; // Equal split
            Money[] results = money.allocate(ratios);
            
            assertThat(results).hasSize(THREE);
            assertThat(results[0].amount()).isEqualTo(BD_THREE_THIRTY_FOUR);
            assertThat(results[1].amount()).isEqualTo(BD_THREE_THIRTY_THREE);
            assertThat(results[TWO].amount()).isEqualTo(BD_THREE_THIRTY_THREE);
            
            // Verify total equals original amount
            Money total = results[0].add(results[1]).add(results[TWO]);
            assertThat(total.amount()).isEqualTo(BD_TEN_DOLLARS);
        }

        @Test
        @DisplayName("Should allocate single recipient")
        void shouldAllocateSingleRecipient() {
            Money money = new Money(TEN_DOLLARS, USD);
            Money[] results = money.allocate(ONE);
            
            assertThat(results).hasSize(ONE);
            assertThat(results[0].amount()).isEqualTo(BD_TEN_DOLLARS);
        }
    }

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {
        
        @Test
        @DisplayName("Should be equal for same amount and currency")
        void shouldBeEqualForSameAmountAndCurrency() {
            Money money1 = new Money(TEN_DOLLARS, USD);
            Money money2 = new Money(TEN_DOLLARS, USD);
            
            assertThat(money1).isEqualTo(money2);
            assertThat(money1.hashCode()).isEqualTo(money2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal for different amounts")
        void shouldNotBeEqualForDifferentAmounts() {
            Money money1 = new Money(TEN_DOLLARS, USD);
            Money money2 = new Money(TWENTY_DOLLARS, USD);
            
            assertThat(money1).isNotEqualTo(money2);
        }

        @Test
        @DisplayName("Should not be equal for different currencies")
        void shouldNotBeEqualForDifferentCurrencies() {
            Money money1 = new Money(TEN_DOLLARS, USD);
            Money money2 = new Money(TEN_DOLLARS, EUR);
            
            assertThat(money1).isNotEqualTo(money2);
        }

        @Test
        @DisplayName("Should handle null and different types")
        void shouldHandleNullAndDifferentTypes() {
            Money money = new Money(TEN_DOLLARS, USD);
            
            assertThat(money).isNotEqualTo(null);
            assertThat(money).isNotEqualTo(USD_STRING);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle zero amounts")
        void shouldHandleZeroAmounts() {
            Money money = new Money(ZERO_DOLLARS, USD);
            assertThat(money.amount()).isEqualTo(BD_ZERO_DOLLARS);
            
            Money[] allocated = money.allocate(THREE);
            assertThat(allocated[0].amount()).isEqualTo(BD_ZERO_DOLLARS);
            assertThat(allocated[1].amount()).isEqualTo(BD_ZERO_DOLLARS);
            assertThat(allocated[TWO].amount()).isEqualTo(BD_ZERO_DOLLARS);
        }

        @Test
        @DisplayName("Should handle negative amounts")
        void shouldHandleNegativeAmounts() {
            Money money = new Money(MINUS_TEN_FIFTY, USD);
            assertThat(money.amount()).isEqualTo(BD_MINUS_TEN_FIFTY);
            
            Money positive = new Money(FIVE_DOLLARS, USD);
            Money result = money.add(positive);
            assertThat(result.amount()).isEqualTo(BD_MINUS_FIVE_FIFTY);
        }

        @Test
        @DisplayName("Should handle very small amounts")
        void shouldHandleVerySmallAmounts() {
            Money money = new Money(ONE_CENT, USD);
            Money[] allocated = money.allocate(THREE);
            
            assertThat(allocated[0].amount()).isEqualTo(BD_ONE_CENT);
            assertThat(allocated[1].amount()).isEqualTo(BD_ZERO_DOLLARS);
            assertThat(allocated[TWO].amount()).isEqualTo(BD_ZERO_DOLLARS);
        }

        @Test
        @DisplayName("Should handle currency with no fraction digits")
        void shouldHandleCurrencyWithNoFractionDigits() {
            Money money = new Money(TWELVE_THIRTY_FOUR_YEN, JPY);
            assertThat(money.amount()).isEqualTo(BD_TWELVE_THIRTY_FOUR);
            
            Money[] allocated = money.allocate(THREE);
            assertThat(allocated[0].amount()).isEqualTo(BD_FOUR_TWELVE);
            assertThat(allocated[1].amount()).isEqualTo(BD_FOUR_ELEVEN);
            assertThat(allocated[TWO].amount()).isEqualTo(BD_FOUR_ELEVEN);
        }
    }
}