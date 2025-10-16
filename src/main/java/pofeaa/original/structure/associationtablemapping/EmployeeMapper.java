package pofeaa.original.structure.associationtablemapping;

import org.jooq.DSLContext;
import org.jooq.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class EmployeeMapper extends AbstractMapper<Employee> {
    private final SkillMapper skillMapper;

    public EmployeeMapper(DSLContext ctx, SkillMapper skillMapper) {
        super(ctx);
        this.skillMapper = skillMapper;
    }

    public Employee find(Long id) {
        Record record = ctx.select()
                .from("employee")
                .where("id = ?", id)
                .fetchOne();
        return load(record);

    }

    protected void loadSkills(Employee employee) {
        skillLinkRows(employee).map(skillMapper::load)
                .forEach(employee::addSkill);
    }

    private Stream<Record> skillLinkRows(Employee employee) {
        return ctx.select()
                .from(table("employee_skill"))
                .join(table("skill")).on(table("employee_skill").field("skill_id", Long.class).eq(table("skill").field("id", Long.class)))
                .where(field("employee_id").eq(employee.getId()))
                .fetchStream();
    }
    @Override
    protected Employee doLoad(Long id, Record record) {
        Employee employee = new Employee(id, new ArrayList<>());
        loadSkills(employee);
        return employee;
    }
}
