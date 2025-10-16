package pofeaa.original.structure.associationtablemapping;

public class Skill {
    private final long id;
    private final String name;

    public Skill(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
