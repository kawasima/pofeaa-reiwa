package pofeaa.original.behavior.lazyload;

import org.jooq.DSLContext;
import org.jooq.Record;

import java.util.List;

public class SupplierMapper {
    private final DSLContext ctx;

    public SupplierMapper(DSLContext ctx) {
        this.ctx = ctx;
    }

    protected Supplier doLoad(Long id, Record record) {
        return new Supplier(
                id,
                record.get("name", String.class),
                new VirtualList<>(new ProductLoader(id, ctx))
        );
    }

    public static class ProductLoader implements VirtualListLoader<Product> {
        private final Long id;
        private final DSLContext ctx;

        public ProductLoader(Long id, DSLContext ctx) {
            this.id = id;
            this.ctx = ctx;
        }

        @Override
        public List<Product> load() {
            return ProductMapper.create(ctx)
                    .findForSupplier(id);
        }
    }
}
