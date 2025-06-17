package pofeaa.original.datasource.datamapper;

public class Identity {
    private final Long value;
    private static final Identity UNDECIDED = new Identity(null);

    private Identity(Long value) {
        this.value = value;
    }

    public static Identity of(Long value) {
        if (value == null || value < 0) {
            throw new IllegalArgumentException("Identity value must be a non-negative Long.");
        }
        return new Identity(value);
    }

    public static Identity undecided() {
        return UNDECIDED;
    }

    public Long getValue() {
        if (value == null) {
            throw new IllegalStateException("Identity value is undecided.");
        }
        return value;
    }

    public Identity decide(Long value) {
        if (this.value != null) {
            throw new IllegalStateException("Identity is already decided.");
        }
        return of(value);
    }
}
