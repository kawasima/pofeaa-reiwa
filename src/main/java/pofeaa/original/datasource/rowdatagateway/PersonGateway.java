package pofeaa.original.datasource.rowdatagateway;

import org.jooq.DSLContext;
import org.jooq.Record;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class PersonGateway {
    private final DSLContext ctx;

    public PersonGateway(DSLContext ctx) {
        this.ctx = ctx;
    }

    private Long id;
    private String firstName;
    private String lastName;
    private Integer numberOfDependents;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Integer getNumberOfDependents() {
        return numberOfDependents;
    }

    public void setNumberOfDependents(Integer numberOfDependents) {
        this.numberOfDependents = numberOfDependents;
    }

    public void update() {
        Record record = ctx.newRecord(table("persons"));
        record.set(field("first_name"), firstName);
        record.set(field("last_name"), lastName);
        record.set(field("number_of_dependents"), numberOfDependents);

        ctx.update(table("persons"))
                .set(record)
                .where(field("id").eq(id))
                .execute();
    }

    public void insert() {
        ctx.insertInto(table("persons"))
                .set(field("first_name"), firstName)
                .set(field("last_name"), lastName)
                .set(field("number_of_dependents"), numberOfDependents)
                .execute();
        Registry.addPerson(this);
    }

    public static PersonGateway load(DSLContext ctx, Record record) {
        // Use getValue with index to avoid field name case issues
        Long id = record.getValue(0, Long.class);
        PersonGateway person = Registry.getPerson(id);
        if (person == null) {
            person = new PersonGateway(ctx);
            person.setId(id);
            person.setFirstName(record.getValue(1, String.class));
            person.setLastName(record.getValue(2, String.class));
            person.setNumberOfDependents(record.getValue(3, Integer.class));
            Registry.addPerson(person);
        }
        return person;
    }
}
