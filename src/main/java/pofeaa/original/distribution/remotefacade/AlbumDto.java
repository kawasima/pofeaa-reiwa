package pofeaa.original.distribution.remotefacade;

import java.io.Serializable;
import java.util.List;

public class AlbumDto implements Serializable {
    private final String title;
    private final String artist;
    private final List<TrackDto> tracks;

    public AlbumDto(String title, String artist, List<TrackDto> tracks) {
        this.title = title;
        this.artist = artist;
        this.tracks = tracks;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public List<TrackDto> getTracks() {
        return tracks;
    }
}
