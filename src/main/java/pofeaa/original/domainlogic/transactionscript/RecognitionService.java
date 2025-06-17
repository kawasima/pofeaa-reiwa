package pofeaa.original.domainlogic.transactionscript;

import pofeaa.original.base.money.Money;
import org.jooq.Record;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Locale;

public class RecognitionService {
    private final Gateway gateway;

    public RecognitionService(Gateway gateway) {
        this.gateway = gateway;
    }

    public Money recognizedRevenue(long contractNumber, LocalDate asof) {
        Currency currency = Currency.getInstance(Locale.US);
        return gateway.findRecognitionsFor(contractNumber, asof)
                .stream()
                .map(amount -> new Money(amount.doubleValue(), currency))
                .reduce(Money.dollars(BigDecimal.ZERO), Money::add);
    }

    public void calculateRevenueRecognitions(long contractNumber) {
        Record contract = gateway.findContract(contractNumber);
        Money totalRevenue = Money.dollars(contract.get("revenue", BigDecimal.class));
        LocalDate recognitionDate = contract.get("date_signed", LocalDate.class);
        String type = contract.get("type", String.class);

        switch(type) {
            case "S": {
                Money[] allocation = totalRevenue.allocate(3);
                gateway.insertRecognition(contractNumber, allocation[0], recognitionDate);
                gateway.insertRecognition(contractNumber, allocation[1], recognitionDate.plusDays(60));
                gateway.insertRecognition(contractNumber, allocation[2], recognitionDate.plusDays(120));
            }
            case "W": {
                gateway.insertRecognition(contractNumber, totalRevenue, recognitionDate);
            }
            case "D": {
                Money[] allocation = totalRevenue.allocate(3);
                gateway.insertRecognition(contractNumber, allocation[0], recognitionDate);
                gateway.insertRecognition(contractNumber, allocation[1], recognitionDate.plusDays(30));
                gateway.insertRecognition(contractNumber, allocation[2], recognitionDate.plusDays(60));
            }
        }
    }
}
