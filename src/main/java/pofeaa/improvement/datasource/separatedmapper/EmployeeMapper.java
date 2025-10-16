package pofeaa.improvement.datasource.separatedmapper;

import org.jooq.DSLContext;
import org.jooq.Record;

import java.util.List;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class EmployeeMapper {
    private final DSLContext ctx;

    public EmployeeMapper(DSLContext ctx) {
        this.ctx = ctx;
    }

    public Employee toDomain(Record record, List<Skill> skills) {
        return new Employee(
                record.get("id", Long.class),
                record.get("name", String.class),
                skills
        );
    }

    public Record toRecord(Employee employee) {
        Record record = ctx.newRecord(table(""));
        record.set(field("id"), employee.getId());
        record.set(field("skills"), employee.getSkills().stream().map(skill -> skill.getId()).toList());
        return record;
    }

}
