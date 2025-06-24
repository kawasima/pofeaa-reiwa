package pofeaa.original.datasource.datamapper;

import org.h2.jdbcx.JdbcDataSource;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PersonMapperTest {
    private DSLContext ctx;
    private PersonMapper personMapper;

    @BeforeEach
    void setup() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:datamapper_test;DB_CLOSE_DELAY=-1");
        ctx = DSL.using(ds, SQLDialect.H2);
        
        // Drop table if exists and create the persons table
        ctx.dropTableIfExists(table("persons")).execute();
        ctx.createTable(table("persons"))
                .column(field("id", SQLDataType.BIGINT.identity(true)))
                .column(field("first_name", SQLDataType.VARCHAR(255)))
                .column(field("last_name", SQLDataType.VARCHAR(255)))
                .column(field("number_of_dependents", SQLDataType.INTEGER))
                .execute();
        
        personMapper = new PersonMapper(ctx);
    }

    @Test
    void testFindExistingPerson() {
        // Insert test data
        ctx.insertInto(table("persons"))
                .set(field("first_name"), "John")
                .set(field("last_name"), "Doe")
                .set(field("number_of_dependents"), 2)
                .execute();
        
        // When finding an existing person
        Person person = personMapper.find(Identity.of(1L));
        
        // Then the person should be found with correct data
        assertThat(person).isNotNull();
        assertThat(person.getId().getValue()).isEqualTo(1L);
        assertThat(person.getFirstName()).isEqualTo("John");
        assertThat(person.getLastName()).isEqualTo("Doe");
        assertThat(person.getNumberOfDependents()).isEqualTo(2);
    }

    @Test
    void testFindNonExistentPerson() {
        // When finding a non-existent person
        Person person = personMapper.find(Identity.of(999L));
        
        // Then null should be returned
        assertThat(person).isNull();
    }

    @Test
    void testFindMultiplePersons() {
        // Insert multiple persons
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
        
        // When finding specific persons
        Person person1 = personMapper.find(Identity.of(1L));
        Person person2 = personMapper.find(Identity.of(2L));
        Person person3 = personMapper.find(Identity.of(3L));
        
        // Then each person should be found correctly
        assertThat(person1).isNotNull();
        assertThat(person1.getFirstName()).isEqualTo("John");
        
        assertThat(person2).isNotNull();
        assertThat(person2.getFirstName()).isEqualTo("Jane");
        
        assertThat(person3).isNotNull();
        assertThat(person3.getFirstName()).isEqualTo("Bob");
    }

    @Test
    void testDoLoadWithValidRecord() {
        // Create a record manually
        Record record = ctx.select()
                .from(table("persons"))
                .where(field("id").eq(1L))
                .fetchOne();
        
        // Insert data first
        ctx.insertInto(table("persons"))
                .set(field("first_name"), "Alice")
                .set(field("last_name"), "Anderson")
                .set(field("number_of_dependents"), 1)
                .execute();
        
        record = ctx.select()
                .from(table("persons"))
                .where(field("id").eq(1L))
                .fetchOne();
        
        // When loading from record
        Person person = personMapper.doLoad(record);
        
        // Then person should be created correctly
        assertThat(person).isNotNull();
        assertThat(person.getId().getValue()).isEqualTo(1L);
        assertThat(person.getFirstName()).isEqualTo("Alice");
        assertThat(person.getLastName()).isEqualTo("Anderson");
        assertThat(person.getNumberOfDependents()).isEqualTo(1);
    }

    @Test
    void testDoLoadWithNullRecord() {
        // When loading from null record
        Person person = personMapper.doLoad(null);
        
        // Then null should be returned
        assertThat(person).isNull();
    }

    @Test
    void testFindWithNullValues() {
        // Insert data with some null values
        ctx.insertInto(table("persons"))
                .set(field("first_name"), "John")
                .set(field("last_name"), (String) null)
                .set(field("number_of_dependents"), (Integer) null)
                .execute();
        
        // When finding the person
        Person person = personMapper.find(Identity.of(1L));
        
        // Then the person should be found with null values handled
        assertThat(person).isNotNull();
        assertThat(person.getFirstName()).isEqualTo("John");
        assertThat(person.getLastName()).isNull();
        assertThat(person.getNumberOfDependents()).isNull();
    }

    @Test
    void testDoLoadWithCaseInsensitiveFieldNames() {
        // This test verifies that the mapper handles H2's uppercase field names correctly
        // Insert data
        ctx.insertInto(table("persons"))
                .set(field("first_name"), "Test")
                .set(field("last_name"), "User")
                .set(field("number_of_dependents"), 5)
                .execute();
        
        // Fetch record using uppercase field names (as H2 returns them)
        Record record = ctx.select(
                field("ID"),
                field("FIRST_NAME"),
                field("LAST_NAME"),
                field("NUMBER_OF_DEPENDENTS")
            )
            .from(table("PERSONS"))
            .where(field("ID").eq(1L))
            .fetchOne();
        
        // When loading from record
        Person person = personMapper.doLoad(record);
        
        // Then person should be created correctly
        assertThat(person).isNotNull();
        assertThat(person.getId().getValue()).isEqualTo(1L);
        assertThat(person.getFirstName()).isEqualTo("Test");
        assertThat(person.getLastName()).isEqualTo("User");
        assertThat(person.getNumberOfDependents()).isEqualTo(5);
    }

    @Test
    void testIdentityValidation() {
        // Test that Identity validation works correctly
        assertThatThrownBy(() -> personMapper.find(Identity.of(-1L)))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThatThrownBy(() -> personMapper.find(Identity.of(null)))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testFindWithLargeId() {
        // Insert data with a specific ID
        ctx.insertInto(table("persons"))
                .columns(field("id"), field("first_name"), field("last_name"), field("number_of_dependents"))
                .values(999999L, "Large", "Id", 0)
                .execute();
        
        // When finding with large ID
        Person person = personMapper.find(Identity.of(999999L));
        
        // Then the person should be found
        assertThat(person).isNotNull();
        assertThat(person.getId().getValue()).isEqualTo(999999L);
        assertThat(person.getFirstName()).isEqualTo("Large");
    }
}