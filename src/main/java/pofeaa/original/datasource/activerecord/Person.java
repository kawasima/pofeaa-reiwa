package pofeaa.original.datasource.activerecord;

import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import javax.sql.DataSource;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class Person {
    private static final DataSource ds = new HikariDataSource();
    static {
        ((HikariDataSource) ds).setJdbcUrl("jdbc:h2:mem:person_test;DB_CLOSE_DELAY=-1");
        ((HikariDataSource) ds).setUsername("sa");
        ((HikariDataSource) ds).setPassword("");
    }

    private Long id;
    private String firstName;
    private String lastName;
    private Integer numberOfDependents;

    public Person(Long id, String firstName, String lastName, Integer numberOfDependents) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.numberOfDependents = numberOfDependents;
    }

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

    private static DSLContext getContext() {
        return DSL.using(ds, SQLDialect.H2);
    }

    public static Person find(Long id) {
        DSLContext ctx = getContext();
        Record record = ctx.select()
                .from(table("persons"))
                .where(field("id").eq(id))
                .fetchOne();

        return load(record);
    }

    public static Person load(Record record) {
        if (record == null) {
            return null;
        }
        return new Person(
                record.get(0, Long.class),    // id
                record.get(1, String.class),  // first_name
                record.get(2, String.class),  // last_name
                record.get(3, Integer.class)  // number_of_dependents
        );
    }

    public void update() {
        DSLContext ctx = getContext();
        ctx.update(table("persons"))
                .set(field("first_name"), this.firstName)
                .set(field("last_name"), this.lastName)
                .set(field("number_of_dependents"), this.numberOfDependents)
                .where(field("id").eq(this.id))
                .execute();
    }

    public void insert() {
        DSLContext ctx = getContext();
        Record result = ctx.insertInto(table("persons"))
                .set(field("first_name"), this.firstName)
                .set(field("last_name"), this.lastName)
                .set(field("number_of_dependents"), this.numberOfDependents)
                .returning(field("id"))
                .fetchOne();
        
        if (result != null) {
            this.id = result.get(field("id"), Long.class);
        }
    }
}
