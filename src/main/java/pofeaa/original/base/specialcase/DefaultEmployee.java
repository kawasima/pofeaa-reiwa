package pofeaa.original.base.specialcase;

import java.math.BigDecimal;

public class DefaultEmployee implements Employee {
    private final String name;
    private final Contract contract;

    public DefaultEmployee(String name, Contract contract) {
        this.name = name;
        this.contract = contract;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public BigDecimal getGrossToDate() {
        return calculateGrossFromPeriod(0);
    }

    @Override
    public Contract getContract() {
        return contract;
    }

    public BigDecimal calculateGrossFromPeriod(int period) {
        if (contract == null || contract == Contract.NULL) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalGross = BigDecimal.ZERO;
        
        // Sum all payments from period 0 up to and including the specified period
        // Period 0 is the most recent, higher numbers are older periods
        for (int currentPeriod = 0; currentPeriod <= period; currentPeriod++) {
            BigDecimal periodPayment = contract.getPaymentForPeriod(currentPeriod);
            totalGross = totalGross.add(periodPayment);
        }
        
        return totalGross;
    }
}
