package pofeaa.original.behavior.unitofwork;

import java.util.UUID;

public class EditAlbumScript {
    private final MapperRegistry mapperRegistry;

    public EditAlbumScript(MapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
    }

    public void updateTitle(UUID albumId, String title) {
        UnitOfWork.newCurrent(mapperRegistry);
        DataMapper<Album> albumMapper = mapperRegistry.getMapper(Album.class);
        Album album = albumMapper.find(albumId);
        album.setTitle(title);
        UnitOfWork.getCurrent().commit();
    }
}
