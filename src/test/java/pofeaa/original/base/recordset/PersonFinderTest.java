package pofeaa.original.base.recordset;

import org.h2.jdbcx.JdbcDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        ctx.insertInto(table("persons"))
                .set(field("id"), 1L)
                .set(field("first_name"), "John")
                .set(field("last_name"), "Doe")
                .set(field("number_of_dependents"), 2)
                .execute();
        
        ctx.insertInto(table("persons"))
                .set(field("id"), 2L)
                .set(field("first_name"), "Jane")
                .set(field("last_name"), "Smith")
                .set(field("number_of_dependents"), 0)
                .execute();
        
        ctx.insertInto(table("persons"))
                .set(field("id"), 3L)
                .set(field("first_name"), "Bob")
                .set(field("last_name"), "Johnson")
                .set(field("number_of_dependents"), 3)
                .execute();
        
        // Insert reservations
        ctx.insertInto(table("reservations"))
                .set(field("id"), 100L)
                .set(field("passenger_id"), 1L)
                .set(field("reservation_date"), "2024-01-01 10:00:00")
                .execute();
        
        ctx.insertInto(table("reservations"))
                .set(field("id"), 200L)
                .set(field("passenger_id"), 2L)
                .set(field("reservation_date"), "2024-01-02 14:30:00")
                .execute();
        
        ctx.insertInto(table("reservations"))
                .set(field("id"), 300L)
                .set(field("passenger_id"), 3L)
                .set(field("reservation_date"), "2024-01-03 09:15:00")
                .execute();
        
        personFinder = new PersonFinder(ctx);
    }

    @Test
    void testFindNameByReservationIdImplicit() {
        // When finding person name by reservation ID using implicit method
        String name = personFinder.findNameByReservationIdImplicit(100L);
        
        // Then the correct last name should be returned
        assertThat(name).isEqualTo("Doe");
    }

    @Test
    void testFindNameByReservationIdExplicit() {
        // When finding person name by reservation ID using explicit method
        String name = personFinder.findNameByReservationIdExplicit(100L);
        
        // Then the correct last name should be returned
        assertThat(name).isEqualTo("Doe");
    }

    @Test
    void testBothMethodsReturnSameResult() {
        // Given a reservation ID
        long reservationId = 200L;
        
        // When using both methods
        String implicitResult = personFinder.findNameByReservationIdImplicit(reservationId);
        String explicitResult = personFinder.findNameByReservationIdExplicit(reservationId);
        
        // Then both should return the same result
        assertThat(implicitResult).isEqualTo(explicitResult);
        assertThat(implicitResult).isEqualTo("Smith");
    }

    @Test
    void testFindNameByReservationIdImplicitNonExistent() {
        // When finding with non-existent reservation ID using implicit method
        // Then should throw IllegalArgumentException
        assertThatThrownBy(() -> personFinder.findNameByReservationIdImplicit(999L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("No person found for reservation ID: 999");
    }

    @Test
    void testFindNameByReservationIdExplicitNonExistent() {
        // When finding with non-existent reservation ID using explicit method
        // Then should return null (jOOQ fetchOne returns null for no results)
        String result = personFinder.findNameByReservationIdExplicit(999L);
        assertThat(result).isNull();
    }

    @Test
    void testMultipleReservationsForSamePerson() {
        // Add another reservation for person 1
        ctx.insertInto(table("reservations"))
                .set(field("id"), 400L)
                .set(field("passenger_id"), 1L)
                .set(field("reservation_date"), "2024-01-04 16:00:00")
                .execute();
        
        // Both reservations should return the same person's name
        String name1 = personFinder.findNameByReservationIdImplicit(100L);
        String name2 = personFinder.findNameByReservationIdImplicit(400L);
        
        assertThat(name1).isEqualTo(name2);
        assertThat(name1).isEqualTo("Doe");
    }

    @Test
    void testFindNameWithNullPassengerId() {
        // Insert a reservation with null passenger_id
        ctx.insertInto(table("reservations"))
                .set(field("id"), 500L)
                .setNull(field("passenger_id"))
                .set(field("reservation_date"), "2024-01-05 11:00:00")
                .execute();
        
        // Implicit method should throw exception for null passenger
        assertThatThrownBy(() -> personFinder.findNameByReservationIdImplicit(500L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("No person found for reservation ID: 500");
        
        // Explicit method should return null
        String explicitResult = personFinder.findNameByReservationIdExplicit(500L);
        assertThat(explicitResult).isNull();
    }

    @Test
    void testFindNameByReservationIdWithSpecialCharacters() {
        // Insert person with special characters in name
        ctx.insertInto(table("persons"))
                .set(field("id"), 4L)
                .set(field("first_name"), "Jean-Pierre")
                .set(field("last_name"), "O'Connor")
                .set(field("number_of_dependents"), 1)
                .execute();
        
        ctx.insertInto(table("reservations"))
                .set(field("id"), 600L)
                .set(field("passenger_id"), 4L)
                .set(field("reservation_date"), "2024-01-06 13:45:00")
                .execute();
        
        // Both methods should handle special characters correctly
        String implicitResult = personFinder.findNameByReservationIdImplicit(600L);
        String explicitResult = personFinder.findNameByReservationIdExplicit(600L);
        
        assertThat(implicitResult).isEqualTo("O'Connor");
        assertThat(explicitResult).isEqualTo("O'Connor");
    }

    @Test
    void testFindNameForAllExistingReservations() {
        // Test all existing reservations return correct names
        assertThat(personFinder.findNameByReservationIdImplicit(100L)).isEqualTo("Doe");
        assertThat(personFinder.findNameByReservationIdImplicit(200L)).isEqualTo("Smith");
        assertThat(personFinder.findNameByReservationIdImplicit(300L)).isEqualTo("Johnson");
        
        assertThat(personFinder.findNameByReservationIdExplicit(100L)).isEqualTo("Doe");
        assertThat(personFinder.findNameByReservationIdExplicit(200L)).isEqualTo("Smith");
        assertThat(personFinder.findNameByReservationIdExplicit(300L)).isEqualTo("Johnson");
    }

    @Test
    void testPerformanceConsistency() {
        // Both methods should perform similarly for the same query
        // This is more of a sanity check than a performance test
        
        long reservationId = 200L;
        
        // Run each method multiple times to ensure consistent results
        for (int i = 0; i < 10; i++) {
            String implicitResult = personFinder.findNameByReservationIdImplicit(reservationId);
            String explicitResult = personFinder.findNameByReservationIdExplicit(reservationId);
            
            assertThat(implicitResult).isEqualTo("Smith");
            assertThat(explicitResult).isEqualTo("Smith");
        }
    }
}