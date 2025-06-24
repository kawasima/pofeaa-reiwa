package pofeaa.original.datasource.tabledatagateway;

import org.h2.jdbcx.JdbcDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;
import static org.assertj.core.api.Assertions.assertThat;

class PersonGatewayTest {
    private DSLContext ctx;
    private PersonGateway personGateway;

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
        
        personGateway = new PersonGateway(ctx);
    }

    @Test
    void testFindAllWithMultiplePersons() {
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
        
        // When finding all persons
        List<Person> persons = personGateway.findAll();
        
        // Then all persons should be returned
        assertThat(persons).isNotNull().hasSize(3);
        
        // Verify first person
        Person person1 = persons.get(0);
        assertThat(person1.getId()).isEqualTo(1L);
        assertThat(person1.getFirstName()).isEqualTo("John");
        assertThat(person1.getLastName()).isEqualTo("Doe");
        assertThat(person1.getNumberOfDependents()).isEqualTo(2);
        
        // Verify second person
        Person person2 = persons.get(1);
        assertThat(person2.getId()).isEqualTo(2L);
        assertThat(person2.getFirstName()).isEqualTo("Jane");
        assertThat(person2.getLastName()).isEqualTo("Smith");
        assertThat(person2.getNumberOfDependents()).isEqualTo(0);
        
        // Verify third person
        Person person3 = persons.get(2);
        assertThat(person3.getId()).isEqualTo(3L);
        assertThat(person3.getFirstName()).isEqualTo("Bob");
        assertThat(person3.getLastName()).isEqualTo("Johnson");
        assertThat(person3.getNumberOfDependents()).isEqualTo(3);
    }

    @Test
    void testFindAllWithEmptyTable() {
        // When finding all persons from empty table
        List<Person> persons = personGateway.findAll();
        
        // Then an empty list should be returned
        assertThat(persons).isNotNull().isEmpty();
    }

    @Test
    void testUpdate() {
        // Insert initial data
        ctx.insertInto(table("persons"))
                .set(field("first_name"), "John")
                .set(field("last_name"), "Doe")
                .set(field("number_of_dependents"), 2)
                .execute();
        
        // Create person object with updated data
        Person person = new Person();
        person.setId(1L);
        person.setFirstName("John Updated");
        person.setLastName("Doe Updated");
        person.setNumberOfDependents(5);
        
        // When updating the person
        personGateway.update(person);
        
        // Then the database should contain the updated values
        var updatedRecord = ctx.select()
                .from(table("persons"))
                .where(field("id").eq(1L))
                .fetchOne();
        
        assertThat(updatedRecord).isNotNull();
        assertThat(updatedRecord.get("FIRST_NAME", String.class)).isEqualTo("John Updated");
        assertThat(updatedRecord.get("LAST_NAME", String.class)).isEqualTo("Doe Updated");
        assertThat(updatedRecord.get("NUMBER_OF_DEPENDENTS", Integer.class)).isEqualTo(5);
    }

    @Test
    void testUpdateNonExistentPerson() {
        // Create person object for non-existent record
        Person person = new Person();
        person.setId(999L);
        person.setFirstName("Ghost");
        person.setLastName("Person");
        person.setNumberOfDependents(0);
        
        // When updating a non-existent person
        personGateway.update(person);
        
        // Then no records should be affected (no exception thrown)
        var count = ctx.fetchCount(table("persons"));
        assertThat(count).isEqualTo(0);
    }

    @Test
    void testFindAllWithNullValues() {
        // Insert data with some null values
        ctx.insertInto(table("persons"))
                .set(field("first_name"), "John")
                .set(field("last_name"), (String) null)
                .set(field("number_of_dependents"), (Integer) null)
                .execute();
        
        // When finding all persons
        List<Person> persons = personGateway.findAll();
        
        // Then the person with null values should be handled correctly
        assertThat(persons).isNotNull().hasSize(1);
        
        Person person = persons.get(0);
        assertThat(person.getFirstName()).isEqualTo("John");
        assertThat(person.getLastName()).isNull();
        assertThat(person.getNumberOfDependents()).isNull();
    }

    @Test
    void testUpdateWithPartialData() {
        // Insert initial data
        ctx.insertInto(table("persons"))
                .set(field("first_name"), "John")
                .set(field("last_name"), "Doe")
                .set(field("number_of_dependents"), 2)
                .execute();
        
        // Create person object with some null values
        Person person = new Person();
        person.setId(1L);
        person.setFirstName("John Updated");
        person.setLastName(null);
        person.setNumberOfDependents(null);
        
        // When updating the person
        personGateway.update(person);
        
        // Then the database should contain the updated values including nulls
        var updatedRecord = ctx.select()
                .from(table("persons"))
                .where(field("id").eq(1L))
                .fetchOne();
        
        assertThat(updatedRecord).isNotNull();
        assertThat(updatedRecord.get("FIRST_NAME", String.class)).isEqualTo("John Updated");
        assertThat(updatedRecord.get("LAST_NAME", String.class)).isNull();
        assertThat(updatedRecord.get("NUMBER_OF_DEPENDENTS", Integer.class)).isNull();
    }

    @Test
    void testFindAllPreservesOrder() {
        // Insert data in specific order
        ctx.insertInto(table("persons"))
                .set(field("first_name"), "Alice")
                .set(field("last_name"), "Anderson")
                .set(field("number_of_dependents"), 1)
                .execute();
        
        ctx.insertInto(table("persons"))
                .set(field("first_name"), "Bob")
                .set(field("last_name"), "Brown")
                .set(field("number_of_dependents"), 2)
                .execute();
        
        ctx.insertInto(table("persons"))
                .set(field("first_name"), "Charlie")
                .set(field("last_name"), "Clark")
                .set(field("number_of_dependents"), 3)
                .execute();
        
        // When finding all persons
        List<Person> persons = personGateway.findAll();
        
        // Then the order should be preserved (by ID)
        assertThat(persons).hasSize(3);
        assertThat(persons.get(0).getFirstName()).isEqualTo("Alice");
        assertThat(persons.get(1).getFirstName()).isEqualTo("Bob");
        assertThat(persons.get(2).getFirstName()).isEqualTo("Charlie");
    }
}