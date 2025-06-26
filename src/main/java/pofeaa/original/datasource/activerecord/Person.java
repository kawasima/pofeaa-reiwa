package pofeaa.original.datasource.activerecord;

import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import javax.sql.DataSource;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

/**
 * Active Record implementation for the Person domain model.
 * <p>
 * A domain model must implement business invariants. However, because it also includes the functionality of Row Data Gateway,
 * it must also satisfy RDB invariants, which makes the design prone to failure.
 * </p>
 */
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

    /**
     * Constructs a new Person instance with the specified attributes.
     * 
     * @param id the unique identifier for this person, may be null for new instances
     * @param firstName the person's first name
     * @param lastName the person's last name
     * @param numberOfDependents the number of dependents this person has
     */
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

    /**
     * Creates and returns a JOOQ DSL context for database operations.
     * 
     * This method provides a configured context using the H2 database dialect
     * and the shared data source connection pool.
     * 
     * @return a DSLContext configured for H2 database operations
     */
    private static DSLContext getContext() {
        return DSL.using(ds, SQLDialect.H2);
    }

    /**
     * Finds and retrieves a Person from the database by their unique identifier.
     * 
     * This method implements the finder pattern of Active Record, querying the
     * persons table for a record matching the given ID and mapping it to a
     * Person domain object.
     * 
     * @param id the unique identifier of the person to retrieve
     * @return the Person instance if found, null if no person exists with the given ID
     */
    public static Person find(Long id) {
        DSLContext ctx = getContext();
        Record record = ctx.select()
                .from(table("persons"))
                .where(field("id").eq(id))
                .fetchOne();

        return load(record);
    }

    /**
     * Maps a database record to a Person domain model object.
     * 
     * This method performs the transformation from a Record Set (database row) to 
     * a Person instance, implementing the Active Record pattern. It extracts field 
     * values from the database record and constructs a fully initialized Person object.
     * 
     * @param record the database record containing person data with fields: id, first_name, 
     *               last_name, and number_of_dependents. May be null.
     * @return a new Person instance populated with data from the record, or null if the 
     *         input record is null
     */
    public static Person load(Record record) {
        if (record == null) {
            return null;
        }
        return new Person(
                record.get(field("id"), Long.class),
                record.get(field("first_name"), String.class),
                record.get("last_name", String.class),
                record.get("number_of_dependents", Integer.class)
        );
    }

    /**
     * Persists the current state of this Person instance to the database.
     * 
     * This method updates the existing database record with the current values
     * of all mutable fields (first_name, last_name, number_of_dependents).
     * The update is performed based on the person's ID.
     * 
     * @throws RuntimeException if the database update fails
     */
    public void update() {
        DSLContext ctx = getContext();
        ctx.update(table("persons"))
                .set(field("first_name"), this.firstName)
                .set(field("last_name"), this.lastName)
                .set(field("number_of_dependents"), this.numberOfDependents)
                .where(field("id").eq(this.id))
                .execute();
    }

    /**
     * Inserts this Person as a new record in the database.
     * 
     * This method creates a new record in the persons table with the current
     * field values. Upon successful insertion, it updates this instance's ID
     * with the auto-generated value from the database.
     * 
     * @throws RuntimeException if the database insert fails
     */
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
