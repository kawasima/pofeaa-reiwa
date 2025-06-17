package pofeaa.original.structure.foreignkeymapping;

import org.jooq.DSLContext;
import org.jooq.Record;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class AlbumMapper extends AbstractMapper<Album> {
    private final ArtistMapper artistMapper;

    protected String findStatement() {
        return "";
    }

    public AlbumMapper(DSLContext ctx, ArtistMapper artistMapper) {
        super(ctx);
        this.artistMapper = artistMapper;
    }

    @Override
    protected Album doLoad(Long id, Record record) {
        String title = record.get("title", String.class);
        long artistId = record.get("artist_id", Long.class);
        Artist artist = artistMapper.find(artistId);
        return new Album(id, title, artist);
    }

    public Album find(Long id) {
        Record record = ctx.select()
                .from(table("album"))
                .where(field("id").eq(id))
                .fetchOne();
        return load(record);
    }
}
