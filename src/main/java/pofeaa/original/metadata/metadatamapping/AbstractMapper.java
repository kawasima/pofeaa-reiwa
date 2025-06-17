package pofeaa.original.metadata.metadatamapping;

import org.jooq.DSLContext;
import org.jooq.Record;

import static org.jooq.impl.DSL.field;

public abstract class AbstractMapper<T> {
    protected DataMap dataMap;
    protected DSLContext ctx;

    public T findObject(Long key) {
        ctx.select(dataMap.columnList())
                .from(dataMap.getTable())
                .where(field("id").eq(key));
    }

    public abstract T load(Record record);
}
