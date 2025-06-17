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
                new VirtualList<>(new ProductLoader(id))
        );
    }

    public static class ProductLoader implements VirtualListLoader<Product> {
        private final Long id;

        public ProductLoader(Long id) {
            this.id = id;
        }

        @Override
        public List<Product> load() {
            return ProductMapper.create(ctx)
                    .findForSupplier(id);
        }
    }
}
