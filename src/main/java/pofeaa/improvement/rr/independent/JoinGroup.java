package pofeaa.improvement.rr.independent;

import am.ik.yavi.core.ConstraintViolation;
import am.ik.yavi.core.Validated;
import am.ik.yavi.fn.Function2;

public class JoinGroup implements Function2<Group, User, Validated<NewMembership>> {
    private final GroupRepository groupRepository;

    public JoinGroup(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @Override
    public Validated<NewMembership> apply(Group group, User user) {
        if (groupRepository.countMembers(group) > 50) {
            return Validated.failureWith(ConstraintViolation.builder()
                    .name("group")
                    .message("Group is full"));
        }
        return Validated.successWith(NewMembership.of(user, group));
    }
}
