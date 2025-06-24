package pofeaa.original.metadata.repository;

import org.h2.jdbcx.JdbcDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pofeaa.original.datasource.datamapper.Identity;

import java.util.List;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;
import static org.assertj.core.api.Assertions.assertThat;

class PersonRepositoryTest {
    private DSLContext ctx;
    private PersonRepository personRepository;

    @BeforeEach
    void setup() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:repository_test;DB_CLOSE_DELAY=-1");
        ctx = DSL.using(ds, SQLDialect.H2);
        
        // Drop table if exists and create the persons table with benefactor_id
        ctx.dropTableIfExists(table("persons")).execute();
        ctx.createTable(table("persons"))
                .column(field("id", SQLDataType.BIGINT.identity(true)))
                .column(field("first_name", SQLDataType.VARCHAR(255)))
                .column(field("last_name", SQLDataType.VARCHAR(255)))
                .column(field("number_of_dependents", SQLDataType.INTEGER))
                .column(field("benefactor_id", SQLDataType.BIGINT))
                .execute();
        
        personRepository = new PersonRepository(ctx);
    }

    @Test
    void testDependentsOfWithSingleDependent() {
        // Insert benefactor
        ctx.insertInto(table("persons"))
                .set(field("first_name"), "John")
                .set(field("last_name"), "Doe")
                .set(field("number_of_dependents"), 1)
                .set(field("benefactor_id"), (Long) null)
                .execute();
        
        // Insert dependent
        ctx.insertInto(table("persons"))
                .set(field("first_name"), "Jane")
                .set(field("last_name"), "Doe")
                .set(field("number_of_dependents"), 0)
                .set(field("benefactor_id"), 1L)
                .execute();
        
        // Create benefactor person object
        Person benefactor = new Person(Identity.of(1L), "John", "Doe", 1);
        
        // When finding dependents
        List<Person> dependents = personRepository.dependentsOf(benefactor);
        
        // Then one dependent should be found
        assertThat(dependents).hasSize(1);
        Person dependent = dependents.getFirst();
        assertThat(dependent.getId().getValue()).isEqualTo(2L);
        assertThat(dependent.getFirstName()).isEqualTo("Jane");
        assertThat(dependent.getLastName()).isEqualTo("Doe");
        assertThat(dependent.getNumberOfDependents()).isEqualTo(0);
    }

    @Test
    void testDependentsOfWithMultipleDependents() {
        // Insert benefactor
        ctx.insertInto(table("persons"))
                .set(field("first_name"), "John")
                .set(field("last_name"), "Smith")
                .set(field("number_of_dependents"), 3)
                .set(field("benefactor_id"), (Long) null)
                .execute();
        
        // Insert multiple dependents
        ctx.insertInto(table("persons"))
                .set(field("first_name"), "Alice")
                .set(field("last_name"), "Smith")
                .set(field("number_of_dependents"), 0)
                .set(field("benefactor_id"), 1L)
                .execute();
        
        ctx.insertInto(table("persons"))
                .set(field("first_name"), "Bob")
                .set(field("last_name"), "Smith")
                .set(field("number_of_dependents"), 0)
                .set(field("benefactor_id"), 1L)
                .execute();
        
        ctx.insertInto(table("persons"))
                .set(field("first_name"), "Charlie")
                .set(field("last_name"), "Smith")
                .set(field("number_of_dependents"), 0)
                .set(field("benefactor_id"), 1L)
                .execute();
        
        // Create benefactor person object
        Person benefactor = new Person(Identity.of(1L), "John", "Smith", 3);
        
        // When finding dependents
        List<Person> dependents = personRepository.dependentsOf(benefactor);
        
        // Then all three dependents should be found
        assertThat(dependents).hasSize(3);
        
        // Verify dependents are found (order may vary)
        assertThat(dependents)
            .extracting(Person::getFirstName)
            .containsExactlyInAnyOrder("Alice", "Bob", "Charlie");
        
        assertThat(dependents)
            .allMatch(p -> p.getLastName().equals("Smith"))
            .allMatch(p -> p.getNumberOfDependents() == 0);
    }

    @Test
    void testDependentsOfWithNoDependents() {
        // Insert person with no dependents
        ctx.insertInto(table("persons"))
                .set(field("first_name"), "Jane")
                .set(field("last_name"), "Johnson")
                .set(field("number_of_dependents"), 0)
                .set(field("benefactor_id"), (Long) null)
                .execute();
        
        // Insert another person who is NOT a dependent of Jane
        ctx.insertInto(table("persons"))
                .set(field("first_name"), "Bob")
                .set(field("last_name"), "Brown")
                .set(field("number_of_dependents"), 0)
                .set(field("benefactor_id"), 999L) // Different benefactor
                .execute();
        
        // Create person object
        Person person = new Person(Identity.of(1L), "Jane", "Johnson", 0);
        
        // When finding dependents
        List<Person> dependents = personRepository.dependentsOf(person);
        
        // Then no dependents should be found
        assertThat(dependents).isEmpty();
    }

    @Test
    void testDependentsOfWithNestedDependencies() {
        // Insert root benefactor
        ctx.insertInto(table("persons"))
                .set(field("first_name"), "Grandpa")
                .set(field("last_name"), "Wilson")
                .set(field("number_of_dependents"), 1)
                .set(field("benefactor_id"), (Long) null)
                .execute();
        
        // Insert first level dependent (parent)
        ctx.insertInto(table("persons"))
                .set(field("first_name"), "Dad")
                .set(field("last_name"), "Wilson")
                .set(field("number_of_dependents"), 2)
                .set(field("benefactor_id"), 1L)
                .execute();
        
        // Insert second level dependents (children of Dad)
        ctx.insertInto(table("persons"))
                .set(field("first_name"), "Child1")
                .set(field("last_name"), "Wilson")
                .set(field("number_of_dependents"), 0)
                .set(field("benefactor_id"), 2L)
                .execute();
        
        ctx.insertInto(table("persons"))
                .set(field("first_name"), "Child2")
                .set(field("last_name"), "Wilson")
                .set(field("number_of_dependents"), 0)
                .set(field("benefactor_id"), 2L)
                .execute();
        
        // Test Grandpa's dependents (should only be Dad, not grandchildren)
        Person grandpa = new Person(Identity.of(1L), "Grandpa", "Wilson", 1);
        List<Person> grandpaDependents = personRepository.dependentsOf(grandpa);
        
        assertThat(grandpaDependents).hasSize(1);
        assertThat(grandpaDependents.getFirst().getFirstName()).isEqualTo("Dad");
        
        // Test Dad's dependents (should be the two children)
        Person dad = new Person(Identity.of(2L), "Dad", "Wilson", 2);
        List<Person> dadDependents = personRepository.dependentsOf(dad);
        
        assertThat(dadDependents).hasSize(2);
        assertThat(dadDependents)
            .extracting(Person::getFirstName)
            .containsExactlyInAnyOrder("Child1", "Child2");
    }

    @Test
    void testDependentsOfWithNullValues() {
        // Insert benefactor
        ctx.insertInto(table("persons"))
                .set(field("first_name"), "John")
                .set(field("last_name"), "Doe")
                .set(field("number_of_dependents"), 1)
                .set(field("benefactor_id"), (Long) null)
                .execute();
        
        // Insert dependent with some null values
        ctx.insertInto(table("persons"))
                .set(field("first_name"), "Jane")
                .set(field("last_name"), (String) null)
                .set(field("number_of_dependents"), (Integer) null)
                .set(field("benefactor_id"), 1L)
                .execute();
        
        // Create benefactor person object
        Person benefactor = new Person(Identity.of(1L), "John", "Doe", 1);
        
        // When finding dependents
        List<Person> dependents = personRepository.dependentsOf(benefactor);
        
        // Then dependent should be found with null values handled
        assertThat(dependents).hasSize(1);
        Person dependent = dependents.getFirst();
        assertThat(dependent.getFirstName()).isEqualTo("Jane");
        assertThat(dependent.getLastName()).isNull();
        assertThat(dependent.getNumberOfDependents()).isEqualTo(0); // null converted to 0 by primitive int
    }

    @Test
    void testDependentsOfWithLargeDataset() {
        // Insert benefactor
        ctx.insertInto(table("persons"))
                .set(field("first_name"), "Boss")
                .set(field("last_name"), "BigFamily")
                .set(field("number_of_dependents"), 100)
                .set(field("benefactor_id"), (Long) null)
                .execute();
        
        // Insert many dependents
        for (int i = 1; i <= 100; i++) {
            ctx.insertInto(table("persons"))
                    .set(field("first_name"), "Dependent" + i)
                    .set(field("last_name"), "BigFamily")
                    .set(field("number_of_dependents"), 0)
                    .set(field("benefactor_id"), 1L)
                    .execute();
        }
        
        // Create benefactor person object
        Person benefactor = new Person(Identity.of(1L), "Boss", "BigFamily", 100);
        
        // When finding dependents
        List<Person> dependents = personRepository.dependentsOf(benefactor);
        
        // Then all 100 dependents should be found
        assertThat(dependents).hasSize(100);
        assertThat(dependents)
            .allMatch(p -> p.getLastName().equals("BigFamily"))
            .allMatch(p -> p.getFirstName().startsWith("Dependent"));
    }

    @Test
    void testDependentsOfWithMixedBenefactors() {
        // Insert multiple benefactors
        ctx.insertInto(table("persons"))
                .set(field("first_name"), "John")
                .set(field("last_name"), "Doe")
                .set(field("number_of_dependents"), 2)
                .set(field("benefactor_id"), (Long) null)
                .execute();
        
        ctx.insertInto(table("persons"))
                .set(field("first_name"), "Jane")
                .set(field("last_name"), "Smith")
                .set(field("number_of_dependents"), 1)
                .set(field("benefactor_id"), (Long) null)
                .execute();
        
        // Insert dependents for John
        ctx.insertInto(table("persons"))
                .set(field("first_name"), "John_Child1")
                .set(field("last_name"), "Doe")
                .set(field("number_of_dependents"), 0)
                .set(field("benefactor_id"), 1L)
                .execute();
        
        ctx.insertInto(table("persons"))
                .set(field("first_name"), "John_Child2")
                .set(field("last_name"), "Doe")
                .set(field("number_of_dependents"), 0)
                .set(field("benefactor_id"), 1L)
                .execute();
        
        // Insert dependent for Jane
        ctx.insertInto(table("persons"))
                .set(field("first_name"), "Jane_Child")
                .set(field("last_name"), "Smith")
                .set(field("number_of_dependents"), 0)
                .set(field("benefactor_id"), 2L)
                .execute();
        
        // Test John's dependents
        Person john = new Person(Identity.of(1L), "John", "Doe", 2);
        List<Person> johnDependents = personRepository.dependentsOf(john);
        
        assertThat(johnDependents).hasSize(2);
        assertThat(johnDependents)
            .extracting(Person::getFirstName)
            .containsExactlyInAnyOrder("John_Child1", "John_Child2");
        
        // Test Jane's dependents
        Person jane = new Person(Identity.of(2L), "Jane", "Smith", 1);
        List<Person> janeDependents = personRepository.dependentsOf(jane);
        
        assertThat(janeDependents).hasSize(1);
        assertThat(janeDependents.getFirst().getFirstName()).isEqualTo("Jane_Child");
    }
}