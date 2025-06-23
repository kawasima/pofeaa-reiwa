package pofeaa.combination.domain.model;

public class Identity {
    private Long value;

    private Identity(Long id) {
        this.value = id;
    }

    public static Identity of(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        return new Identity(id);
    }

    public static Identity undecided() {
        return new Identity(null);
    }

    public boolean isUndecided() {
        return value == null;
    }

    public void decide(Long value) {
        if (!isUndecided()) {
            throw new IllegalStateException("Identity is already decided");
        }
        this.value = value;
    }

    public Long asLong() {
        return value;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Identity identity = (Identity) obj;
        return java.util.Objects.equals(value, identity.value);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return "Identity{" + value + "}";
    }
}
