package pofeaa.combination.domain.model;

import pofeaa.original.base.money.Money;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ActivityWindow {
    private final List<Activity> activities;

    public LocalDateTime getStartTimestamp() {
        return activities.stream()
                .min(Comparator.comparing(Activity::getTimestamp))
                .orElseThrow(() -> new IllegalStateException("No activities in the window"))
                .getTimestamp();
    }

    public LocalDateTime getEndTimestamp() {
        return activities.stream()
                .max(Comparator.comparing(Activity::getTimestamp))
                .orElseThrow(() -> new IllegalStateException("No activities in the window"))
                .getTimestamp();
    }

    public Money calculateBalance(Identity accountId) {
        Money depositBalance = activities.stream()
                .filter(activity -> activity.getTargetAccountId().equals(accountId))
                .map(Activity::getMoney)
                .reduce(Money.ZERO, Money::add);

        Money withdrawalBalance = activities.stream()
                .filter(activity -> activity.getSourceAccountId().equals(accountId))
                .map(Activity::getMoney)
                .reduce(Money.ZERO, Money::add);

        return depositBalance.subtract(withdrawalBalance);
    }

    public ActivityWindow(List<Activity> activities) {
        this.activities = activities != null ? new ArrayList<>(activities) : new ArrayList<>();
    }

    public ActivityWindow(Activity... activities) {
        this.activities = new ArrayList<>(List.of(activities));
    }
    
    public ActivityWindow() {
        this.activities = new ArrayList<>();
    }

    public List<Activity> getActivities() {
        return Collections.unmodifiableList(activities);
    }

    public void addActivity(Activity activity) {
        activities.add(activity);
    }
}
