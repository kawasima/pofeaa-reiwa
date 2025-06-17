package pofeaa.original.domainlogic.tablemodule;

import org.jooq.DSLContext;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class Product {
    private final DSLContext ctx;

    public Product(DSLContext ctx) {
        this.ctx = ctx;
    }

    public ProductType getProductType(long id) {
        return ctx.select(field("type"))
           .from(table("products"))
           .where(field("id").eq(id))
           .fetchOne()
           .map(record -> ProductType.valueOf(record.get("type", String.class)));
    }
}
