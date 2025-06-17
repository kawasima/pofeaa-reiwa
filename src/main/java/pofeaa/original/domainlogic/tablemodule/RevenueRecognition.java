package pofeaa.original.domainlogic.tablemodule;

import org.jooq.DSLContext;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class RevenueRecognition {
    private final DSLContext ctx;

    public RevenueRecognition(DSLContext ctx) {
        this.ctx = ctx;
    }

    public long insert(long contractId, BigDecimal amount, LocalDate date) {
        int execute = ctx.insertInto(table("revenue_recognitions"))
                .set(field("contract_id"), contractId)
                .set(field("amount"), amount)
                .set(field("recognized_on"), date)
                .returning()
                .execute();
        return 1; // Return the ID of the inserted record
    }
}
