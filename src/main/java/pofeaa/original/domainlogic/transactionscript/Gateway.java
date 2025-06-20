package pofeaa.original.domainlogic.transactionscript;

import pofeaa.original.base.money.Money;
import org.jooq.DSLContext;
import org.jooq.Record;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

/**
 * Table Data Gateway for Revenue Recognition.
 */
public class Gateway {
    private final DSLContext ctx;

    public Gateway(DSLContext ctx) {
        this.ctx = ctx;
    }

    public List<BigDecimal> findRecognitionsFor(long contractId, LocalDate asof) {
        return ctx.select(field("amount"))
                .from(table("revenue_recognitions"))
                .where(field("contract_id").eq(contractId))
                        .and(field("recognized_on").le(asof))
                .fetch()
                .map(r -> r.get("amount", BigDecimal.class));

    }

    public Record findContract(long contractId) {
        return ctx.select(field("revenue"),
                          field("date_signed"),
                          field("type"))
                .from(table("contracts"))
                .join(table("products"))
                .on(field("contracts.product_id").eq(field("products.id")))
                .where(field("contracts.id").eq(contractId))
                .fetchOne();
    }

    public void insertRecognition(long contractId, Money amount, LocalDate asOf) {
        ctx.insertInto(table("revenue_recognitions"))
                .set(field("contract_id"), contractId)
                .set(field("amount"), amount.amount())
                .set(field("recognized_on"), asOf)
                .execute();
    }
}
