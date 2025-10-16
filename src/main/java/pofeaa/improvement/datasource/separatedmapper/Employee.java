package pofeaa.improvement.datasource.separatedmapper;

import java.util.List;

public class Employee {
    private final Long id;
    private final String name;
    private final List<Skill> skills;

    public Employee(Long id, String name, List<Skill> skills) {
        this.id = id;
        this.name = name;
        this.skills = skills;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Skill> getSkills() {
        return skills;
    }
}
