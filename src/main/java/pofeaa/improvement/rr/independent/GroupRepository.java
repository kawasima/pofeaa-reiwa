package pofeaa.improvement.rr.independent;

import java.util.List;

public interface GroupRepository {
    List<User> findUsersByGroup(Group group);
    int countMembers(Group group);
}
