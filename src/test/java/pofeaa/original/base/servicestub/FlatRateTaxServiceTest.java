package pofeaa.original.base.servicestub;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pofeaa.original.base.money.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.*;

@DisplayName("FlatRateTaxService Tests")
class FlatRateTaxServiceTest {

    private FlatRateTaxService taxService;
    private static final BigDecimal EXPECTED_RATE = new BigDecimal("0.0500");

    @BeforeEach
    void setUp() {
        taxService = new FlatRateTaxService();
    }

    @Nested
    @DisplayName("Tax Rate Calculation")
    class TaxRateCalculation {

        @Test
        @DisplayName("Should return correct flat tax rate of 5%")
        void shouldReturnCorrectFlatTaxRate() {
            Address address = Address.usAddress("123 Main St", "New York", "NY", "10001");
            Money saleAmount = Money.dollars(new BigDecimal("100.00"));

            TaxInfo taxInfo = taxService.getSalesTaxInfo("PRODUCT", address, saleAmount);

            assertThat(taxInfo.getStateRate()).isEqualTo(EXPECTED_RATE);
        }

        @Test
        @DisplayName("Should calculate tax amount as 5% of sale amount")
        void shouldCalculateTaxAmountAsFivePercentOfSaleAmount() {
            Address address = Address.usAddress("456 Oak Ave", "Los Angeles", "CA", "90001");
            Money saleAmount = Money.dollars(new BigDecimal("200.00"));

            TaxInfo taxInfo = taxService.getSalesTaxInfo("WIDGET", address, saleAmount);

            BigDecimal expectedTaxAmount = new BigDecimal("10.00");
            assertThat(taxInfo.getStateAmount().amount()).isEqualTo(expectedTaxAmount);
        }

        @ParameterizedTest
        @ValueSource(strings = {"10.00", "50.00", "100.00", "250.00", "999.99", "1000.00"})
        @DisplayName("Should calculate correct tax for various amounts")
        void shouldCalculateCorrectTaxForVariousAmounts(String amountStr) {
            BigDecimal amount = new BigDecimal(amountStr);
            Address address = Address.usAddress("789 Pine St", "Chicago", "IL", "60601");
            Money saleAmount = Money.dollars(amount);

            TaxInfo taxInfo = taxService.getSalesTaxInfo("ITEM", address, saleAmount);

            BigDecimal expectedTax = amount.multiply(EXPECTED_RATE).setScale(2, RoundingMode.HALF_UP);
            assertThat(taxInfo.getStateAmount().amount()).isEqualTo(expectedTax);
            assertThat(taxInfo.getStateRate()).isEqualTo(EXPECTED_RATE);
        }
    }

    @Nested
    @DisplayName("Product Code Independence")
    class ProductCodeIndependence {

        @Test
        @DisplayName("Should apply same rate regardless of product code")
        void shouldApplySameRateRegardlessOfProductCode() {
            Address address = Address.usAddress("321 Elm St", "Boston", "MA", "02101");
            Money saleAmount = Money.dollars(new BigDecimal("100.00"));

            TaxInfo taxInfo1 = taxService.getSalesTaxInfo("ELECTRONICS", address, saleAmount);
            TaxInfo taxInfo2 = taxService.getSalesTaxInfo("CLOTHING", address, saleAmount);
            TaxInfo taxInfo3 = taxService.getSalesTaxInfo("FOOD", address, saleAmount);
            TaxInfo taxInfo4 = taxService.getSalesTaxInfo(null, address, saleAmount);

            assertThat(taxInfo1.getStateRate()).isEqualTo(EXPECTED_RATE);
            assertThat(taxInfo2.getStateRate()).isEqualTo(EXPECTED_RATE);
            assertThat(taxInfo3.getStateRate()).isEqualTo(EXPECTED_RATE);
            assertThat(taxInfo4.getStateRate()).isEqualTo(EXPECTED_RATE);

            assertThat(taxInfo1.getStateAmount()).isEqualTo(taxInfo2.getStateAmount());
            assertThat(taxInfo2.getStateAmount()).isEqualTo(taxInfo3.getStateAmount());
            assertThat(taxInfo3.getStateAmount()).isEqualTo(taxInfo4.getStateAmount());
        }
    }

    @Nested
    @DisplayName("Address Independence")
    class AddressIndependence {

        @Test
        @DisplayName("Should apply same rate for US addresses")
        void shouldApplySameRateForUSAddresses() {
            Money saleAmount = Money.dollars(new BigDecimal("100.00"));

            Address nyAddress = Address.usAddress("123 Broadway", "New York", "NY", "10001");
            Address caAddress = Address.usAddress("456 Sunset Blvd", "Los Angeles", "CA", "90001");
            Address txAddress = Address.usAddress("789 Main St", "Houston", "TX", "77001");

            TaxInfo nyTaxInfo = taxService.getSalesTaxInfo("PRODUCT", nyAddress, saleAmount);
            TaxInfo caTaxInfo = taxService.getSalesTaxInfo("PRODUCT", caAddress, saleAmount);
            TaxInfo txTaxInfo = taxService.getSalesTaxInfo("PRODUCT", txAddress, saleAmount);

            assertThat(nyTaxInfo.getStateRate()).isEqualTo(EXPECTED_RATE);
            assertThat(caTaxInfo.getStateRate()).isEqualTo(EXPECTED_RATE);
            assertThat(txTaxInfo.getStateRate()).isEqualTo(EXPECTED_RATE);

            assertThat(nyTaxInfo.getStateAmount()).isEqualTo(caTaxInfo.getStateAmount());
            assertThat(caTaxInfo.getStateAmount()).isEqualTo(txTaxInfo.getStateAmount());
        }

        @Test
        @DisplayName("Should apply same rate for UK addresses")
        void shouldApplySameRateForUKAddresses() {
            Money saleAmount = Money.dollars(new BigDecimal("100.00"));

            Address ukAddress = Address.ukAddress(
                "10 Downing Street",
                null,
                "London",
                "Westminster",
                "SW1A 2AA"
            );

            TaxInfo taxInfo = taxService.getSalesTaxInfo("PRODUCT", ukAddress, saleAmount);

            assertThat(taxInfo.getStateRate()).isEqualTo(EXPECTED_RATE);
            assertThat(taxInfo.getStateAmount().amount()).isEqualTo(new BigDecimal("5.00"));
        }

        @Test
        @DisplayName("Should apply same rate for Japanese addresses")
        void shouldApplySameRateForJapaneseAddresses() {
            Money saleAmount = Money.dollars(new BigDecimal("100.00"));

            Address jpAddress = Address.japaneseAddress(
                "100-0001",
                "Tokyo",
                "Chiyoda",
                "1-1-1 Imperial Palace"
            );

            TaxInfo taxInfo = taxService.getSalesTaxInfo("PRODUCT", jpAddress, saleAmount);

            assertThat(taxInfo.getStateRate()).isEqualTo(EXPECTED_RATE);
            assertThat(taxInfo.getStateAmount().amount()).isEqualTo(new BigDecimal("5.00"));
        }

        @Test
        @DisplayName("Should apply same rate for custom built addresses")
        void shouldApplySameRateForCustomBuiltAddresses() {
            Money saleAmount = Money.dollars(new BigDecimal("100.00"));

            Address customAddress = Address.builder()
                .line1("123 Custom St")
                .city("Custom City")
                .country("Custom Country")
                .countryCode("CC")
                .build();

            TaxInfo taxInfo = taxService.getSalesTaxInfo("PRODUCT", customAddress, saleAmount);

            assertThat(taxInfo.getStateRate()).isEqualTo(EXPECTED_RATE);
            assertThat(taxInfo.getStateAmount().amount()).isEqualTo(new BigDecimal("5.00"));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle zero amount")
        void shouldHandleZeroAmount() {
            Address address = Address.usAddress("123 Main St", "New York", "NY", "10001");
            Money saleAmount = Money.dollars(BigDecimal.ZERO);

            TaxInfo taxInfo = taxService.getSalesTaxInfo("PRODUCT", address, saleAmount);

            assertThat(taxInfo.getStateRate()).isEqualTo(EXPECTED_RATE);
            assertThat(taxInfo.getStateAmount().amount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle very small amounts")
        void shouldHandleVerySmallAmounts() {
            Address address = Address.usAddress("123 Main St", "New York", "NY", "10001");
            Money saleAmount = Money.dollars(new BigDecimal("0.01"));

            TaxInfo taxInfo = taxService.getSalesTaxInfo("PRODUCT", address, saleAmount);

            assertThat(taxInfo.getStateRate()).isEqualTo(EXPECTED_RATE);
            BigDecimal expectedTax = new BigDecimal("0.01").multiply(EXPECTED_RATE)
                .setScale(2, RoundingMode.HALF_UP);
            assertThat(taxInfo.getStateAmount().amount()).isEqualTo(expectedTax);
        }

        @Test
        @DisplayName("Should handle very large amounts")
        void shouldHandleVeryLargeAmounts() {
            Address address = Address.usAddress("123 Main St", "New York", "NY", "10001");
            Money saleAmount = Money.dollars(new BigDecimal("1000000.00"));

            TaxInfo taxInfo = taxService.getSalesTaxInfo("PRODUCT", address, saleAmount);

            assertThat(taxInfo.getStateRate()).isEqualTo(EXPECTED_RATE);
            assertThat(taxInfo.getStateAmount().amount()).isEqualTo(new BigDecimal("50000.00"));
        }

        @Test
        @DisplayName("Should handle amounts with many decimal places")
        void shouldHandleAmountsWithManyDecimalPlaces() {
            Address address = Address.usAddress("123 Main St", "New York", "NY", "10001");
            Money saleAmount = Money.dollars(new BigDecimal("99.99"));

            TaxInfo taxInfo = taxService.getSalesTaxInfo("PRODUCT", address, saleAmount);

            assertThat(taxInfo.getStateRate()).isEqualTo(EXPECTED_RATE);
            BigDecimal expectedTax = new BigDecimal("99.99").multiply(EXPECTED_RATE)
                .setScale(2, RoundingMode.HALF_UP);
            assertThat(taxInfo.getStateAmount().amount()).isEqualTo(expectedTax);
        }

        @Test
        @DisplayName("Should handle null product code")
        void shouldHandleNullProductCode() {
            Address address = Address.usAddress("123 Main St", "New York", "NY", "10001");
            Money saleAmount = Money.dollars(new BigDecimal("100.00"));

            TaxInfo taxInfo = taxService.getSalesTaxInfo(null, address, saleAmount);

            assertThat(taxInfo.getStateRate()).isEqualTo(EXPECTED_RATE);
            assertThat(taxInfo.getStateAmount().amount()).isEqualTo(new BigDecimal("5.00"));
        }

        @Test
        @DisplayName("Should handle empty product code")
        void shouldHandleEmptyProductCode() {
            Address address = Address.usAddress("123 Main St", "New York", "NY", "10001");
            Money saleAmount = Money.dollars(new BigDecimal("100.00"));

            TaxInfo taxInfo = taxService.getSalesTaxInfo("", address, saleAmount);

            assertThat(taxInfo.getStateRate()).isEqualTo(EXPECTED_RATE);
            assertThat(taxInfo.getStateAmount().amount()).isEqualTo(new BigDecimal("5.00"));
        }
    }

    @Nested
    @DisplayName("Consistency Tests")
    class ConsistencyTests {

        @Test
        @DisplayName("Should return consistent results for same inputs")
        void shouldReturnConsistentResultsForSameInputs() {
            Address address = Address.usAddress("123 Main St", "New York", "NY", "10001");
            Money saleAmount = Money.dollars(new BigDecimal("100.00"));

            TaxInfo taxInfo1 = taxService.getSalesTaxInfo("PRODUCT", address, saleAmount);
            TaxInfo taxInfo2 = taxService.getSalesTaxInfo("PRODUCT", address, saleAmount);
            TaxInfo taxInfo3 = taxService.getSalesTaxInfo("PRODUCT", address, saleAmount);

            assertThat(taxInfo1.getStateRate()).isEqualTo(taxInfo2.getStateRate());
            assertThat(taxInfo2.getStateRate()).isEqualTo(taxInfo3.getStateRate());
            assertThat(taxInfo1.getStateAmount()).isEqualTo(taxInfo2.getStateAmount());
            assertThat(taxInfo2.getStateAmount()).isEqualTo(taxInfo3.getStateAmount());
        }

        @Test
        @DisplayName("Should return non-null TaxInfo")
        void shouldReturnNonNullTaxInfo() {
            Address address = Address.usAddress("123 Main St", "New York", "NY", "10001");
            Money saleAmount = Money.dollars(new BigDecimal("100.00"));

            TaxInfo taxInfo = taxService.getSalesTaxInfo("PRODUCT", address, saleAmount);

            assertThat(taxInfo).isNotNull();
            assertThat(taxInfo.getStateRate()).isNotNull();
            assertThat(taxInfo.getStateAmount()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Mathematical Precision")
    class MathematicalPrecision {

        @Test
        @DisplayName("Should maintain precision for fractional cents")
        void shouldMaintainPrecisionForFractionalCents() {
            Address address = Address.usAddress("123 Main St", "New York", "NY", "10001");
            Money saleAmount = Money.dollars(new BigDecimal("33.33"));

            TaxInfo taxInfo = taxService.getSalesTaxInfo("PRODUCT", address, saleAmount);

            BigDecimal expectedTax = new BigDecimal("33.33").multiply(EXPECTED_RATE)
                .setScale(2, RoundingMode.HALF_UP);
            assertThat(taxInfo.getStateAmount().amount()).isEqualTo(expectedTax);
        }

        @Test
        @DisplayName("Should handle rounding correctly")
        void shouldHandleRoundingCorrectly() {
            Address address = Address.usAddress("123 Main St", "New York", "NY", "10001");
            
            Money saleAmount1 = Money.dollars(new BigDecimal("10.01"));
            TaxInfo taxInfo1 = taxService.getSalesTaxInfo("PRODUCT", address, saleAmount1);
            BigDecimal expectedTax1 = new BigDecimal("10.01").multiply(EXPECTED_RATE)
                .setScale(2, RoundingMode.HALF_UP);
            assertThat(taxInfo1.getStateAmount().amount()).isEqualTo(expectedTax1);

            Money saleAmount2 = Money.dollars(new BigDecimal("10.09"));
            TaxInfo taxInfo2 = taxService.getSalesTaxInfo("PRODUCT", address, saleAmount2);
            BigDecimal expectedTax2 = new BigDecimal("10.09").multiply(EXPECTED_RATE)
                .setScale(2, RoundingMode.HALF_UP);
            assertThat(taxInfo2.getStateAmount().amount()).isEqualTo(expectedTax2);
        }
    }
}