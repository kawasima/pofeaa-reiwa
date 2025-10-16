package pofeaa.improvement.relationship.parity.unidirectional;

import org.jooq.DSLContext;
import org.jooq.Record;
import pofeaa.combination.domain.model.Identity;

import java.util.List;
import java.util.Optional;

import static org.jooq.impl.DSL.table;

public class StudentMapper {
    private final DSLContext ctx;
    private final CourseMapper courseMapper;

    public StudentMapper(DSLContext ctx, CourseMapper courseMapper) {
        this.ctx = ctx;
        this.courseMapper = courseMapper;
    }

    public Optional<Student> findById(Identity id) {
        Record studentRec = ctx.select()
                .from(table("student"))
                .where(table("student").field("id", Long.class).eq(id.asLong()))
                .fetchOne();
        if (studentRec == null) {
            return Optional.empty();
        }
        List<Course> courses = courseMapper.findByStudentId(id);
        return Optional.of(new Student(
                Identity.of(studentRec.get("id", Long.class)),
                studentRec.get("name", String.class),
                courses
        ));
    }
}
