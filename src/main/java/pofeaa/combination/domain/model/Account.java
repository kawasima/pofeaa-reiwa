package pofeaa.combination.domain.model;

import pofeaa.original.base.money.Money;

import java.time.LocalDateTime;

public abstract class Account {
    private final Identity id;
    private final Money baselineBalance;
    private final ActivityWindow activityWindow;

    protected Account(Identity id, Money baselineBalance, ActivityWindow activityWindow) {
        this.id = id;
        this.baselineBalance = baselineBalance;
        this.activityWindow = activityWindow;
    }

    public Identity getId() {
        return id;
    }

    public Money getBaselineBalance() {
        return baselineBalance;
    }

    public ActivityWindow getActivityWindow() {
        return activityWindow;
    }

    public Money calculateBalance() {
        return baselineBalance.add(activityWindow.calculateBalance(id));
    }

    public boolean withdraw(Money money, Identity targetAccountId) {
        if (!mayWithdraw(money)) {
            return false;
        }

        Activity withdrawal = Activity.of(
                Identity.undecided(),
                id,
                id,
                targetAccountId,
                LocalDateTime.now(),
                money);
        activityWindow.addActivity(withdrawal);
        return true;
    }

    public boolean deposit(Money money, Identity sourceAccountId) {
        Activity deposit = Activity.of(
                Identity.undecided(),
                id,
                sourceAccountId,
                id,
                LocalDateTime.now(),
                money);
        activityWindow.addActivity(deposit);
        return true;
    }

    private boolean mayWithdraw(Money money) {
        return calculateBalance().subtract(money).isPositiveOrZero();
    }
}
