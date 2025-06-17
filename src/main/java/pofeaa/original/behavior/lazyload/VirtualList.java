package pofeaa.original.behavior.lazyload;

import java.util.AbstractList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Thread-safe implementation of a virtual list that lazily loads its contents.
 * Uses AtomicReference with lazy initialization for modern thread-safe implementation.
 * 
 * Note: This class ensures thread-safe lazy loading, but the thread-safety of
 * list operations depends on the implementation returned by the loader.
 * For full thread-safety, the loader should return a synchronized list.
 * 
 * @param <T> the type of elements in this list
 */
public class VirtualList<T> extends AbstractList<T> {
    private final AtomicReference<List<T>> sourceRef = new AtomicReference<>();
    private final Supplier<List<T>> supplier;

    public VirtualList(VirtualListLoader<T> loader) {
        this(loader, false);
    }

    public VirtualList(VirtualListLoader<T> loader, boolean synchronizeList) {
        this.supplier = synchronizeList ? 
            () -> Collections.synchronizedList(loader.load()) : 
            loader::load;
    }

    /**
     * Uses AtomicReference for thread-safe lazy initialization.
     * This is a modern alternative to double-checked locking.
     */
    private List<T> getSource() {
        return sourceRef.updateAndGet(existing -> 
            existing != null ? existing : supplier.get()
        );
    }

    @Override
    public int size() {
        return getSource().size();
    }

    @Override
    public T get(int index) {
        return getSource().get(index);
    }

    @Override
    public T set(int index, T element) {
        return getSource().set(index, element);
    }

    @Override
    public void add(int index, T element) {
        getSource().add(index, element);
    }

    @Override
    public T remove(int index) {
        return getSource().remove(index);
    }

    @Override
    public void clear() {
        List<T> list = sourceRef.get();
        if (list != null) {
            list.clear();
        }
    }

    @Override
    public boolean isEmpty() {
        List<T> list = sourceRef.get();
        return list == null || list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return getSource().contains(o);
    }

    @Override
    public int indexOf(Object o) {
        return getSource().indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return getSource().lastIndexOf(o);
    }
}
