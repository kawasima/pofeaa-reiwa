package pofeaa.original.base.servicestub;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ChargeGenerator {
    private final TaxService taxService;

    public ChargeGenerator(TaxService taxService) {
        this.taxService = taxService;
    }

    public Charge[] calculateCharges(BillingSchedule schedule) {
        List<Charge> charges = new ArrayList<>();
        Charge baseCharge = new Charge(schedule.getBillingAmount(), false);
        charges.add(baseCharge);

        TaxInfo info = taxService.getSalesTaxInfo(
                schedule.getProduct(),
                schedule.getAddress(),
                schedule.getBillingAmount()
        );

        if (info.getStateRate().compareTo(BigDecimal.ZERO) > 0) {
            Charge taxCharge = new Charge(info.getStateAmount(), true);
            charges.add(taxCharge);
        }
        return charges.toArray(Charge[]::new);
    }
}
