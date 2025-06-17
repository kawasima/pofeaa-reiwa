package pofeaa.original.datasource.tabledatagateway;

import org.jooq.DSLContext;
import org.jooq.Record;

import java.util.List;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class PersonGateway {
    private final DSLContext ctx;

    public PersonGateway(DSLContext ctx) {
        this.ctx = ctx;
    }

    public List<Person> findAll() {
        return ctx.select()
                .from(table("persons"))
                .fetch()
                .map(record -> {
                    Person person = new Person();
                    person.setId(record.get("id", Long.class));
                    person.setFirstName(record.get("first_name", String.class));
                    person.setLastName(record.get("last_name", String.class));
                    person.setNumberOfDependents(record.get("number_of_dependents", Integer.class));
                    return person;
                });
    }

    public void update(Person person) {
        Record record = ctx.newRecord(table("persons"));
        record.set(field("first_name"), person.getFirstName());
        record.set(field("last_name"), person.getLastName());
        record.set(field("number_of_dependents"), person.getNumberOfDependents());

        ctx.update(table("persons"))
                .set(record)
                .where("id = ?", person.getId())
                .execute();
    }
}
