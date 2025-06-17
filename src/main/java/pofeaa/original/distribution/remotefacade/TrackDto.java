package pofeaa.original.distribution.remotefacade;

import java.util.List;

public class TrackDto {
    private final String title;
    private final List<String> performers;

    public TrackDto(String title, List<String> performers) {
        this.title = title;
        this.performers = performers;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getPerformers() {
        return performers;
    }
}
