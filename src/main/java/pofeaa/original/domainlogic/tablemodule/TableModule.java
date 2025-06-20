package pofeaa.original.domainlogic.tablemodule;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Table;

import java.util.HashMap;
import java.util.Map;

import static org.jooq.impl.DSL.field;

public class TableModule {
    protected final Table<Record> table;
    protected final DSLContext ctx;
    private final Map<Long, Record> records;

    public TableModule(Table<Record> table, DSLContext ctx) {
        this.table = table;
        this.ctx = ctx;
        this.records = new HashMap<>();
    }

    public Record find(long id) {
        return records.computeIfAbsent(id, key -> ctx.selectFrom(table)
                .where(field("id").eq(key))
                .fetchOne());
    }
}
