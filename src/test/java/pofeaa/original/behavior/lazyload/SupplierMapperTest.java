package pofeaa.original.behavior.lazyload;

import org.h2.jdbcx.JdbcDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

class SupplierMapperTest {
    DSLContext ctx;
    SupplierMapper supplierMapper;

    @BeforeEach
    void setup() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        ctx = DSL.using(ds, SQLDialect.H2);
        ctx.createTable(table("suppliers"))
                .column(field("id", Long.class))
                .column(field("name", String.class))
                .execute();
        supplierMapper = new SupplierMapper(ctx);
    }

    @AfterEach
    void tearDown() {
        ctx.dropTable(table("suppliers")).execute();
    }


    @Test
    void test() {
    }
}