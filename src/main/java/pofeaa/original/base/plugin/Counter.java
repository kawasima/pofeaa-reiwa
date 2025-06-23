package pofeaa.original.base.plugin;

import java.util.concurrent.atomic.AtomicLong;

public class Counter implements IdGenerator {
    private final AtomicLong count = new AtomicLong(0);

    @Override
    public Long nextId() {
        return count.getAndIncrement();
    }
}
