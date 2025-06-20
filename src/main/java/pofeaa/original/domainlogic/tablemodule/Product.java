package pofeaa.original.domainlogic.tablemodule;

import org.jooq.DSLContext;

import static org.jooq.impl.DSL.table;

public class Product extends TableModule {
    public Product(DSLContext ctx) {
        super(table("products"), ctx);
    }

    public String getName(long id) {
        return find(id).getValue("NAME", String.class);
    }

    public ProductType getProductType(long id) {
        String type = find(id).getValue("TYPE", String.class);
        return ProductType.fromCode(type);
    }
}
