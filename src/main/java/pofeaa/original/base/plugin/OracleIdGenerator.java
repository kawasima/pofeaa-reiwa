package pofeaa.original.base.plugin;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;

public class OracleIdGenerator implements IdGenerator {
    private final DSLContext ctx;
    private final String sequenceName;

    public OracleIdGenerator(DSLContext ctx, String sequenceName) {
        this.ctx = ctx;
        this.sequenceName = sequenceName;
    }

    @Override
    public Long nextId() {
        try {
            return ctx.select(DSL.field(sequenceName + ".nextval", Long.class))
               .from("dual")
               .fetchOptional(0, Long.class)
               .orElseThrow(() -> new RuntimeException("Failed to generate ID from Oracle sequence: " + sequenceName));
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate ID from Oracle sequence: " + sequenceName, e);
        }
    }
}
