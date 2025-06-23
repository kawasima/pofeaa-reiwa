package pofeaa.original.domainlogic.domainmodel;

import org.junit.jupiter.api.Test;
import pofeaa.original.base.money.Money;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ContractTest {
    @Test
    void recognitionForSpreadsheet() {
        Product calc = Product.newSpreadsheet("Thinking Calculator");
        LocalDate dateSigned = LocalDate.of(2023, 1, 1);
        Contract contract = new Contract(calc,
                Money.dollars(new BigDecimal("1000.00")),
                dateSigned);
        contract.calculateRecognitions();
        assertThat(contract.recognizedRevenue(dateSigned))
                .isEqualTo(Money.dollars(new BigDecimal("333.34")));
        assertThat(contract.recognizedRevenue(dateSigned.plusDays(59)))
                .isEqualTo(Money.dollars(new BigDecimal("333.34")));
        assertThat(contract.recognizedRevenue(dateSigned.plusDays(60)))
                .isEqualTo(Money.dollars(new BigDecimal("666.67")));
        assertThat(contract.recognizedRevenue(dateSigned.plusDays(89)))
                .isEqualTo(Money.dollars(new BigDecimal("666.67")));
        assertThat(contract.recognizedRevenue(dateSigned.plusDays(90)))
                .isEqualTo(Money.dollars(new BigDecimal("1000.00")));

    }

    @Test
    void recognitionForWordProcessor() {
        Product calc = Product.newWordProcessor("Thinking Word Processor");
        LocalDate dateSigned = LocalDate.of(2023, 1, 1);
        Contract contract = new Contract(calc,
                Money.dollars(new BigDecimal("1000.00")),
                dateSigned);
        contract.calculateRecognitions();
        assertThat(contract.recognizedRevenue(dateSigned))
                .isEqualTo(Money.dollars(new BigDecimal("1000.00")));
        assertThat(contract.recognizedRevenue(dateSigned.plusDays(100)))
                .isEqualTo(Money.dollars(new BigDecimal("1000.00")));
    }

    @Test
    void recognitionForDatabase() {
        Product calc = Product.newDatabase("Thinking Database");
        LocalDate dateSigned = LocalDate.of(2023, 1, 1);
        Contract contract = new Contract(calc,
                Money.dollars(new BigDecimal("1000.00")),
                dateSigned);
        contract.calculateRecognitions();
        assertThat(contract.recognizedRevenue(dateSigned))
                .isEqualTo(Money.dollars(new BigDecimal("333.34")));
        assertThat(contract.recognizedRevenue(dateSigned.plusDays(29)))
                .isEqualTo(Money.dollars(new BigDecimal("333.34")));
        assertThat(contract.recognizedRevenue(dateSigned.plusDays(30)))
                .isEqualTo(Money.dollars(new BigDecimal("666.67")));
        assertThat(contract.recognizedRevenue(dateSigned.plusDays(59)))
                .isEqualTo(Money.dollars(new BigDecimal("666.67")));
        assertThat(contract.recognizedRevenue(dateSigned.plusDays(60)))
                .isEqualTo(Money.dollars(new BigDecimal("1000.00")));
    }
}