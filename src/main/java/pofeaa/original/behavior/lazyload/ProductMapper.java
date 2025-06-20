package pofeaa.original.behavior.lazyload;

import org.jooq.DSLContext;

import java.util.List;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class ProductMapper {
    private final DSLContext ctx;

    public ProductMapper(DSLContext ctx) {
        this.ctx = ctx;
    }

    public static ProductMapper create(DSLContext ctx) {
        return new ProductMapper(ctx);
    }

    public List<Product> findForSupplier(Long supplierId) {
        return ctx.select()
                .from(table("products"))
                .where(field("supplier_id").eq(supplierId))
                .fetch()
                .map(record -> new Product(
                ));
    }
}
