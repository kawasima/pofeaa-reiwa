package pofeaa.original.distribution.remotefacade;

/**
 * AlbumService is a Remote Facade.
 */
public class AlbumService {
    private final AlbumFinder albumFinder;
    private final AlbumAssembler albumAssembler;

    public AlbumService(AlbumFinder albumFinder, AlbumAssembler albumAssembler) {
        this.albumFinder = albumFinder;
        this.albumAssembler = albumAssembler;
    }

    public AlbumDto getAlbum(String key) {
        Album album = albumFinder.findAlbum(key);
        if (album == null) {
            return null;
        }
        return albumAssembler.writeDto(album);
    }
}
