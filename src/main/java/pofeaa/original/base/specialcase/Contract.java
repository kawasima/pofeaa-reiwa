package pofeaa.original.base.specialcase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Contract {
    public static final Contract NULL = new Contract();
    
    private final Map<Integer, BigDecimal> periodPayments = new HashMap<>();
    private LocalDate startDate;
    
    public Contract() {
    }
    
    public Contract(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public void addPaymentForPeriod(int period, BigDecimal amount) {
        periodPayments.put(period, amount);
    }
    
    public BigDecimal getPaymentForPeriod(int period) {
        return periodPayments.getOrDefault(period, BigDecimal.ZERO);
    }
    
    public boolean hasPeriod(int period) {
        return periodPayments.containsKey(period);
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
}
