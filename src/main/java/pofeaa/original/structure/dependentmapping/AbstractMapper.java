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

    /**
     * Maps a database record to a domain model object with identity management.
     * 
     * This method transforms a Record Set (database row) into a domain model instance,
     * implementing the Identity Map pattern to ensure each database record is loaded
     * only once per session. If the object with the given ID has already been loaded,
     * it returns the cached instance instead of creating a new one.
     * 
     * @param record the database record containing the data to be mapped, must include an 'id' field
     * @return the domain model object corresponding to the record, either newly created or 
     *         retrieved from the identity map
     */
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
