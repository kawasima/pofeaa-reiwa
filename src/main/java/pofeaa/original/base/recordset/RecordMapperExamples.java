package pofeaa.original.base.recordset;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import pofeaa.combination.transactionscript.generated.tables.records.PersonsRecord;

import static pofeaa.combination.transactionscript.generated.Tables.PERSONS;

/**
 * Examples demonstrating different approaches to using RecordMapper with jOOQ.
 * 
 * This class shows the advantages of using RecordMapper for:
 * 1. Cleaner code separation
 * 2. Reusable mapping logic
 * 3. Type safety
 * 4. Easier testing and maintenance
 */
public class RecordMapperExamples {
    private final DSLContext ctx;
    
    // Static mappers can be reused across the application
    private static final RecordMapper<Record, Person> GENERIC_PERSON_MAPPER = record -> 
        new Person(
            record.get("FIRST_NAME", String.class),
            record.get("LAST_NAME", String.class)
        );
    
    private static final RecordMapper<PersonsRecord, Person> TYPED_PERSON_MAPPER = record ->
        new Person(
            record.getFirstName(),
            record.getLastName()
        );

    public RecordMapperExamples(DSLContext ctx) {
        this.ctx = ctx;
    }

    // Approach 1: Manual mapping (not using RecordMapper)
    public Person findPersonManual(long personId) {
        PersonsRecord record = ctx.selectFrom(PERSONS)
                .where(PERSONS.ID.eq(personId))
                .fetchOne();
        
        if (record == null) {
            return null;
        }
        
        // Manual mapping - repetitive and error-prone
        return new Person(
                record.getFirstName(),
                record.getLastName()
        );
    }

    // Approach 2: Using RecordMapper in separate step
    public Person findPersonWithSeparateMapper(long personId) {
        PersonsRecord record = ctx.selectFrom(PERSONS)
                .where(PERSONS.ID.eq(personId))
                .fetchOne();
        
        return record != null ? TYPED_PERSON_MAPPER.map(record) : null;
    }

    // Approach 3: Using RecordMapper directly with fetchOne() - BEST APPROACH
    public Person findPersonWithIntegratedMapper(long personId) {
        // This is the cleanest approach - mapping happens directly in the query
        return ctx.selectFrom(PERSONS)
                .where(PERSONS.ID.eq(personId))
                .fetchOne(TYPED_PERSON_MAPPER);
    }

    // Approach 4: Using inline RecordMapper (good for one-off cases)
    public Person findPersonWithInlineMapper(long personId) {
        return ctx.selectFrom(PERSONS)
                .where(PERSONS.ID.eq(personId))
                .fetchOne(record -> new Person(
                    record.getFirstName(),
                    record.getLastName()
                ));
    }

    // Approach 5: Using generic Record with RecordMapper
    public Person findPersonGenericWithMapper(long personId) {
        return ctx.select(PERSONS.FIRST_NAME, PERSONS.LAST_NAME)
                .from(PERSONS)
                .where(PERSONS.ID.eq(personId))
                .fetchOne(GENERIC_PERSON_MAPPER);
    }

    /**
     * Advantages of using RecordMapper:
     * 
     * 1. SEPARATION OF CONCERNS: Mapping logic is separated from query logic
     * 2. REUSABILITY: Same mapper can be used across different queries
     * 3. TESTABILITY: Mappers can be tested independently
     * 4. MAINTAINABILITY: Changes to mapping logic are centralized
     * 5. TYPE SAFETY: Compile-time checking of field access
     * 6. PERFORMANCE: Direct mapping in fetchOne() is more efficient
     * 7. NULL SAFETY: jOOQ handles null records automatically when using fetchOne(mapper)
     * 8. CONSISTENCY: Same mapping behavior across the application
     */
}