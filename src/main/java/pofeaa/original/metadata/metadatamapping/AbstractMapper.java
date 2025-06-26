package pofeaa.original.metadata.metadatamapping;

import org.jooq.DSLContext;
import org.jooq.Record;

import java.lang.reflect.InvocationTargetException;

import static org.jooq.impl.DSL.field;

public abstract class AbstractMapper<T> {
    protected final DataMap<T> dataMap;
    protected final DSLContext ctx;

    protected AbstractMapper(DataMap<T> dataMap, DSLContext ctx) {
        this.dataMap = dataMap;
        this.ctx = ctx;
    }

    public T findObject(Long key) {
        Record record = ctx.select(dataMap.columnList())
                .from(dataMap.getTable())
                .where(field("id").eq(key))
                .fetchOne();
        return load(record);
    }

    /**
     * Maps a database record to a domain model object.
     * 
     * This method performs the transformation from a Record Set (database row) to 
     * a domain model instance. It creates a new instance of the domain class and 
     * populates its fields with values from the database record using metadata mapping.
     * 
     * @param record the database record containing field values to be mapped
     * @return a fully populated domain model object with data from the record
     * @throws RuntimeException if the domain object cannot be instantiated due to
     *         reflection errors (missing constructor, access restrictions, etc.)
     */
    public T load(Record record) {
        try {
            @SuppressWarnings("unchecked")
            T domainObject = (T) dataMap.getDomainClass().getConstructor().newInstance();
            loadFields(record, domainObject);
            return domainObject;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("Error creating instance of " + dataMap.getDomainClass().getName(), e);
        }
    }

    private void loadFields(Record record, T domainObject) {
        dataMap.getColumns().forEach(column -> column.setField(domainObject, record.get(column.getColumnName())));
    }
}
