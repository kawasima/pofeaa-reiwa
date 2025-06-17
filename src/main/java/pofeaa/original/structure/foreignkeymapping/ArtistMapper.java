package pofeaa.original.structure.foreignkeymapping;

import org.jooq.DSLContext;
import org.jooq.Record;

public class ArtistMapper extends AbstractMapper<Artist> {
    public ArtistMapper(DSLContext ctx) {
        super(ctx);
    }

    @Override
    protected String findStatement() {
        return "SELECT * FROM artist WHERE id = ?";
    }

    @Override
    protected Artist doLoad(Long id, Record record) {
        return new Artist(
                record.get("artist_id", Long.class),
                record.get("name", String.class));
    }

    public Artist find(Long id) {
        return abstractFind(id);
    }
}
