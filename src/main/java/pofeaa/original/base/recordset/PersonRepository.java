package pofeaa.original.base.recordset;

import pofeaa.combination.transactionscript.generated.tables.records.PersonsRecord;

public class PersonRepository {
    private final PersonFinder personFinder;

    public PersonRepository(PersonFinder personFinder) {
        this.personFinder = personFinder;
    }

    public Person getPersonImplicit(long id) {
        org.jooq.Record record = personFinder.findPersonByIdImplicit(id);
        if (record == null) {
            return null;
        }
        return record.map(rec -> Person.of(
            rec.get("FIRST_NAME", String.class),
            rec.get("LAST_NAME", String.class)
        ));
    }

    public Person getPersonExplicit(long id) {
        PersonsRecord record = personFinder.findPersonByIdExplicit(id);
        if (record == null) {
            return null;
        }
        return record.map(r -> {
            PersonsRecord pRec = (PersonsRecord) r;
            return Person.of(
                pRec.getFirstName(),
                pRec.getLastName()
            );
        });
    }
}
