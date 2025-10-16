package pofeaa.original.behavior.lazyload;

import org.jooq.DSLContext;
import org.jooq.Record;

import java.util.List;

public class SupplierMapper {
    private final DSLContext ctx;
    private final ProductMapper productMapper;

    public SupplierMapper(DSLContext ctx) {
        this.ctx = ctx;
        this.productMapper = new ProductMapper(ctx);
    }

    protected Supplier doLoad(Long id, Record record) {
        return new Supplier(
                id,
                record.get("name", String.class),
                new ProductsLoader(id, productMapper)
        );
    }

    public Supplier findById(Long id) {
        Record record = ctx.select()
                .from("suppliers")
                .where("id = ?", id)
                .fetchOne();
        if (record == null) {
            return null;
        }
        return doLoad(id, record);
    }
}
