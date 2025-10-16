package pofeaa.improvement.datasource.separatedmapper;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;

import java.util.List;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class SkillMapper {
    private static final List<Field<?>> FIELDS = List.of(
            field("id", Long.class),
            field("name", String.class)
    );
    private final DSLContext ctx;

    public SkillMapper(DSLContext ctx) {
        this.ctx = ctx;
    }

    public Skill toDomain(Record record) {
        return new Skill(
                record.get("id", Long.class),
                record.get("name", String.class)
        );
    }

    public Record toRecord(Skill skill) {
        Record record = ctx.newRecord(FIELDS);
        record.set(field("id"), skill.getId());
        record.set(field("name"), skill.getName());
        return record;
    }
}
