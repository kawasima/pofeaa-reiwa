package pofeaa.original.behavior.lazyload;

import java.util.List;

public interface VirtualListLoader<T> {
    List<T> load();
}
