package pofeaa.original.structure.dependentmapping;

import org.jooq.DSLContext;
import org.jooq.Record;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractMapper<T> {
    abstract protected String findStatement();
    private final Map<Long, T> loadedMap = new HashMap<>();

    protected final DSLContext ctx;

    protected AbstractMapper(DSLContext ctx) {
        this.ctx = ctx;
    }

    protected T load(Record record) {
        Long id = record.get("id", Long.class);
        return loadedMap.computeIfAbsent(id, k -> {
            T domainObject = doLoad(id, record);
            doRegister(id, domainObject);
            return domainObject;
        });
    }

    protected void doRegister(Long id, T domainObject) {
        loadedMap.putIfAbsent(id, domainObject);
    }

    abstract protected T doLoad(Long id, Record record);
}
