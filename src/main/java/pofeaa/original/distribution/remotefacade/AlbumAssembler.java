package pofeaa.original.distribution.remotefacade;

import java.util.List;
import java.util.stream.Collectors;

public class AlbumAssembler {
    public AlbumDto writeDto(Album album) {
        if (album == null) {
            return null;
        }
        String title = album.getTitle();
        Artist artist = album.getArtist();
        String artistName = artist != null ? artist.getName() : null;
        List<TrackDto> trackDtos = album.getTracks().stream()
                .map(this::writeTrack)
                .collect(Collectors.toList());
        return new AlbumDto(title, artistName, trackDtos);
    }

    public TrackDto writeTrack(Track track) {
        if (track == null) {
            return null;
        }
        String title = track.getTitle();
        List<String> performerNames = track.getPerformers().stream()
                .map(Artist::getName)
                .collect(Collectors.toList());
        return new TrackDto(title, performerNames);
    }
}
