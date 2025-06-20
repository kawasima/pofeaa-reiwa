package pofeaa.original.structure.foreignkeymapping;

import org.jooq.DSLContext;
import org.jooq.Record;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class ArtistMapper extends AbstractMapper<Artist> {
    public ArtistMapper(DSLContext ctx) {
        super(ctx);
    }

    @Override
    protected String findStatement() {
        return "SELECT * FROM artists WHERE id = ?";
    }

    @Override
    protected Artist doLoad(Long id, Record record) {
        return new Artist(
                record.get("artist_id", Long.class),
                record.get("name", String.class));
    }

    public Artist find(Long id) {
        Record record = ctx.select()
                .from(table("artists"))
                .where(field("id").eq(id))
                .fetchOne();
        return doLoad(id, record);
    }
}
