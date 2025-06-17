package pofeaa.original.datasource.rowdatagateway;

import org.jooq.DSLContext;

import java.util.List;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class PersonFinder {
    private final DSLContext ctx;

    public PersonFinder(DSLContext ctx) {
        this.ctx = ctx;
    }

    public PersonGateway find(Long id) {
        PersonGateway person = Registry.getPerson(id);
        if (person != null) {
            return person;
        }
        return ctx.select()
                .from(table("persons"))
                .where(field("id").eq(id))
                .fetchOne()
                .map(record -> PersonGateway.load(ctx, record));
    }

    public List<PersonGateway> findResponsibles() {
        return ctx.select()
                .from(table("persons"))
                .where(field("number_of_dependents").gt(0))
                .fetch()
                .map(record -> PersonGateway.load(ctx, record));
    }
}
