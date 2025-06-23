package pofeaa.original.base.servicestub;

import pofeaa.original.base.money.Money;

public class BillingSchedule {
    private final Money billingAmount;
    private final String product;
    private final Address address;

    public BillingSchedule(Money amount, String product, Address address) {
        this.billingAmount = amount;
        this.product = product;
        this.address = address;
    }

    public Money getBillingAmount() {
        return billingAmount;
    }

    public String getProduct() {
        return product;
    }

    public Address getAddress() {
        return address;
    }
}
