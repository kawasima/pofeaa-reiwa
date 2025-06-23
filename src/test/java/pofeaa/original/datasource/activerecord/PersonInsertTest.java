package pofeaa.original.datasource.activerecord;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Test for verifying that the Person.insert() method properly sets the returned ID value.
 * 
 * Note: This test uses the Person class's static database connection.
 * The Person class uses an in-memory H2 database that persists for the test session.
 */
@DisplayName("Person Active Record Insert Test")
class PersonInsertTest {

    @BeforeAll
    static void setUpDatabase() {
        // Use a dedicated test database to avoid conflicts with other tests
        var ds = new org.h2.jdbcx.JdbcDataSource();
        ds.setURL("jdbc:h2:mem:person_test;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("");
        
        DSLContext ctx = DSL.using(ds, SQLDialect.H2);
        
        // Create persons table in the test database
        ctx.execute("CREATE TABLE IF NOT EXISTS persons (" +
                   "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                   "first_name VARCHAR(50), " +
                   "last_name VARCHAR(50), " +
                   "number_of_dependents INTEGER)");
    }

    @Test
    @DisplayName("Should set the returned ID value after insert")
    void shouldSetReturnedIdValueAfterInsert() {
        // Given - Create a Person with null ID (will be auto-generated)
        Person person = new Person(null, "Jane", "Smith", 3);
        
        // Verify ID is initially null
        assertThat(person.getId()).isNull();
        
        // When - Insert the person
        person.insert();
        
        // Then - ID should be set to the returned value from database
        assertThat(person.getId()).isNotNull();
        assertThat(person.getId()).isGreaterThan(0L);
        
        // Verify the person can be found by the generated ID
        Person foundPerson = Person.find(person.getId());
        assertThat(foundPerson).isNotNull();
        assertThat(foundPerson.getFirstName()).isEqualTo("Jane");
        assertThat(foundPerson.getLastName()).isEqualTo("Smith");
        assertThat(foundPerson.getNumberOfDependents()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should generate sequential IDs for multiple inserts")
    void shouldGenerateSequentialIdsForMultipleInserts() {
        // Given - Create multiple persons
        Person person1 = new Person(null, "Alice", "Johnson", 1);
        Person person2 = new Person(null, "Bob", "Williams", 2);
        Person person3 = new Person(null, "Carol", "Brown", 0);
        
        // When - Insert them in sequence
        person1.insert();
        person2.insert();
        person3.insert();
        
        // Then - IDs should be sequential and properly set
        assertThat(person1.getId()).isNotNull();
        assertThat(person2.getId()).isNotNull();
        assertThat(person3.getId()).isNotNull();
        
        assertThat(person2.getId()).isGreaterThan(person1.getId());
        assertThat(person3.getId()).isGreaterThan(person2.getId());
        
        // Verify all can be retrieved
        assertThat(Person.find(person1.getId()).getFirstName()).isEqualTo("Alice");
        assertThat(Person.find(person2.getId()).getFirstName()).isEqualTo("Bob");
        assertThat(Person.find(person3.getId()).getFirstName()).isEqualTo("Carol");
    }

    @Test
    @DisplayName("Should handle insert with explicit ID correctly")
    void shouldHandleInsertWithExplicitIdCorrectly() {
        // Given - Create a Person with null ID (typical for new records)
        Person person = new Person(null, "David", "Davis", 4);
        
        // When - Insert the person (database will auto-generate ID)
        person.insert();
        
        // Then - ID should be set to the auto-generated value
        assertThat(person.getId()).isNotNull();
        assertThat(person.getId()).isGreaterThan(0L);
        
        // Verify the person exists in database with correct data
        Person foundPerson = Person.find(person.getId());
        assertThat(foundPerson).isNotNull();
        assertThat(foundPerson.getFirstName()).isEqualTo("David");
        assertThat(foundPerson.getLastName()).isEqualTo("Davis");
        assertThat(foundPerson.getNumberOfDependents()).isEqualTo(4);
    }
}