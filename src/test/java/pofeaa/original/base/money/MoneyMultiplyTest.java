package pofeaa.original.base.money;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;

import static org.assertj.core.api.Assertions.*;

/**
 * Test for the multiply method in Money class.
 */
@DisplayName("Money Multiply Method Tests")
class MoneyMultiplyTest {

    @Test
    @DisplayName("Should multiply Money by BigDecimal factor")
    void shouldMultiplyMoneyByBigDecimalFactor() {
        // Given
        Money money = Money.dollars(new BigDecimal("10.50"));
        BigDecimal factor = new BigDecimal("2.5");
        
        // When
        Money result = money.multiply(factor);
        
        // Then
        assertThat(result.amount()).isEqualTo(new BigDecimal("26.25"));
        assertThat(result.currency()).isEqualTo(Currency.getInstance(Locale.US));
    }

    @Test
    @DisplayName("Should multiply Money by zero")
    void shouldMultiplyMoneyByZero() {
        // Given
        Money money = Money.dollars(new BigDecimal("100.00"));
        BigDecimal factor = BigDecimal.ZERO;
        
        // When
        Money result = money.multiply(factor);
        
        // Then
        assertThat(result.amount()).isEqualTo(new BigDecimal("0.00"));
        assertThat(result.currency()).isEqualTo(Currency.getInstance(Locale.US));
    }

    @Test
    @DisplayName("Should multiply Money by one")
    void shouldMultiplyMoneyByOne() {
        // Given
        Money money = Money.dollars(new BigDecimal("42.75"));
        BigDecimal factor = BigDecimal.ONE;
        
        // When
        Money result = money.multiply(factor);
        
        // Then
        assertThat(result.amount()).isEqualTo(new BigDecimal("42.75"));
        assertThat(result.currency()).isEqualTo(Currency.getInstance(Locale.US));
    }

    @Test
    @DisplayName("Should multiply Money by fractional amount")
    void shouldMultiplyMoneyByFractionalAmount() {
        // Given
        Money money = Money.dollars(new BigDecimal("100.00"));
        BigDecimal factor = new BigDecimal("0.08"); // 8%
        
        // When
        Money result = money.multiply(factor);
        
        // Then
        assertThat(result.amount()).isEqualTo(new BigDecimal("8.00"));
        assertThat(result.currency()).isEqualTo(Currency.getInstance(Locale.US));
    }

    @Test
    @DisplayName("Should handle rounding correctly")
    void shouldHandleRoundingCorrectly() {
        // Given
        Money money = Money.dollars(new BigDecimal("10.00"));
        BigDecimal factor = new BigDecimal("0.333"); // Results in 3.33
        
        // When
        Money result = money.multiply(factor);
        
        // Then
        assertThat(result.amount()).isEqualTo(new BigDecimal("3.33"));
        assertThat(result.currency()).isEqualTo(Currency.getInstance(Locale.US));
    }

    @Test
    @DisplayName("Should preserve currency when multiplying")
    void shouldPreserveCurrencyWhenMultiplying() {
        // Given
        Money euroMoney = new Money(50.0, Currency.getInstance("EUR"));
        BigDecimal factor = new BigDecimal("1.5");
        
        // When
        Money result = euroMoney.multiply(factor);
        
        // Then
        assertThat(result.amount()).isEqualTo(new BigDecimal("75.00"));
        assertThat(result.currency()).isEqualTo(Currency.getInstance("EUR"));
    }

    @Test
    @DisplayName("Should throw exception when multiplying by null")
    void shouldThrowExceptionWhenMultiplyingByNull() {
        // Given
        Money money = Money.dollars(new BigDecimal("10.00"));
        
        // When/Then
        assertThatThrownBy(() -> money.multiply(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cannot multiply Money by null");
    }

    @Test
    @DisplayName("Should multiply negative Money amounts")
    void shouldMultiplyNegativeMoneyAmounts() {
        // Given - Create negative money by subtracting larger amount
        Money positiveMoney = Money.dollars(new BigDecimal("100.00"));
        Money largeMoney = Money.dollars(new BigDecimal("150.00"));
        Money negativeMoney = positiveMoney.subtract(largeMoney);
        BigDecimal factor = new BigDecimal("2.0");
        
        // When
        Money result = negativeMoney.multiply(factor);
        
        // Then
        assertThat(result.amount()).isEqualTo(new BigDecimal("-100.00"));
        assertThat(result.currency()).isEqualTo(Currency.getInstance(Locale.US));
    }

    @Test
    @DisplayName("Should multiply by negative factor")
    void shouldMultiplyByNegativeFactor() {
        // Given
        Money money = Money.dollars(new BigDecimal("25.00"));
        BigDecimal factor = new BigDecimal("-2.0");
        
        // When
        Money result = money.multiply(factor);
        
        // Then
        assertThat(result.amount()).isEqualTo(new BigDecimal("-50.00"));
        assertThat(result.currency()).isEqualTo(Currency.getInstance(Locale.US));
    }

    @Test
    @DisplayName("Should handle precision with tax calculations")
    void shouldHandlePrecisionWithTaxCalculations() {
        // Given - Simulating tax calculation scenario
        Money purchaseAmount = Money.dollars(new BigDecimal("19.99"));
        BigDecimal taxRate = new BigDecimal("0.08375"); // 8.375% tax rate
        
        // When
        Money taxAmount = purchaseAmount.multiply(taxRate);
        
        // Then - Should properly round to nearest cent
        assertThat(taxAmount.amount()).isEqualTo(new BigDecimal("1.67")); // 19.99 * 0.08375 = 1.6741625, rounded to 1.67
        assertThat(taxAmount.currency()).isEqualTo(Currency.getInstance(Locale.US));
    }
}