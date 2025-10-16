package pofeaa.original.datasource.activerecord;

import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import pofeaa.original.datasource.PersonName;

import javax.sql.DataSource;

import java.time.LocalDate;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

/**
 * Active Record implementation for the Person domain model.
 * <p>
 * A domain model must implement business invariants. However, because it also includes the functionality of Row Data Gateway,
 * it must also satisfy RDB invariants, which makes the design prone to failure.
 * </p>
 */
public sealed abstract class Person permits UnderagePerson, OveragePerson {
    private static final DataSource ds = new HikariDataSource();
    static {
        ((HikariDataSource) ds).setJdbcUrl("jdbc:h2:mem:person_test;DB_CLOSE_DELAY=-1");
        ((HikariDataSource) ds).setUsername("sa");
        ((HikariDataSource) ds).setPassword("");
    }

    private Long id;
    private final PersonName name;
    private final int age;

    /**
     * Constructs a new Person instance with the specified attributes.
     * 
     * @param id the unique identifier for this person, may be null for new instances
     * @param name the person's name
     */
    public Person(Long id, PersonName name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    public Long getId() {
        return id;
    }

    public PersonName getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    /**
     * Creates and returns a JOOQ DSL context for database operations.
     * 
     * This method provides a configured context using the H2 database dialect
     * and the shared data source connection pool.
     * 
     * @return a DSLContext configured for H2 database operations
     */
    protected static DSLContext getContext() {
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
    static Person load(Record record) {
        if (record == null) {
            return null;
        }
        LocalDate birthDate = record.get("birth_date", LocalDate.class);
        int age = LocalDate.now().getYear() - birthDate.getYear();
        if (age < 18) {
            return UnderagePerson.load(record);
        } else {
            return OveragePerson.load(record);
        }
    }

    public abstract void update();
    public abstract void insert();
}
