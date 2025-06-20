package pofeaa.original.metadata.metadatamapping;

import org.h2.jdbcx.JdbcDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

class PersonMapperTest {
    private DSLContext ctx;

    @BeforeEach
    void setup() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        ctx = DSL.using(ds, SQLDialect.H2);
        ctx.createTable(table("persons"))
                .column(field("id", SQLDataType.BIGINT.identity(true)))
                .column(field("first_name", SQLDataType.VARCHAR(255)))
                .column(field("last_name", SQLDataType.VARCHAR(255)))
                .column(field("number_of_dependents", SQLDataType.INTEGER))
                .execute();
        ctx.insertInto(table("persons"))
                .set(field("first_name"), "John")
                .set(field("last_name"), "Doe")
                .set(field("number_of_dependents"), 2)
                .execute();
        ctx.commit();
    }

    @Test
    void test() {
        PersonMapper personMapper = new PersonMapper(ctx);
        Set<Person> people = personMapper.findLastNamesLike("oe");
        System.out.println(people);
    }
}