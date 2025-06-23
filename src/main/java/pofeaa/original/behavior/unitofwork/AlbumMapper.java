package pofeaa.original.behavior.unitofwork;

import org.jooq.DSLContext;

import java.util.UUID;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class AlbumMapper implements DataMapper<Album> {
    private final DSLContext ctx;

    public AlbumMapper(DSLContext ctx) {
        this.ctx = ctx;
    }

    public Album find(UUID id) {
        return ctx.selectFrom(table("albums"))
              .where(field("id").eq(id))
              .fetchOne()
              .map(rec -> new Album(rec.getValue("ID", UUID.class), rec.getValue("TITLE", String.class)));
    }
    @Override
    public void insert(Album album) {
        ctx.insertInto(table("albums"))
           .set(field("id"), album.getId())
           .set(field("title"), album.getTitle())
           .execute();
    }

    @Override
    public void update(Album album) {
        ctx.update(table("albums"))
           .set(field("title"), album.getTitle())
           .where(field("id").eq(album.getId()))
           .execute();
    }

    @Override
    public void delete(Album album) {
        ctx.deleteFrom(table("albums"))
           .where(field("id").eq(album.getId()))
           .execute();
    }
}
