package pofeaa.original.distribution.remotefacade;

import java.util.List;

public class Track {
    private String title;

    private List<Artist> performers;

    public List<Artist> getPerformers() {
        return performers;
    }

    public void addPerformer(Artist performer) {
        performers.add(performer);
    }

    public void removePerformer(Artist performer) {
        performers.remove(performer);
    }

    public String getTitle() {
        return title;
    }
}