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

    public static PersonGateway load(DSLContext ctx, Record recrod) {
        PersonGateway person = Registry.getPerson(recrod.get("id", Long.class));
        if (person == null) {
            person = new PersonGateway(ctx);
            person.setId(recrod.get("id", Long.class));
            person.setFirstName(recrod.get("first_name", String.class));
            person.setLastName(recrod.get("last_name", String.class));
            person.setNumberOfDependents(recrod.get("number_of_dependents", Integer.class));
            Registry.addPerson(person);
        }
        return person;
    }
}
