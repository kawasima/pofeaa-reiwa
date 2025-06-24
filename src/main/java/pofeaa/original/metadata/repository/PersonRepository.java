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
                .where(field("benefactor_id").eq(person.getId().getValue()))
                .fetch()
                .map(record -> {
                    Integer numDependents = record.getValue(3, Integer.class);
                    return new Person(
                            Identity.of(record.getValue(0, Long.class)),
                            record.getValue(1, String.class),
                            record.getValue(2, String.class),
                            numDependents != null ? numDependents : 0
                    );
                });
    }
}
