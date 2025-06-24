package pofeaa.original.base.recordset;

import org.h2.jdbcx.JdbcDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PersonRepositoryTest {
    private DSLContext ctx;
    private PersonRepository personRepository;

    @BeforeEach
    void setup() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        ctx = DSL.using(ds, SQLDialect.H2);
        
        // Create tables using raw SQL
        ctx.execute("DROP TABLE IF EXISTS reservations");
        ctx.execute("DROP TABLE IF EXISTS persons");
        
        ctx.execute("""
            CREATE TABLE persons (
                id BIGINT NOT NULL PRIMARY KEY,
                first_name VARCHAR(50),
                last_name VARCHAR(50),
                number_of_dependents INT
            )
        """);
        
        ctx.execute("""
            CREATE TABLE reservations (
                id BIGINT NOT NULL PRIMARY KEY,
                passenger_id BIGINT,
                reservation_date TIMESTAMP,
                FOREIGN KEY (passenger_id) REFERENCES persons(id)
            )
        """);
        
        // Insert test data
        ctx.execute("INSERT INTO persons (id, first_name, last_name, number_of_dependents) VALUES (1, 'John', 'Doe', 2)");
        ctx.execute("INSERT INTO persons (id, first_name, last_name, number_of_dependents) VALUES (2, 'Jane', 'Smith', 0)");
        ctx.execute("INSERT INTO persons (id, first_name, last_name, number_of_dependents) VALUES (3, 'Bob', 'Johnson', 3)");
        
        PersonFinder personFinder = new PersonFinder(ctx);
        personRepository = new PersonRepository(personFinder);
    }

    @Test
    void testGetPersonImplicit() {
        // When getting person by ID using implicit method
        Person person = personRepository.getPersonImplicit(1L);
        
        // Then the correct person should be returned
        assertThat(person).isNotNull();
        assertThat(person.getFirstName()).isEqualTo("John");
        assertThat(person.getLastName()).isEqualTo("Doe");
    }

    @Test
    void testGetPersonExplicit() {
        // When getting person by ID using explicit method
        Person person = personRepository.getPersonExplicit(1L);
        
        // Then the correct person should be returned
        assertThat(person).isNotNull();
        assertThat(person.getFirstName()).isEqualTo("John");
        assertThat(person.getLastName()).isEqualTo("Doe");
    }

    @Test
    void testBothMethodsReturnSameResult() {
        // Given a person ID
        long personId = 2L;
        
        // When using both methods
        Person implicitResult = personRepository.getPersonImplicit(personId);
        Person explicitResult = personRepository.getPersonExplicit(personId);
        
        // Then both should return equivalent results
        assertThat(implicitResult).isNotNull();
        assertThat(explicitResult).isNotNull();
        assertThat(implicitResult.getFirstName()).isEqualTo(explicitResult.getFirstName());
        assertThat(implicitResult.getLastName()).isEqualTo(explicitResult.getLastName());
        assertThat(implicitResult.getFirstName()).isEqualTo("Jane");
        assertThat(implicitResult.getLastName()).isEqualTo("Smith");
    }

    @Test
    void testGetPersonImplicitNonExistent() {
        // When getting non-existent person using implicit method
        Person person = personRepository.getPersonImplicit(999L);
        
        // Then should return null (no exception thrown)
        assertThat(person).isNull();
    }

    @Test
    void testGetPersonExplicitNonExistent() {
        // When getting non-existent person using explicit method
        Person person = personRepository.getPersonExplicit(999L);
        
        // Then should return null
        assertThat(person).isNull();
    }

    @Test
    void testGetAllTestPersons() {
        // Test all existing persons can be retrieved
        Person person1 = personRepository.getPersonExplicit(1L);
        Person person2 = personRepository.getPersonExplicit(2L);
        Person person3 = personRepository.getPersonExplicit(3L);
        
        assertThat(person1).isNotNull();
        assertThat(person1.getFirstName()).isEqualTo("John");
        assertThat(person1.getLastName()).isEqualTo("Doe");
        
        assertThat(person2).isNotNull();
        assertThat(person2.getFirstName()).isEqualTo("Jane");
        assertThat(person2.getLastName()).isEqualTo("Smith");
        
        assertThat(person3).isNotNull();
        assertThat(person3.getFirstName()).isEqualTo("Bob");
        assertThat(person3.getLastName()).isEqualTo("Johnson");
    }

    @Test
    void testGetPersonWithSpecialCharacters() {
        // Insert person with special characters
        ctx.execute("INSERT INTO persons (id, first_name, last_name, number_of_dependents) VALUES (4, 'Jean-Pierre', 'O''Connor', 1)");
        
        // Both methods should handle special characters correctly
        Person implicitResult = personRepository.getPersonImplicit(4L);
        Person explicitResult = personRepository.getPersonExplicit(4L);
        
        assertThat(implicitResult).isNotNull();
        assertThat(implicitResult.getFirstName()).isEqualTo("Jean-Pierre");
        assertThat(implicitResult.getLastName()).isEqualTo("O'Connor");
        
        assertThat(explicitResult).isNotNull();
        assertThat(explicitResult.getFirstName()).isEqualTo("Jean-Pierre");
        assertThat(explicitResult.getLastName()).isEqualTo("O'Connor");
    }

    @Test
    void testGetPersonWithNullFields() {
        // Insert person with null first name
        ctx.execute("INSERT INTO persons (id, first_name, last_name, number_of_dependents) VALUES (5, NULL, 'NullFirst', 0)");
        
        Person implicitResult = personRepository.getPersonImplicit(5L);
        Person explicitResult = personRepository.getPersonExplicit(5L);
        
        assertThat(implicitResult).isNotNull();
        assertThat(implicitResult.getFirstName()).isNull();
        assertThat(implicitResult.getLastName()).isEqualTo("NullFirst");
        
        assertThat(explicitResult).isNotNull();
        assertThat(explicitResult.getFirstName()).isNull();
        assertThat(explicitResult.getLastName()).isEqualTo("NullFirst");
    }

    @Test
    void testConsistencyAcrossMultipleCalls() {
        // Both methods should be consistent across multiple calls
        for (int i = 0; i < 5; i++) {
            Person implicit = personRepository.getPersonImplicit(1L);
            Person explicit = personRepository.getPersonExplicit(1L);
            
            assertThat(implicit).isNotNull();
            assertThat(explicit).isNotNull();
            assertThat(implicit.getFirstName()).isEqualTo("John");
            assertThat(explicit.getFirstName()).isEqualTo("John");
            assertThat(implicit.getLastName()).isEqualTo("Doe");
            assertThat(explicit.getLastName()).isEqualTo("Doe");
        }
    }

}