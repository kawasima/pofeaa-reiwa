package pofeaa.original.structure.foreignkeymapping;

public class Album {
    private final long id;
    private final String title;
    private final Artist artist;

    public Album(long id, String title, Artist artist) {
        this.id = id;
        this.title = title;
        this.artist = artist;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Artist getArtist() {
        return artist;
    }
}
