package pofeaa.improvement.datasource.separatedmapper;

public class Skill {
    private final Long id;
    private final String name;

    public Skill(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
