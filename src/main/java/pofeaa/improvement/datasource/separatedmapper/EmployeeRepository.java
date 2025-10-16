package pofeaa.improvement.datasource.separatedmapper;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.table;

public class EmployeeRepository {
    private final EmployeeMapper employeeMapper;
    private final SkillMapper skillMapper;
    private final DSLContext ctx;

    public EmployeeRepository(DSLContext ctx, EmployeeMapper employeeMapper, SkillMapper skillMapper) {
        this.ctx = ctx;
        this.employeeMapper = employeeMapper;
        this.skillMapper = skillMapper;
    }

    public List<Employee> findAll() {
        Result<Record> results = ctx.select()
                .from(table("employee"))
                .fetch();
        List<Long> employeeIds = results.getValues("id", Long.class);
        Map<Long, List<Skill>> skillsByEmployeeId = skillsByEmployeeIds(employeeIds);
        
        return results.map(record -> employeeMapper.toDomain(record, skillsByEmployeeId.getOrDefault(
                record.get("id", Long.class), List.of()
        )));
    }
    
    private Map<Long, List<Skill>> skillsByEmployeeIds(List<Long> employeeIds) {
        if (employeeIds.isEmpty()) {
            return Map.of();
        }
        
        return ctx.select()
                .from(table("employee_skill"))
                .join(table("skill"))
                .on(table("employee_skill").field("skill_id", Long.class).eq(table("skill").field("id", Long.class)))
                .where(table("employee_skill").field("employee_id", Long.class).in(employeeIds))
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(
                        record -> record.get(table("employee_skill").field("employee_id", Long.class)),
                        Collectors.mapping(
                                skillMapper::toDomain,
                                Collectors.toList()
                        )
                ));
    }
}
