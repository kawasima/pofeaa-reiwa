package pofeaa.combination.domain.model;

import pofeaa.original.base.money.Money;

import java.time.LocalDateTime;

public class Activity {
    private final Identity id;

    private final Identity ownerAccountId;
    private final Identity sourceAccountId;
    private final Identity targetAccountId;

    private final LocalDateTime timestamp;

    private final Money money;

    private Activity(Identity id, Identity ownerAccountId, Identity sourceAccountId, Identity targetAccountId, LocalDateTime timestamp, Money money) {
        this.id = id;
        this.ownerAccountId = ownerAccountId;
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.timestamp = timestamp;
        this.money = money;
    }

    public static Activity of(Identity id, Identity ownerAccountId, Identity sourceAccountId, Identity targetAccountId, LocalDateTime timestamp, Money money) {
        if (id == null || ownerAccountId == null || sourceAccountId == null || targetAccountId == null || timestamp == null || money == null) {
            throw new IllegalArgumentException("All parameters must be provided");
        }
        return new Activity(id, ownerAccountId, sourceAccountId, targetAccountId, timestamp, money);
    }

    public Identity getId() {
        return id;
    }

    public Identity getOwnerAccountId() {
        return ownerAccountId;
    }

    public Identity getSourceAccountId() {
        return sourceAccountId;
    }

    public Identity getTargetAccountId() {
        return targetAccountId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Money getMoney() {
        return money;
    }
}
