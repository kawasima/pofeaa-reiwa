package pofeaa.original.datasource.datamapper;

import org.jooq.DSLContext;
import org.jooq.Record;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class PersonMapper {
    private final DSLContext ctx;

    public PersonMapper(DSLContext ctx) {
        this.ctx = ctx;
    }

    public Person find(Identity id) {
        Record record = ctx.select()
                .from(table("persons"))
                .where(field("id").eq(id.getValue()))
                .fetchOne();
        return doLoad(record);
    }

    protected Person doLoad(Record record) {
        if (record == null) {
            return null;
        }
        // Use getValue with index to avoid field name case issues
        Long id = record.getValue(0, Long.class);
        String firstName = record.getValue(1, String.class);
        String lastName = record.getValue(2, String.class);
        Integer numberOfDependents = record.getValue(3, Integer.class);
        return new Person(Identity.of(id), firstName, lastName, numberOfDependents);
    }
}
