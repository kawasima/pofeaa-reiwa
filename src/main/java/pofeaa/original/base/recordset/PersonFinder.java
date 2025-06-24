package pofeaa.original.base.recordset;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import pofeaa.combination.transactionscript.generated.tables.records.PersonsRecord;

import java.util.Optional;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;
import static pofeaa.combination.transactionscript.generated.Tables.PERSONS;
import static pofeaa.combination.transactionscript.generated.Tables.RESERVATIONS;

public class PersonFinder {
    private final DSLContext ctx;

    public PersonFinder(DSLContext ctx) {
        this.ctx = ctx;
    }

    public String findNameByReservationIdImplicit(long reservationId) {
        Record record = ctx.select()
                .from(table("reservations"))
                .join(table("persons"))
                .on(field("reservations.passenger_id").eq(field("persons.id")))
                .where(field("reservations.id").eq(reservationId))
                .fetchOne();
        return Optional.ofNullable(record)
                .map(r -> r.get("LAST_NAME", String.class))
                .orElseThrow(() -> new IllegalArgumentException("No person found for reservation ID: " + reservationId));
    }

    public String findNameByReservationIdExplicit(long reservationId) {
        return ctx.selectFrom(RESERVATIONS.join(PERSONS)
                        .on(RESERVATIONS.PASSENGER_ID.eq(PERSONS.ID)))
                .where(RESERVATIONS.ID.eq(reservationId))
                .fetchOne(PERSONS.LAST_NAME);
    }

    public Record findPersonByIdImplicit(long personId) {
        return ctx.select()
                .from(PERSONS)
                .where(PERSONS.ID.eq(personId))
                .fetchOne();
    }

    public PersonsRecord findPersonByIdExplicit(long personId) {
        return ctx.selectFrom(PERSONS)
                .where(PERSONS.ID.eq(personId))
                .fetchOne();
    }


}
