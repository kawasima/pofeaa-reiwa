package pofeaa.original.domainlogic.tablemodule;

import org.jooq.DSLContext;

public class Contract {
    private final DSLContext ctx;

    public Contract(DSLContext ctx) {
        this.ctx = ctx;
    }

    public void calculateRecognitions(long contractId) {
        RevenueRecognition revenueRecognition = new RevenueRecognition(ctx);
        Product product = new Product(ctx);

    }
}
