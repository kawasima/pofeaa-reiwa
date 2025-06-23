package pofeaa.original.behavior.unitofwork;

import org.h2.jdbcx.JdbcDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

class EditAlbumScriptIntegrationTest {
    
    private DSLContext ctx;
    private MapperRegistry mapperRegistry;
    private EditAlbumScript editAlbumScript;
    
    @BeforeEach
    void setUp() {
        // Setup in-memory database
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        
        ctx = DSL.using(ds, SQLDialect.H2);
        
        // Create albums table
        ctx.createTable(table("albums"))
            .column(field("id", SQLDataType.UUID.notNull()))
            .column(field("title", SQLDataType.VARCHAR(255)))
            .constraints(
                DSL.constraint("pk_albums").primaryKey(field("id"))
            )
            .execute();
        
        // Setup mapper registry
        mapperRegistry = new MapperRegistry();
        AlbumMapper albumMapper = new AlbumMapper(ctx);
        mapperRegistry.registerMapper(Album.class, albumMapper);
        
        // Create EditAlbumScript
        editAlbumScript = new EditAlbumScript(mapperRegistry);
    }
    
    @AfterEach
    void tearDown() {
        // Clean up
        ctx.dropTable(table("albums")).execute();
        
        // Clean up UnitOfWork
        try {
            UnitOfWork.getCurrent();
            UnitOfWork.setCurrent(null);
        } catch (IllegalStateException e) {
            // No current UnitOfWork, which is fine
        }
    }
    
    @Test
    @DisplayName("Should update album title in database")
    void shouldUpdateAlbumTitleInDatabase() {
        // Given
        UUID albumId = UUID.randomUUID();
        String originalTitle = "Dark Side of the Moon";
        String newTitle = "The Dark Side of the Moon";
        
        // Insert album directly
        ctx.insertInto(table("albums"))
            .set(field("id"), albumId)
            .set(field("title"), originalTitle)
            .execute();
        
        // When
        editAlbumScript.updateTitle(albumId, newTitle);
        
        // Then
        String titleInDb = ctx.select(field("title", String.class))
            .from(table("albums"))
            .where(field("id").eq(albumId))
            .fetchOne()
            .value1();
        
        assertThat(titleInDb).isEqualTo(newTitle);
    }
    
    @Test
    @DisplayName("Should handle non-existent album")
    void shouldHandleNonExistentAlbum() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        
        // When & Then
        assertThatThrownBy(() -> editAlbumScript.updateTitle(nonExistentId, "New Title"))
            .isInstanceOf(NullPointerException.class);
    }
    
    @Test
    @DisplayName("Should support multiple updates in same transaction")
    void shouldSupportMultipleUpdates() {
        // Given
        UUID albumId1 = UUID.randomUUID();
        UUID albumId2 = UUID.randomUUID();
        
        ctx.insertInto(table("albums"))
            .set(field("id"), albumId1)
            .set(field("title"), "Album 1")
            .execute();
        
        ctx.insertInto(table("albums"))
            .set(field("id"), albumId2)
            .set(field("title"), "Album 2")
            .execute();
        
        // When
        editAlbumScript.updateTitle(albumId1, "Updated Album 1");
        editAlbumScript.updateTitle(albumId2, "Updated Album 2");
        
        // Then
        var albums = ctx.select(field("id"), field("title"))
            .from(table("albums"))
            .orderBy(field("title"))
            .fetch();
        
        assertThat(albums).hasSize(2);
        assertThat(albums.get(0).getValue("title", String.class)).isEqualTo("Updated Album 1");
        assertThat(albums.get(1).getValue("title", String.class)).isEqualTo("Updated Album 2");
    }
    
    @Test
    @DisplayName("Should create new album using Album.create()")
    void shouldCreateNewAlbum() {
        // Given
        String title = "New Album";
        
        // When
        UnitOfWork.newCurrent(mapperRegistry);
        Album album = Album.create(title);
        UnitOfWork.getCurrent().commit();
        
        // Then
        var count = ctx.selectCount()
            .from(table("albums"))
            .where(field("id").eq(album.getId()))
            .fetchOne()
            .value1();
        
        assertThat(count).isEqualTo(1);
        
        var savedTitle = ctx.select(field("title", String.class))
            .from(table("albums"))
            .where(field("id").eq(album.getId()))
            .fetchOne()
            .value1();
        
        assertThat(savedTitle).isEqualTo(title);
    }
    
    @Test
    @DisplayName("Should track changes through UnitOfWork")
    void shouldTrackChangesThroughUnitOfWork() {
        // Given
        UUID albumId = UUID.randomUUID();
        ctx.insertInto(table("albums"))
            .set(field("id"), albumId)
            .set(field("title"), "Original")
            .execute();
        
        // When
        UnitOfWork.newCurrent(mapperRegistry);
        DataMapper<Album> mapper = mapperRegistry.getMapper(Album.class);
        Album album = mapper.find(albumId);
        
        // Make multiple changes
        album.setTitle("First Change");
        album.setTitle("Second Change");
        album.setTitle("Final Change");
        
        UnitOfWork.getCurrent().commit();
        
        // Then
        String finalTitle = ctx.select(field("title", String.class))
            .from(table("albums"))
            .where(field("id").eq(albumId))
            .fetchOne()
            .value1();
        
        assertThat(finalTitle).isEqualTo("Final Change");
    }
    
    @Test
    @DisplayName("Should handle special characters in title")
    void shouldHandleSpecialCharactersInTitle() {
        // Given
        UUID albumId = UUID.randomUUID();
        String titleWithSpecialChars = "Album's Title: \"Best of\" & More!";
        
        ctx.insertInto(table("albums"))
            .set(field("id"), albumId)
            .set(field("title"), "Original Title")
            .execute();
        
        // When
        editAlbumScript.updateTitle(albumId, titleWithSpecialChars);
        
        // Then
        String savedTitle = ctx.select(field("title", String.class))
            .from(table("albums"))
            .where(field("id").eq(albumId))
            .fetchOne()
            .value1();
        
        assertThat(savedTitle).isEqualTo(titleWithSpecialChars);
    }
}