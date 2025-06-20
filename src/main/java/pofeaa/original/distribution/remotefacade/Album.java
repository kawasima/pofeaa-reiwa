package pofeaa.original.distribution.remotefacade;

import java.util.List;

public class Album {
    private final String title;
    private final Artist artist;

    private final List<Track> tracks;

    public Album(String title, Artist artist, List<Track> tracks) {
        this.title = title;
        this.artist = artist;
        this.tracks = tracks;
    }

    public void addTrack(Track track) {
        tracks.add(track);
    }

    public void removeTrack(Track track) {
        tracks.remove(track);
    }

    public String getTitle() {
        return title;
    }

    public Artist getArtist() {
        return artist;
    }

    public List<Track> getTracks() {
        return tracks;
    }
}
