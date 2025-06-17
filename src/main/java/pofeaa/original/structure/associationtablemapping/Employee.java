package pofeaa.original.structure.associationtablemapping;

import java.util.List;

public class Employee {
    private final long id;
    private final List<Skill> skills;

    public Employee(long id, List<Skill> skills) {
        this.id = id;
        this.skills = skills;
    }

    public void addSkill(Skill skill) {
        skills.add(skill);
    }

    public void removeSkill(Skill skill) {
        skills.remove(skill);
    }

    public long getId() {
        return id;
    }
}
