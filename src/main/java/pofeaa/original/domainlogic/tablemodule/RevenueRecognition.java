package pofeaa.original.domainlogic.tablemodule;

import org.jooq.DSLContext;
import static org.jooq.impl.DSL.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RevenueRecognition extends TableModule {
    public RevenueRecognition(DSLContext ctx) {
        super(table("revenue_recognitions"), ctx);
    }

    public long insert(long contractId, BigDecimal amount, LocalDate date) {
       return ctx.insertInto(table("revenue_recognitions"))
                .set(field("contract_id"), contractId)
                .set(field("amount"), amount)
                .set(field("recognized_on"), date)
                .returning()
                .execute();
    }
}
