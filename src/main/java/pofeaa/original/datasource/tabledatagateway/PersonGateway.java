package pofeaa.original.datasource.tabledatagateway;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

import java.util.List;
import java.util.Optional;

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
                    // Use getValue with index to avoid field name case issues
                    person.setId(record.getValue(0, Long.class));
                    person.setFirstName(record.getValue(1, String.class));
                    person.setLastName(record.getValue(2, String.class));
                    person.setAge(record.getValue(3, Integer.class));
                    return person;
                });
    }

    public Optional<Person> findById(Long id) {
        Result<Record> res = ctx.select()
                .from(table("persons"))
                .where(field("id").eq(id))
                .fetch();
        if (res.isEmpty()) {
            return Optional.empty();
        } else {
            Record record = res.get(0);
            Person person = new Person();
            // Use getValue with index to avoid field name case issues
            person.setId(record.getValue(0, Long.class));
            person.setFirstName(record.getValue(1, String.class));
            person.setLastName(record.getValue(2, String.class));
            person.setAge(record.getValue(3, Integer.class));
            return Optional.of(person);
        }
    }

    public void update(Person person) {
        ctx.update(table("persons"))
                .set(field("first_name"), person.getFirstName())
                .set(field("last_name"), person.getLastName())
                .set(field("age"), person.getAge())
                .where(field("id").eq(person.getId()))
                .execute();
    }
}
