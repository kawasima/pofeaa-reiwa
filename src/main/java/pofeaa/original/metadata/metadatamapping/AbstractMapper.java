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
        dataMap.getColumns().forEach(column -> {
            column.setField(domainObject, record.get(column.getColumnName()));
        });
    }
}
