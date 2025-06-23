package pofeaa.original.base.recordset;

import org.jooq.DSLContext;
import org.jooq.Record;

import java.util.Optional;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class ImplicitInterface {
    private final DSLContext ctx;

    public ImplicitInterface(DSLContext ctx) {
        this.ctx = ctx;
    }

    public String findNameByReservationId(long reservationId) {
        Record record = ctx.select()
                .from(table("reservations"))
                .join(table("persons"))
                .on(field("reservations.passenger_id").eq(field("persons.id")))
                .where(field("id").eq(reservationId))
                .fetchOne();
        return Optional.ofNullable(record)
                .map(r -> r.get("persons.last_name", String.class))
                .orElseThrow(() -> new IllegalArgumentException("No person found for reservation ID: " + reservationId));
    }
}
