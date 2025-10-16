package pofeaa.original.structure.associationtablemapping;

import org.jooq.DSLContext;
import org.jooq.Record;

public class SkillMapper extends AbstractMapper<Skill> {
    protected SkillMapper(DSLContext ctx) {
        super(ctx);
    }

    public Skill find(Long id) {
        Record record = ctx.select()
                .from("skill")
                .where("id = ?", id)
                .fetchOne();
        return load(record);
    }

    @Override
    protected Skill doLoad(Long id, Record record) {
        String name = record.get("name", String.class);
        return new Skill(id, name);
    }
}
