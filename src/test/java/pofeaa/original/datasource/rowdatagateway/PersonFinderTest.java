package pofeaa.original.datasource.rowdatagateway;

import org.h2.jdbcx.JdbcDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PersonFinderTest {
    private DSLContext ctx;
    private PersonFinder personFinder;

    @BeforeEach
    void setup() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        ctx = DSL.using(ds, SQLDialect.H2);
        
        // Drop table if exists and create the persons table
        ctx.dropTableIfExists(table("persons")).execute();
        ctx.createTable(table("persons"))
                .column(field("id", SQLDataType.BIGINT.identity(true)))
                .column(field("first_name", SQLDataType.VARCHAR(255)))
                .column(field("last_name", SQLDataType.VARCHAR(255)))
                .column(field("number_of_dependents", SQLDataType.INTEGER))
                .execute();
        
        // Insert test data
        ctx.insertInto(table("persons"))
                .set(field("first_name"), "John")
                .set(field("last_name"), "Doe")
                .set(field("number_of_dependents"), 2)
                .execute();
        
        ctx.insertInto(table("persons"))
                .set(field("first_name"), "Jane")
                .set(field("last_name"), "Smith")
                .set(field("number_of_dependents"), 0)
                .execute();
        
        ctx.insertInto(table("persons"))
                .set(field("first_name"), "Bob")
                .set(field("last_name"), "Johnson")
                .set(field("number_of_dependents"), 3)
                .execute();
        
        personFinder = new PersonFinder(ctx);
    }

    @AfterEach
    void tearDown() {
        // Clear the Registry to ensure test isolation
        Registry.clear();
    }

    @Test
    void testFindWithPersonInRegistry() {
        // First, load a person into the registry
        PersonGateway firstLoad = personFinder.find(1L);
        assertThat(firstLoad).isNotNull();
        assertThat(firstLoad.getId()).isEqualTo(1L);
        assertThat(firstLoad.getFirstName()).isEqualTo("John");
        
        // Modify the database to prove we're getting the cached version
        ctx.update(table("persons"))
                .set(field("first_name"), "Modified")
                .where(field("id").eq(1L))
                .execute();
        
        // Second load should return the same cached instance
        PersonGateway secondLoad = personFinder.find(1L);
        assertThat(secondLoad).isSameAs(firstLoad);
        assertThat(secondLoad.getFirstName()).isEqualTo("John"); // Still has original value
    }

    @Test
    void testFindWithPersonNotInRegistry() {
        // When finding a person not in registry
        PersonGateway result = personFinder.find(1L);
        
        // Then the person should be loaded from database
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getNumberOfDependents()).isEqualTo(2);
        
        // Verify person is now in registry
        PersonGateway cached = Registry.getPerson(1L);
        assertThat(cached).isSameAs(result);
    }

    @Test
    void testFindResponsibles() {
        // When finding responsible persons (those with dependents > 0)
        List<PersonGateway> responsibles = personFinder.findResponsibles();
        
        // Then only persons with dependents should be returned
        assertThat(responsibles).isNotNull().hasSize(2);
        
        // Verify the correct persons were returned
        PersonGateway person1 = responsibles.get(0);
        assertThat(person1.getFirstName()).isEqualTo("John");
        assertThat(person1.getNumberOfDependents()).isEqualTo(2);
        
        PersonGateway person2 = responsibles.get(1);
        assertThat(person2.getFirstName()).isEqualTo("Bob");
        assertThat(person2.getNumberOfDependents()).isEqualTo(3);
        
        // Jane should not be included (0 dependents)
        assertThat(responsibles)
            .extracting(PersonGateway::getFirstName)
            .doesNotContain("Jane");
    }

    @Test
    void testFindNonExistentPerson() {
        // When finding a non-existent person
        // Then an exception should be thrown due to fetchOne() returning null
        assertThatThrownBy(() -> personFinder.find(999L))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void testFindResponsiblesWhenNoneExist() {
        // Clear existing data and insert only persons with no dependents
        ctx.deleteFrom(table("persons")).execute();
        
        ctx.insertInto(table("persons"))
                .set(field("first_name"), "Alice")
                .set(field("last_name"), "Brown")
                .set(field("number_of_dependents"), 0)
                .execute();
        
        // When finding responsible persons
        List<PersonGateway> responsibles = personFinder.findResponsibles();
        
        // Then the list should be empty
        assertThat(responsibles).isNotNull().isEmpty();
    }
}