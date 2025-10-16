package pofeaa.original.behavior.lazyload;

public class ValueHolderImpl<T> implements ValueHolder<T> {
    private T value;
    private final ValueLoader<T> loader;

    public ValueHolderImpl(ValueLoader<T> loader) {
        this.loader = loader;
    }

    @Override
    public T getValue() {
        if (value == null) {
            value = loader.load();
        }
        return value;
    }
}
