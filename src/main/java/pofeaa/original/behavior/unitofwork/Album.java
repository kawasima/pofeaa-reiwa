package pofeaa.original.behavior.unitofwork;

import java.util.UUID;

public class Album {
    private final UUID id;
    private String title;

    public Album(UUID id, String title) {
        this.id = id;
        this.title = title;
    }

    public static Album create(String title) {
        Album album = new Album(UUID.randomUUID(), title);
        UnitOfWork.getCurrent().registerNew(album);
        return album;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        UnitOfWork.getCurrent().registerDirty(this);
    }
}
