package pofeaa.original.base.servicestub;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pofeaa.original.base.money.Money;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Test for ChargeGenerator to verify it works with the service stub classes.
 */
@DisplayName("ChargeGenerator Service Stub Tests")
class ChargeGeneratorTest {

    @Test
    @DisplayName("Should generate charges with tax when tax service returns tax info")
    void shouldGenerateChargesWithTaxWhenTaxServiceReturnsTaxInfo() {
        // Given
        TaxService taxService = new MockTaxService();
        ChargeGenerator generator = new ChargeGenerator(taxService);
        
        Address address = Address.usAddress("123 Main St", "New York", "NY", "10001");
        Money amount = Money.dollars(new BigDecimal("100.00"));
        BillingSchedule schedule = new BillingSchedule(amount, "WIDGET", address);
        
        // When
        Charge[] charges = generator.calculateCharges(schedule);
        
        // Then
        assertThat(charges).hasSize(2);
        
        // Base charge (not taxable)
        Charge baseCharge = charges[0];
        assertThat(baseCharge.getAmount().amount()).isEqualTo(new BigDecimal("100.00"));
        assertThat(baseCharge.isTaxable()).isFalse();
        
        // Tax charge (taxable)
        Charge taxCharge = charges[1];
        assertThat(taxCharge.getAmount().amount()).isEqualTo(new BigDecimal("8.50"));
        assertThat(taxCharge.isTaxable()).isTrue();
    }

    @Test
    @DisplayName("Should generate only base charge when no tax applies")
    void shouldGenerateOnlyBaseChargeWhenNoTaxApplies() {
        // Given
        TaxService noTaxService = new NoTaxService();
        ChargeGenerator generator = new ChargeGenerator(noTaxService);
        
        Address address = Address.usAddress("123 Main St", "Portland", "OR", "97201");
        Money amount = Money.dollars(new BigDecimal("50.00"));
        BillingSchedule schedule = new BillingSchedule(amount, "SERVICE", address);
        
        // When
        Charge[] charges = generator.calculateCharges(schedule);
        
        // Then
        assertThat(charges).hasSize(1);
        
        Charge baseCharge = charges[0];
        assertThat(baseCharge.getAmount().amount()).isEqualTo(new BigDecimal("50.00"));
        assertThat(baseCharge.isTaxable()).isFalse();
    }

    /**
     * Mock TaxService that returns a fixed tax rate of 8.5%
     */
    private static class MockTaxService implements TaxService {
        @Override
        public TaxInfo getSalesTaxInfo(String productCode, Address addr, Money saleAmount) {
            BigDecimal taxRate = new BigDecimal("0.085"); // 8.5%
            Money taxAmount = Money.dollars(
                saleAmount.amount().multiply(taxRate)
            );
            return new TaxInfo(taxRate, taxAmount);
        }
    }

    /**
     * Mock TaxService that returns no tax (0% rate)
     */
    private static class NoTaxService implements TaxService {
        @Override
        public TaxInfo getSalesTaxInfo(String productCode, Address addr, Money saleAmount) {
            return new TaxInfo(BigDecimal.ZERO, Money.dollars(BigDecimal.ZERO));
        }
    }
}