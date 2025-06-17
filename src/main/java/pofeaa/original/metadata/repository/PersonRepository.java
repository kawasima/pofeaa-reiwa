package pofeaa.original.metadata.repository;

import org.jooq.DSLContext;
import pofeaa.original.datasource.datamapper.Identity;

import java.util.List;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class PersonRepository {
    private final DSLContext ctx;

    public PersonRepository(DSLContext ctx) {
        this.ctx = ctx;
    }

    public List<Person> dependentsOf(Person person) {
        return ctx.select()
                .from(table("persons"))
                .where(field("benefactor_id").eq(person.getId()))
                .fetch()
                .map(record ->
                    new Person(
                            Identity.of(record.get("id", Long.class)),
                            record.get("first_name", String.class),
                            record.get("last_name", String.class),
                            record.get("number_of_dependents", Integer.class))
                );
    }
}
