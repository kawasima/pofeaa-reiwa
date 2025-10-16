package pofeaa.improvement.rr.independent;

public class NewMembership {
    private final User user;
    private final Group group;

    private NewMembership(User user, Group group) {
        this.user = user;
        this.group = group;

    }

    public static NewMembership of(User user, Group group) {
        return new NewMembership(user, group);
    }
}
