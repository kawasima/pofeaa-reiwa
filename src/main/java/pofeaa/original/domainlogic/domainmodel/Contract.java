package pofeaa.original.domainlogic.domainmodel;

import pofeaa.original.base.money.Money;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Contract {
    private final Product product;
    private final Money revenue;
    private final LocalDate whenSigned;

    private final List<RevenueRecognition> revenueRecognitions = new ArrayList<>();

    public Contract(Product product, Money revenue, LocalDate whenSigned) {
        this.product = product;
        this.revenue = revenue;
        this.whenSigned = whenSigned;
    }

    public void calculateRecognitions() {
        product.calculateRevenueRecognitions(this);
    }

    public Money recognizedRevenue(LocalDate asOf) {
        return revenueRecognitions.stream()
                .filter(rr -> rr.isRecognizableBy(asOf))
                .map(RevenueRecognition::getAmount)
                .reduce(Money.dollars(BigDecimal.ZERO), Money::add);
    }

    public void addRevenueRecognition(RevenueRecognition revenueRecognition) {
        revenueRecognitions.add(revenueRecognition);
    }

    public Money getRevenue() {
        return revenue;
    }

    public LocalDate getWhenSigned() {
        return whenSigned;
    }
}
